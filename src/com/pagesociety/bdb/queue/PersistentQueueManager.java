package com.pagesociety.bdb.queue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.pagesociety.bdb.BDBConstants;
import com.pagesociety.bdb.BDBStore;
import com.pagesociety.persistence.PersistenceException;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.db.Cursor;
import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseConfig;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DatabaseType;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.Transaction;
import com.sleepycat.db.TransactionConfig;

public class PersistentQueueManager 
{
	
	private BDBStore context;
	private TransactionConfig queue_transaction_config;
	private TransactionConfig queue_meta_data_transaction_config;

	private Map<String,PersistentQueue> queue_map;
	private List<String> queue_list;

	private Database queue_manager_meta_db;
	private static final String QUEUE_META_DATA_DB_NAME = "QUEUE_META_INFO.db";

	private static final Logger logger = Logger.getLogger(PersistentQueueManager.class);
	
	
	public void init(BDBStore context,Map<String,Object> config) throws PersistenceException
	{
		this.context  = context;
		this.queue_transaction_config 			   = new TransactionConfig();
		this.queue_meta_data_transaction_config    = new TransactionConfig();
		this.queue_map = new HashMap<String, PersistentQueue>();
		this.queue_list = new ArrayList<String>();
		init_queue_meta_db(config);
		bootstrap_existing_queues();
	}
	
	private void init_queue_meta_db(Map<String,Object> config) throws PersistenceException
	{

		DatabaseConfig cfg = context.getDefaultBTreeConfig();
		try{
			queue_manager_meta_db = context.getEnvironment().openDatabase(null, QUEUE_META_DATA_DB_NAME, null, cfg);
		}catch (Exception e){	

			logger.error("init_queue_meta_data_db(Map<String,Object>)", e);
			throw new PersistenceException("UNABLE TO OPEN ENTITY DEFINITION DB "+BDBConstants.ENTITY_DEFINITION_DB_NAME);
		}		

		logger.debug("init_entity_definition_db(HashMap<Object,Object>) - OPENED ENTITY DEFINITION DATABASE ");
			
	}
	
	private void bootstrap_existing_queues() throws PersistenceException
	{

		List<PersistentQueue> queue_meta_data_items = get_existing_queues_from_db();
		for (int i = 0; i < queue_meta_data_items.size(); i++)
		{
			PersistentQueue q = queue_meta_data_items.get(i);
			setup_queue(q);
		}
	}
		
	private void setup_queue(PersistentQueue q) throws PersistenceException
	{
		String queue_name    = q.getName();
		String queue_db_name = get_queue_db_name(queue_name);
		DatabaseConfig cfg 	 = get_queue_config(q.getRecordSize(),q.getRecordsPerExtent());
		Database queue_db;
		try{
			queue_db = context.getEnvironment().openDatabase(null, queue_db_name, null, cfg);
		}catch (Exception e)
		{	
			throw new PersistenceException("UNABLE TO OPEN QUEUE DB "+queue_db_name);
		}
		q.setDbh(queue_db);
		logger.info("OPENED QUEUE DB "+queue_db_name);
		queue_map.put(queue_name, q);
		queue_list.add(queue_name);
	}
	
	//queue extent size //
	//http://www.oracle.com/technology/documentation/berkeley-db/xml/ref/am_conf/extentsize.html
	public String createQueue(String name,int record_size,int num_records_in_extent) throws PersistenceException
	{
		if(queue_map.get(name) != null)
			throw new PersistenceException("QUEUE NAMED "+name+" ALREADY EXISTS");
		
		PersistentQueue q = new PersistentQueue(name,record_size,num_records_in_extent);
		add_queue_to_db(q);
		setup_queue(q);
		return name;
	}

	public void deleteQueue(String name) throws PersistenceException
	{
		PersistentQueue q = queue_map.get(name);
		if(q == null)
			throw new PersistenceException("deleteQueue: QUEUE NAMED "+name+" DOES NOT EXIST");
		try
		{
			delete_queue_from_db(q);
			q.getDbh().close();
			context.getEnvironment().removeDatabase(null, get_queue_db_name(name),null);
		}catch(Exception dbe)
		{
			logger.error("FAILED DELETING QUEUE DB "+name);
			throw new PersistenceException("FAILED DELETING QUEUE DB "+get_queue_db_name(name)+" SEE LOGS.");
		}
		queue_map.remove(name);
		queue_list.remove(name);
	}
	
