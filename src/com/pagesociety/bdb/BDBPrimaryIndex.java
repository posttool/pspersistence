package com.pagesociety.bdb;

import java.io.FileNotFoundException;
import java.util.List;

import org.apache.log4j.Logger;

import com.pagesociety.bdb.binding.FieldBinding;
import com.pagesociety.bdb.binding.EntityBinding;
import com.pagesociety.bdb.index.iterator.IterableIndex;
import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.pagesociety.persistence.QueryResult;
import com.pagesociety.persistence.Types;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.Cursor;
import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseConfig;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DatabaseType;
import com.sleepycat.db.DeadlockException;
import com.sleepycat.db.Environment;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.Sequence;
import com.sleepycat.db.SequenceConfig;
import com.sleepycat.db.Transaction;
import com.sleepycat.db.TransactionConfig;

public class BDBPrimaryIndex implements IterableIndex
{
	private static final Logger logger = Logger.getLogger(BDBPrimaryIndex.class);
	
	private EntityDefinition _def;
	private EntityBinding	 _binding;
	private Database 		 _dbh;
	private Environment 	 _environment;
	private Database 		 _sequence_db;
	private Sequence 		 _sequence;
	private DatabaseEntry 	 _sequence_key;
	
	protected BDBPrimaryIndex(EntityBinding binding)
	{
		_binding = binding;
	}
	
	
	protected void setup(Environment environment,EntityDefinition def) throws PersistenceException
	{
		
		_def = def;
		_environment = environment;
		_sequence_key = new DatabaseEntry();
		try{
			FieldBinding.valueToEntry(Types.TYPE_STRING,"SEQ",_sequence_key);
			open(def);
		}catch(Exception e)
		{
			e.printStackTrace();
			throw new PersistenceException("FAILED OPENING PRIMARY ENTITY INDEX FOR "+def.getName());
		}
	}
	
	private void open(EntityDefinition def) throws Exception
	{
		_def = def;
		open_primary_index_db();
		open_sequence_db(def.getName());
		open_sequence();		
	}
	
	public Database getDbh()
	{
		return _dbh;
	}
	
	public EntityDefinition getEntityDefinition()
	{
		return _def;
	}
	
	protected void setEntityDefinition(EntityDefinition def)
	{
		_def = def;
	}
	
	
	private void open_primary_index_db() throws DatabaseException,FileNotFoundException
	{
		
		String entity_name = _def.getName();
		String dbname = get_primary_index_db_name(entity_name);
		DatabaseConfig cfg = get_primary_db_config_btree();

		logger.info("\tOPENING PRIMARY INDEX FOR "+entity_name+"..."+dbname);
		_dbh = _environment.openDatabase(null, dbname,null, cfg);

	}
	
