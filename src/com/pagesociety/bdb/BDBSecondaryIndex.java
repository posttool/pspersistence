package com.pagesociety.bdb;


import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.pagesociety.bdb.binding.EntityBinding;
import com.pagesociety.bdb.binding.FieldBinding;
import com.pagesociety.bdb.index.iterator.IterableIndex;
import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.EntityIndex;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.Cursor;
import com.sleepycat.db.CursorConfig;
import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseConfig;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DatabaseType;
import com.sleepycat.db.DeadlockException;
import com.sleepycat.db.Environment;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.Transaction;
import com.sleepycat.db.TransactionConfig;

public abstract class BDBSecondaryIndex implements IterableIndex
{
	private static final Logger logger = Logger.getLogger(BDBSecondaryIndex.class);
	
	protected BDBPrimaryIndex 			primary_index;
	protected Environment 				environment;	
	protected EntityIndex				index;
	
	/*default handles */
	protected Database 		  db_handle;
	protected Database 		  delete_handle;

	protected int type;
	public static final int TYPE_UNDEFINED 	  		= 0x00; 
	public static final int TYPE_NORMAL_INDEX    	= 0x01; 
	/* set indexes deconstruct arrays multiple rows in the index*/
	public static final int TYPE_SET_INDEX 			= 0x02; 
	
	public BDBSecondaryIndex()
	{
		this.index				= null;
		this.primary_index 		= null;
		this.type				= TYPE_UNDEFINED;
	}

	public int getType()
	{
		return type;
	}
	
	public boolean isNormalIndex()
	{
		return type == TYPE_NORMAL_INDEX;
	}

	public boolean isSetIndex()
	{
		return type == TYPE_SET_INDEX;
	}
	
	public String getName()
	{
		return index.getName();
	}
	
	/* this is used when you rename an index at runtime */
	public void setName(String name)
	{
		index.setName(name);
	}

	public EntityDefinition getEntityDefinition()
	{
		return primary_index.getEntityDefinition();
	}
	
	public BDBPrimaryIndex getPrimaryIndex()
	{
		return primary_index;
	}	

	public EntityIndex getEntityIndex()
	{
		return index;
	}
	
	public List<FieldDefinition> getFields()
	{
		return index.getFields();
	}
	
	public Map<String,String> getAttributes()
	{
		return index.getAttributes();
	}
	
	public Object getAttribute(String name)
	{
		return index.getAttributes().get(name);
	}
	
	public void setup(BDBPrimaryIndex primary_index,EntityIndex index) throws PersistenceException
	{
		try{
			this.index			= index;
			this.primary_index  = primary_index;
			this.environment 	= primary_index.getDbh().getEnvironment();
			/* set my own db handle */		
			db_handle 	   = openIndexDBHandle(environment,getDefaultIndexDbConfig(),getIndexDbName(getEntityDefinition().getName(),getName()));
			delete_handle  = openIndexDBHandle(environment,getDefaultDeleteDbConfig(),getDeleteIndexDbName(getEntityDefinition().getName(),getName()));
		
		}catch(Exception e)
		{
			e.printStackTrace();
			throw new PersistenceException("PROBLEM SETTING UP INDEX!!! "+getName());
		}
	}
	


	public void close() throws DatabaseException
	{
		logger.info("CLOSING SEC INDEX DB HANDLE FOR IDX "+getName()+" ON ENTITY "+getEntityDefinition().getName());
		db_handle.close();
		delete_handle.close();
	}


	public void delete() throws DatabaseException
	{	
		try{
			System.err.println("DELETEING SECONDARY INDEX DB "+getIndexDbName(getEntityDefinition().getName(),getName()));
			close();
			environment.removeDatabase(null, getIndexDbName(getEntityDefinition().getName(),getName()),null);
			environment.removeDatabase(null, getDeleteIndexDbName(getEntityDefinition().getName(),getName()),null);
		}catch(FileNotFoundException e)
		{
			logger.error(e);
		}
	}
	
	protected String getIndexDbName(String entity_name,String index_name)
	{
		int type = index.getEntityIndexType();
		return entity_name+"_"+EntityIndex.typeToString(type).toUpperCase()+"_"+index_name+BDBConstants.DB_SUFFIX;
	}
	
	protected String getDeleteIndexDbName(String entity_name,String index_name)
	{
		int type = index.getEntityIndexType();
		return entity_name+"_"+EntityIndex.typeToString(type).toUpperCase()+"_"+index_name+"_DELETE_MAP_"+BDBConstants.DB_SUFFIX;
	}