	private void add_queue_to_db(PersistentQueue q) throws PersistenceException
	{
		Transaction txn = null;
		try{
			txn = context.getEnvironment().beginTransaction(null, queue_meta_data_transaction_config);
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();	
			StringBinding.stringToEntry(q.getName(), key);
			q.encode(data);
			queue_manager_meta_db.put(txn, key, data);
			txn.commit();
		}catch(DatabaseException dbe)
		{
			try {
				txn.abort();
			} catch (DatabaseException e) {
				e.printStackTrace();
				logger.error(e);
			}
			logger.error("ADDING QUEUE TO META INFO DB FAILED",dbe);
			throw new PersistenceException("FAILED ADDING QUEUE TO META INFO DB");
		}
	}
	
	
	private void delete_queue_from_db(PersistentQueue q) throws PersistenceException
	{
		Transaction txn= null;
		try{
			txn = context.getEnvironment().beginTransaction(null, queue_meta_data_transaction_config);
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();	
			StringBinding.stringToEntry(q.getName(), key);
			queue_manager_meta_db.delete(txn, key);
			txn.commit();
		}catch(DatabaseException dbe)
		{
			try {
				txn.abort();
			} catch (DatabaseException e) {
				e.printStackTrace();
				logger.error(e);
			}
			logger.error("ADDING QUEUE TO META INFO DB FAILED",dbe);
			throw new PersistenceException("FAILED ADDING QUEUE TO META INFO DB");
		}
		
	}
	
	//commit flag means whether or not to commit
	public DatabaseEntry enqueue(Transaction parent_txn,String queue_name,byte[] queue_item,boolean durable_commit) throws PersistenceException
	{
		PersistentQueue q = queue_map.get(queue_name);
		if(q == null)
			throw new PersistenceException("enqueue: QUEUE NAMED "+queue_name+" DOES NOT EXIST");
		if(queue_item.length > q.getRecordSize())
			throw new PersistenceException("QUEUE ITEM IS GRETER THAN QUEUE RECORD SIZE. ITEM LENGTH IS "+queue_item.length+" & QUEUE RECORD SIZE IS "+q.getRecordSize());
		Transaction txn = null;
		int retry_count = 0;
		while(retry_count < BDBStore.MAX_DEADLOCK_RETRIES)
		{
			try{

				txn = context.getEnvironment().beginTransaction(parent_txn, queue_transaction_config);
				DatabaseEntry key 	= new DatabaseEntry();
				DatabaseEntry data	= new DatabaseEntry(queue_item);
				q.getDbh().append(txn, key, data);
				if(durable_commit)
					txn.commit();
				else
					txn.commitNoSync();
				return key;
				
			}catch(DatabaseException dle)
			{
				try{
					txn.abort();
				}catch(DatabaseException dbe)
				{					
					logger.error("FAILED ABORTING TRANSACTION",dbe);
					throw new PersistenceException("FAILED ABORTING TRANSACTION");
				}
				retry_count++;
				logger.error("DEADLOCKING ON QUEUE APPEND RETRY COUNT:"+retry_count);
				if (retry_count >= BDBStore.MAX_DEADLOCK_RETRIES) 
				{
						throw new PersistenceException("UNABLE TO INSERT QUEUE ITEM DUE TO DEADLOCKING");
				}
			}		
		}
		return null;//should never get here//	
	}
	
	//returns null if blocking is false and there is nothing on the queue
	//commit flag is whether or not we commit the transaction to disk
	public byte[] dequeue(Transaction parent_txn,String queue_name,boolean durable_commit,boolean block) throws PersistenceException
	{
		PersistentQueue q = queue_map.get(queue_name);
		if(q == null)
			throw new PersistenceException("dequeue: QUEUE NAMED "+queue_name+" DOES NOT EXIST");
		Transaction txn = null;
		int retry_count = 0;
		while(retry_count < BDBStore.MAX_DEADLOCK_RETRIES)
		{
			try{

				txn = context.getEnvironment().beginTransaction(parent_txn, queue_transaction_config);
				DatabaseEntry key 	= new DatabaseEntry();
				DatabaseEntry data	= new DatabaseEntry();
				OperationStatus op_stat = q.getDbh().consume(txn, key, data,block);
				if(durable_commit)
					txn.commit();
				else
					txn.commitNoSync();
				if(op_stat == OperationStatus.NOTFOUND)
					return null;
				return fix_data(data.getData());
				
			}catch(DatabaseException dle)
			{
				try{
					txn.abort();
				}catch(DatabaseException dbe)
				{					
					logger.error("FAILED ABORTING TRANSACTION",dbe);
					throw new PersistenceException("FAILED ABORTING TRANSACTION");
				}
				retry_count++;
				logger.error("DEADLOCKING ON QUEUE CONSUME RETRY COUNT:"+retry_count);
				if (retry_count >= BDBStore.MAX_DEADLOCK_RETRIES) 
				{
						throw new PersistenceException("UNABLE TO CONSUME QUEUE ITEM DUE TO DEADLOCKING");
				}
			}		
		}
		return null;//should never get here//	
	}