	protected void renameDb(EntityDefinition olddef,EntityDefinition newdef)
	{
		String old_name = olddef.getName();
		String new_name = newdef.getName();
		
		String old_db_name = get_primary_index_db_name(old_name);
		String new_db_name = get_primary_index_db_name(new_name);
		
		String old_seq_name = get_sequence_db_name(old_name);
		String new_seq_name = get_sequence_db_name(new_name);
		
		try {
			close();
			System.out.println("\tRENAMING "+old_db_name+" to "+new_db_name);
			_environment.renameDatabase(null,old_db_name,null,new_db_name);
			System.out.println("RENAMING "+old_seq_name+" to "+new_seq_name);
			_environment.renameDatabase(null,old_seq_name,null,new_seq_name);
			open(newdef);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	protected  void close() throws DatabaseException
	{
		//logger.info("CLOSING PRIMARY ENTITY DB FOR "+_def.getName());
	
		_dbh.sync();
		_dbh.close();
		_sequence.close();
		_sequence_db.close();
	}

	protected  void close(List<BDBSecondaryIndex> sec_idxes) throws DatabaseException
	{
		close();
		for(int ii = 0; ii < sec_idxes.size();ii++)
		{
			BDBSecondaryIndex s_idx = sec_idxes.get(ii);
			s_idx.close();
		}
	}

	static TransactionConfig DELETE_CFG = new TransactionConfig();
	//static {DELETE_CFG.setReadUncommitted(false);}	
	protected DatabaseEntry saveEntity(Transaction parent_txn,Entity e) throws DatabaseException
	{

		DatabaseEntry pkey 			= new DatabaseEntry();
		long seqnum 				= Entity.UNDEFINED;
		int retry_count = 0;
		Transaction txn = null;		

		/* THIS IS WHERE WE DECIDE TO DO AN UPDATE INSTEAD OF AN INSERT */
			long entity_id = e.getId();
			if(entity_id == Entity.UNDEFINED)
			{
				seqnum = _sequence.get(null, 1);
				LongBinding.longToEntry(seqnum, pkey);//we dont use valueToEntry because we dont want the
													//nullflag first
			}
			else
			{
				seqnum = entity_id;
				LongBinding.longToEntry(seqnum, pkey);//we dont use valueToEntry because we dont want the
				retry_count = 0;
				Transaction delete_txn = null;
				while(retry_count < BDBStore.MAX_DEADLOCK_RETRIES)
				{
					try{

						delete_txn = _environment.beginTransaction(parent_txn,DELETE_CFG);
						_dbh.delete(delete_txn, pkey);
						delete_txn.commitNoSync();
						//System.out.println("COMMITED DELETE");
						break;
					}catch(DeadlockException dle)
					{

						try{
							delete_txn.abort();
						}catch(DatabaseException dbe)
						{
							System.err.println("FAILED ABORTING TRANSACTION");
							dbe.printStackTrace();
						}
						retry_count++;
						System.out.println("DEADLOCKING ON PRIMARY INDEX DELETE RETRY COUNT:"+retry_count);
						if (retry_count >= BDBStore.MAX_DEADLOCK_RETRIES) 
						{

								//e.setId(-1);
								throw new DatabaseException("UNABLE TO DELETE RECORD DUE TO DEADLOCKING");
						}
					}
				}
			}

			return insertEntity(parent_txn,pkey, e);
	}
	
	protected DatabaseEntry insertEntity(Transaction parent_txn,DatabaseEntry pkey,Entity e) throws DatabaseException
	{

		Transaction txn = null;
		int retry_count = 0;	
		while (retry_count < BDBStore.MAX_DEADLOCK_RETRIES) 
		{
			try{
				txn = _environment.beginTransaction(parent_txn, null);		
				DatabaseEntry data = new DatabaseEntry();
				try{
					_binding.entityToEntry(e, data);
				}catch(DatabaseException pe)
				{
					try{
						txn.abort();
					}catch(DatabaseException dbe)
					{
						dbe.printStackTrace();
					}
					
					logger.error(pe);
					throw new DatabaseException("PROBLEM BINDING ENTITY");
				}
				_dbh.put(txn, pkey, data);
				txn.commitNoSync();
				//System.out.println(">>> LOW LEVEL SAVE OF ENTITY "+e);
				break;
			}
			catch (DeadlockException de) {
					txn.abort();
					retry_count++;
					logger.info("WRITE DEADLOCK OCCURRED retry count "+retry_count);
					if (retry_count >= BDBStore.MAX_DEADLOCK_RETRIES) {
						//e.setId(-1);
						throw new DatabaseException("108 SAVE FAILED FOR ENTITY DUE TO DEADLOCKING.RETRY WAS GREATER THAN MAX_NUMBER_RETRYS.");
					}
			}	 
		}//end while loop
		e.setId(LongBinding.entryToLong(pkey));
		return pkey;
	}
	
	protected DatabaseEntry deleteEntity(Transaction parent_txn,Entity e) throws PersistenceException
	{
		DatabaseEntry pkey 			= new DatabaseEntry();
		int retry_count = 0;
		long id = e.getId();
		/* this static stuff in store should be wrapped up in a BDBConfig object that gets set on setup */
		Transaction txn = null;
		while (retry_count < BDBStore.MAX_DEADLOCK_RETRIES) 
		{
			try {
			    txn = _environment.beginTransaction(parent_txn,TransactionConfig.DEFAULT);
				LongBinding.longToEntry(id,pkey);
				OperationStatus op_stat = _dbh.delete(txn, pkey);
				if(op_stat == OperationStatus.NOTFOUND)
					pkey = null;
				txn.commitNoSync();
				//System.out.println(">>> LOW LEVEL DELETE OF ENTITY "+e);
				return pkey;
			}catch (DeadlockException de) 
			{
			try {
				txn.abort();
				} catch (DatabaseException ae) {
					throw new PersistenceException("109 DATABASE EXCEPTION.UNABLE TO ABORT TRANSACTION");
				}
				retry_count++;
				logger.error("DELETE ENTITY WRITE DEADLOCK OCCURRED retry count "+retry_count);
				if (retry_count >= BDBStore.MAX_DEADLOCK_RETRIES) {
					throw new PersistenceException("108 DELETE FAILED FOR ENTITY DUE TO DEADLOCKING.RETRY WAS GREATER THAN MAX_NUMBER_RETRYS.");
				}

			} catch (DatabaseException dbe) {
	    	try {
				txn.abort();
			} catch (DatabaseException e1) {
				logger.error(e1);
			}
	        throw new PersistenceException("110 DELETE FAILED FOR ENTITY DATABASE EXCEPTION." + dbe.toString());
	    }
	}//end while loop
		
	logger.error("112 SHOULD NOT BE HERE IN SAVE ENTITY!!");
	//SHOULD NEVER GET HERE SINCE WE ALWAYS THROW AN EXCEPTION OR RETURN FROM WHILE LOOP!!!
	return null;
	}
	
	
	static TransactionConfig GET_BY_ID_CFG = new TransactionConfig();
	static {GET_BY_ID_CFG.setReadCommitted(true);}		
	protected Entity getById(Transaction parent_txn,long id) throws PersistenceException
	{
		int retry_count = 0;
		Entity e 		= null;
		while (retry_count < BDBStore.MAX_DEADLOCK_RETRIES)
		{
			Transaction txn = null;
			try
			{
				txn = _environment.beginTransaction(parent_txn, GET_BY_ID_CFG);

				// cache
				DatabaseEntry key 		= new DatabaseEntry();
				DatabaseEntry data 		= new DatabaseEntry();
				LongBinding.longToEntry(id,key);
				OperationStatus op_stat;
				if ((op_stat = _dbh.get(txn, key, data, LockMode.READ_COMMITTED)) == OperationStatus.SUCCESS)
				{
					//System.out.println("PIDX GET BY ID "+id+" WAS FOUND");
					e = _binding.getEntitySetId(_def, key, data);
					txn.commit();
					//TODO: in the future lets resolve entity definitions up here//
					//does an entity even need a pointer to its def in the first place or
					//is type enough???//
					return e;
				}
				else
				{
					System.out.println("!!!!!!!!!!!!!!!!!! "+id+" NOT FOUND IN PIDX "+getEntityDefinition().getName()+" OP STAT WAS "+op_stat);
					try{
						throw new Exception();
					}catch(Exception ee)
					{
						ee.printStackTrace();
						//System.exit(0);
					}
					txn.commit();
					return null;
				}

			}
			catch (DeadlockException de)
			{
				try{
					txn.abort();
				}catch(Exception ee)
				{
					ee.printStackTrace();
				}
				retry_count++;
				logger.info("READ DEADLOCK OCCURRED FOR " + _def.getName() + " " + id + " retry #:" + retry_count);
				if (retry_count >= BDBStore.MAX_DEADLOCK_RETRIES)
				{
					throw new PersistenceException("108 READ FAILED FOR " + _def.getName() + " " + id
							+ " DUE TO DEADLOCKING.RETRY WAS GREATER THAN MAX_NUMBER_RETRYS.");
				}
			}
			catch(DatabaseException dbe)
			{
				throw new PersistenceException("FAILED GET BY ID ON PIDX",dbe);
			}

		}// end while loop
		// SHOULD NEVER GET HERE SINCE WE ALWAYS THROW AN EXCEPTION OR RETURN
		// FROM WHILE LOOP!!!

		return null;
	}
	
	/* this is how the secondary indexes use us to get back an entity */
	public Entity getByPrimaryKey(Transaction txn,DatabaseEntry pkey) throws DatabaseException/* or persistence exception*/
	{
		int retry_count = 0;
		Entity e = null;
		while (retry_count < BDBStore.MAX_DEADLOCK_RETRIES)
		{
			try
			{
				DatabaseEntry data = new DatabaseEntry();
				if (_dbh.get(txn, pkey, data, LockMode.READ_UNCOMMITTED) == OperationStatus.SUCCESS)
				{
					//System.out.println("PKEY IS "+LongBinding.entryToLong(pkey));
					e = _binding.getEntitySetId(_def, pkey, data);
					return e;
				}
				else
				{
					return null;
				}
			}
			catch (DeadlockException de)
			{
				retry_count++;
				if (retry_count >= BDBStore.MAX_DEADLOCK_RETRIES)
				{
					throw new DatabaseException("108 READ FAILED FOR " + _def.getName()+ 
							 " DUE TO DEADLOCKING.RETRY WAS GREATER THAN MAX_NUMBER_RETRYS.");
				}
			}

		}// end while loop
		// SHOULD NEVER GET HERE SINCE WE ALWAYS THROW AN EXCEPTION OR RETURN
		// FROM WHILE LOOP!!!
		return null;
	}
	
	/* this is how iterators over primary indexes use us to get back an entity */
	public Entity getByRow(DatabaseEntry key,DatabaseEntry data) throws DatabaseException/* or persistence exception*/
	{
		return _binding.getEntitySetId(_def, key, data);
	}
	
	protected QueryResult getEntitiesOrderedById(String entity_type, int start, long number_of_records) throws PersistenceException
	{
		BDBQueryResult qr = new BDBQueryResult();
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry();
		Entity e = null;
		Cursor cursor = null;
		try
		{
			cursor = _dbh.openCursor(null, null);			
			
			/* revisit this stuff */
			for(int i = 0; i <= start;i++)
			{
				OperationStatus op_stat = cursor.getNext(key, data, LockMode.DEFAULT);					
				/** at the end of the table **/
				if(op_stat == OperationStatus.NOTFOUND)
					return qr;
			}
			do
			{
				e = _binding.getEntitySetId(_def, key, data);
				qr.add(e);
				if (qr.getEntities().size() == number_of_records)
				{
					//qr.setSearchToken(entity_type, "ID", e.getId(), 0, number_of_records, ascending, (EqualityCondition) null);
					break;
				}

			}while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS);
			cursor.close();
		}
		catch (DatabaseException de)
		{
			try{
				cursor.close();
			}catch(DatabaseException ee)
			{
				logger.error(ee);
				throw new PersistenceException("SERIOUS: FAILED ON CURSOR CLOSE.");
			}
			logger.error(de);
			throw new PersistenceException("get entities ordered by id failed " + de.getMessage());
		}
		return qr;
	}
	

	private static String get_primary_index_db_name(String entity_name)
	{
		return "PRIMARY_INDEX_"+entity_name+".db";
	}

	private static String get_sequence_db_name(String entity_name)
	{
		return "SEQUENCE_"+entity_name+".db";
	}

	private void open_sequence_db(String type) throws DatabaseException,FileNotFoundException
	{
		/*probably chnge this to recno.we know there is only one record in here*/
		DatabaseConfig cfg 		= get_primary_db_config_hash();
		String sequence_db_name = get_sequence_db_name(type);
		_sequence_db = _environment.openDatabase(null, get_sequence_db_name(type), null, cfg);
		logger.info("\tOPENING SEQUENCE DATABASE FOR "+type+"..."+sequence_db_name);
	}
	

	private void open_sequence() throws DatabaseException
	{
		// sequence //
		SequenceConfig sequence_config = new SequenceConfig();
		sequence_config.setAllowCreate(true);
		sequence_config.setInitialValue(1);
		sequence_config.setCacheSize(1024*1000);
		sequence_config.setAutoCommitNoSync(true);
		_sequence = _sequence_db.openSequence(null, _sequence_key, sequence_config);
	}
	
	protected void delete() throws DatabaseException
	{	
		try{
			close();
			_environment.removeDatabase(null, get_primary_index_db_name(_def.getName()),null);
			_environment.removeDatabase(null, get_sequence_db_name(_def.getName()),null);
		}catch(FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}
	
	public int truncate(Transaction txn,boolean count) throws DatabaseException
	{
		return getDbh().truncate(txn,count);
	}

	
	/*better for random access */
	private static DatabaseConfig get_primary_db_config_hash()
	{
		DatabaseConfig cfg = new DatabaseConfig();
		cfg.setType(DatabaseType.HASH);
		cfg.setAllowCreate(true);
		cfg.setTransactional(true);
		//cfg.setReadUncommitted(true);
		return cfg;
	}
	
	private DatabaseConfig get_primary_db_config_btree()
	{
		DatabaseConfig cfg = new DatabaseConfig();
		cfg.setType(DatabaseType.BTREE);
		cfg.setAllowCreate(true);
		cfg.setTransactional(true);
		//cfg.setReadUncommitted(true);
		return cfg;
	}	
	
	//this is the iterable interface so we can use the same iterators
	// on primary indexes as well
	public boolean isMultiFieldIndex()
	{
		return false;
	}
	
	public int getNumIndexedFields()
	{
		return 1;
	}


	public boolean isNormalIndex() {
		return true;
	}


	public boolean isSetIndex() {
		return false;
	}
	
	public boolean isFreeTextIndex()
	{
		return false;
	}
	

	public String getName()
	{
		return _def.getName();
	}


	public Database getReverseIndexDbh() {
		//this is useless on a primary index.
		//we use reverse indexes in STARTSWITHDATA.move(key,data)
		return null;
	}
}