	public void insertIndexEntry(Transaction parent_txn,Entity e,DatabaseEntry data) throws DatabaseException
	{	
		Set<DatabaseEntry> keys = new HashSet<DatabaseEntry>();
		getInsertKeys(e,keys);

		Iterator<DatabaseEntry> iter = keys.iterator();
		while(iter.hasNext())
		{	
			DatabaseEntry key   = iter.next();
			Transaction txn 	= null;
			int retry_count = 0;
			
			//fail fast here for now....for some reason our access pattern causes this retry to be useless
			//without starting the whole transaction over again//
			//while (retry_count < BDBStore.MAX_DEADLOCK_RETRIES) 
			//{
			    try {
					txn = environment.beginTransaction(parent_txn, null);	
					db_handle.put(txn, key, data);
					delete_handle.put(txn,data,key);	
					txn.commitNoSync();
					/* dont delete. we break out upon success */
					//break;
			    } catch (DeadlockException de) {
			    	try{
			    		txn.abort();
			    	}catch(DatabaseException dbe)
			    	{
			    		dbe.printStackTrace();
			    		System.err.println("FAILED ABORTING TXN.");
			    	}
			    		retry_count++;
			    	System.err.println(Thread.currentThread().getId()+" SEC INDEX WRITE DEADLOCK OCCURRED retry_count "+retry_count+" FOR "+e);
			    	//if (retry_count >= BDBStore.MAX_DEADLOCK_RETRIES) 
		            //{
			    		throw new DatabaseException(e.getId()+":108 SAVE FAILED FOR SEC INDEX.RETRY WAS GREATER THAN MAX_NUMBER_RETRYS.");
		            //}
			    }
			//}//end while loop	
		}
	}
	
	
	public void deleteIndexEntry(Transaction parent_txn,DatabaseEntry pkey) throws DatabaseException
	{	
//		System.out.println(getName()+" DELETEING INDEX ENTRYS FOR PKEY "+FieldBinding.entryToValue(Types.TYPE_LONG,pkey));
		OperationStatus op_stat;
		DatabaseEntry data  = new DatabaseEntry();
		Cursor del_cursor 	= null;
		Cursor idx_cursor 	= null;
		Transaction txn 	= null; 
		
		int retry_count = 0;
		while (retry_count < BDBStore.MAX_DEADLOCK_RETRIES) 
		{
		    try {
				txn = environment.beginTransaction(parent_txn, null);	
				del_cursor 			    = delete_handle.openCursor(txn, null);
				op_stat 				= del_cursor.getSearchKey(pkey, data, LockMode.DEFAULT);
				if(op_stat == OperationStatus.NOTFOUND)
				{
					logger.error(getName()+" UNABLE TO DELETE!!!! pkey not found.PROBABLY RECORD WAS NEVER INSERTED. ARE YOU NOT DEALING WITH NULLS? "+LongBinding.entryToLong(pkey));
					del_cursor.close();
					txn.commitNoSync();
					try{
						System.err.println("SHOULD NOT BE HERE!!!");
						throw new Exception();
					}catch(Exception e)
					{
						e.printStackTrace();
					}
					return;
				}
				idx_cursor 			    = db_handle.openCursor(txn, null);				
				do{
					op_stat = idx_cursor.getSearchBoth(data, pkey, LockMode.DEFAULT);
					if(op_stat == OperationStatus.NOTFOUND)
					{
						idx_cursor.close();
						del_cursor.close();
						txn.commitNoSync();
						throw new DatabaseException("INDEX SEEMS CORRUPTED NO KEY/PKEY ENTRY FOR DEL INDEX");
					}
					op_stat = idx_cursor.delete();
//					System.out.println(">>>>>>>>>"+getName()+" LOW LEVEL DELETEING OF "+FieldBinding.entryToValue(Types.TYPE_LONG,pkey));
					op_stat = del_cursor.delete();					
				}while(del_cursor.getNextDup(pkey, data, LockMode.DEFAULT) == OperationStatus.SUCCESS);
			

				idx_cursor.close();
				del_cursor.close();
				txn.commitNoSync();
				/* we break on succes since we are in deadlock loop */
				break;
		    } catch (DeadlockException de) {
				idx_cursor.close();
				del_cursor.close();
		    	txn.abort();
		    	retry_count++;
	            logger.info(Thread.currentThread().getId()+" SEC INDEX DELETE DEADLOCK OCCURRED retry count "+retry_count);
	            if (retry_count >= BDBStore.MAX_DEADLOCK_RETRIES) 
	            {
	            	throw new DatabaseException("108 DELETE FAILED FOR SEC INDEX .RETRY WAS GREATER THAN MAX_NUMBER_RETRYS.");
	            }
			}

		}//end while loop	
	}
	