	// TODO bug in bdb java api
	// cfg.setRecordPadding(0) dont work!
	private byte[] fix_data(byte[] data)
	{
		int i = data.length-1;
		while (i!=-1)
		{
			if (data[i]!=32)
				break;
			i--;
		}
		byte[] b = new byte[i+1];
		System.arraycopy(data, 0, b, 0, i+1);
		return b;
	}

	public List<String> listQueues() throws PersistenceException
	{
		return queue_list;
	}

	
	public void shutdown() throws PersistenceException
	{
		System.out.println("SHUTTING DOWN QUEUE SUBSYSTEM");
		try{
			queue_manager_meta_db.close();
		}catch(DatabaseException dbe)
		{
			dbe.printStackTrace();
		}
		System.out.println("CLOSED QUEUE SYBSYSTEM META DB");
			
			for(int i = 0;i < queue_list.size();i++)
		{
			String q_name = queue_list.get(i);
			String q_db_name = get_queue_db_name(q_name);
			try{
				PersistentQueue q = queue_map.get(q_name);
				Database dbh = q.getDbh();
				dbh.close();
			}catch(DatabaseException dbe)
			{
				dbe.printStackTrace();
			}
			System.out.println("CLOSED "+q_db_name);
		}
		System.out.println("QUEUE SUBSYSTEM SHUTDOWN OK");
	}
	
	private DatabaseConfig get_queue_config(int record_size, int records_per_extent) 
	{
		DatabaseConfig cfg = new DatabaseConfig();
		cfg.setType(DatabaseType.QUEUE);
		cfg.setAllowCreate(true);
		cfg.setTransactional(true);
		cfg.setReadUncommitted(true);
		cfg.setRecordLength(record_size);
		cfg.setQueueExtentSize(record_size*records_per_extent);
		cfg.setRecordPad(0x00);
		return cfg;

	}
	
	private String get_queue_db_name(String queue_name) 
	{
		return "QUEUE_"+queue_name+".db";
	}

	private List<PersistentQueue> get_existing_queues_from_db() throws PersistenceException
	{
		List<PersistentQueue> meta_data_items = new ArrayList<PersistentQueue>();
		Cursor cursor = null;
		try
		{
			cursor 				= queue_manager_meta_db.openCursor(null, null);
			DatabaseEntry key 	= new DatabaseEntry();
			DatabaseEntry data 	= new DatabaseEntry();
			while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
			{
				PersistentQueue qi = new PersistentQueue();
				qi.decode(data);
				meta_data_items.add(qi);
			}
			cursor.close();
			return meta_data_items;
		}
		catch (Exception de)
		{
			logger.error("get_existing_queues_from_db()", de);			
			try {
				cursor.close();
			} catch (DatabaseException e) {
				e.printStackTrace();
			}
			throw new PersistenceException("ERROR READING QUEUE META INFO.");
		}
	}

/////////////BEGIN PERSISTENT QUEUE///	
	class PersistentQueue
	{
		private String name;
		private int rec_size;
		private int recs_per_extent;
		private Database dbh;
		
		public PersistentQueue(String name,int rec_size,int recs_per_extent)
		{
			this.name 			 = name;
			this.rec_size 		 = rec_size;
			this.recs_per_extent = recs_per_extent;
			this.dbh			 = null;
		}
		
		public PersistentQueue() 
		{

		}

		public void setName(String name)
		{
			this.name = name;
		}
		
		public String getName()
		{
			return name;
		}
		
		public void setRecordSize(int record_size)
		{
			this.rec_size = record_size;
		}
		
		public int getRecordSize()
		{
			return rec_size;
		}
		
		public void setRecordsPerExtent(int records_per_extent)
		{
			this.recs_per_extent = records_per_extent;
		}
		
		public int getRecordsPerExtent()
		{
			return recs_per_extent;
		}
		
		public void setDbh(Database dbh)
		{
			this.dbh = dbh;
		}
		
		public Database getDbh()
		{
			return dbh;
		}
		
		public void encode(DatabaseEntry d)
		{
			if(d.getData() == null)
				d.setData(new byte[256]);
			
			TupleOutput to = new TupleOutput(d.getData());
			to.writeString(getName());
			to.writeInt(getRecordSize());
			to.writeInt(getRecordsPerExtent());
		}
		
		public void decode(DatabaseEntry data)
		{
			 TupleInput ti =  new TupleInput(data.getData(), data.getOffset(),data.getSize());
			 setName(ti.readString());
			 setRecordSize(ti.readInt());
			 setRecordsPerExtent(ti.readInt());
		}
	}
	
}