	public void truncate(Transaction txn) throws DatabaseException
	{
		db_handle.truncate(txn,false);
		delete_handle.truncate(txn,false);
	}

	public void primaryIndexNameChanged(String old_name,String new_name) 
	{
			
		/*recalculate name. this is assuming that the primary index name is correct
		 * before this call happens*/
		String name = getName();
		String old_db_name = getIndexDbName(old_name,name);
		String new_db_name = getIndexDbName(new_name,name);
		String old_delete_db_name = getDeleteIndexDbName(old_name,name);
		String new_delete_db_name = getDeleteIndexDbName(new_name,name);
		try {
			close();
			logger.info("RENAMING "+old_db_name+" to "+new_db_name);
			environment.renameDatabase(null,old_db_name,null,new_db_name);
			logger.info("RENAMING "+old_delete_db_name+" to "+new_delete_db_name);
			environment.renameDatabase(null,old_delete_db_name,null,new_delete_db_name);
			db_handle 		= openIndexDBHandle(environment,getDefaultIndexDbConfig(),new_db_name);		
			delete_handle 	= openIndexDBHandle(environment,getDefaultDeleteDbConfig(),new_delete_db_name);		
		} catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	
	public Database getDbh()
	{
		return db_handle;
	}
	
	public Database getReverseIndexDbh()
	{
		return delete_handle;
	}

	
	protected DatabaseConfig getDefaultIndexDbConfig()
	{  
		DatabaseConfig sec_cfg = new DatabaseConfig();
		sec_cfg.setErrorStream(System.err);
		sec_cfg.setErrorPrefix("DB FOR INDEX "+getName());
		sec_cfg.setType(DatabaseType.BTREE);
		sec_cfg.setAllowCreate(true);
		sec_cfg.setSortedDuplicates(true);
		sec_cfg.setTransactional(true);
		return sec_cfg;
	}
	
	protected DatabaseConfig getDefaultDeleteDbConfig()
	{  
		DatabaseConfig sec_cfg = new DatabaseConfig();
		sec_cfg.setErrorStream(System.err);
		sec_cfg.setErrorPrefix("DB FOR INDEX DELETE "+getName());
		sec_cfg.setType(DatabaseType.BTREE);
		sec_cfg.setAllowCreate(true);
		sec_cfg.setSortedDuplicates(true);
		sec_cfg.setTransactional(true);
		return sec_cfg;
	}
	
	
	/* static utility for sub classes opening their handles up*/
	protected static Database openIndexDBHandle(Environment environment,DatabaseConfig cfg,String dbname) throws PersistenceException
	{
		Database handle = null;
		try
		{		
			handle = environment.openDatabase(null, dbname,null,cfg);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new PersistenceException("UNABLE TO OPEN INDEX DB "+dbname);
		}		
		return handle;
	}

	/* utility function for subclasses */
	protected static Object get_required_query_attribute(Map<String,Object> query_attributes,String attrname) throws PersistenceException
	{
		Object val = query_attributes.get(attrname);
		if(val == null)
			throw new PersistenceException("MISSING REQUIRED QUERY PARAM: "+attrname);

		return val;
	}

	//TODO: this can be set above when constructor is called
	//as an optimization
	public boolean isMultiFieldIndex()
	{
		return !(index.getFields().size() == 1);
	}
	
	public int getNumIndexedFields()
	{
		return index.getFields().size();
	}
	
	public abstract List<Object> getDistinctKeys() throws PersistenceException;
	public abstract void getInsertKeys(Entity entity,Set<DatabaseEntry> result) throws DatabaseException;
	
	public abstract boolean indexesField(String fieldname);
	public abstract boolean invalidatedByFieldDelete(FieldDefinition f);
	public abstract void    fieldChangedName(String old_name,String new_name);


/* called when the index is being constructed so that the index has an opportunity to
 * say it only accepts a certain kind of field. if the fields fail vlidation the index
 * should throw a perstence exception with an appropriate message. 
 */
	public void validateFields(List<FieldDefinition> fields) throws PersistenceException
	{
		return;	
	}
	
	
}
