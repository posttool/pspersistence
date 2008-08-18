package com.pagesociety.bdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


import org.apache.log4j.Logger;

import com.pagesociety.bdb.binding.EntityBinding;
import com.pagesociety.bdb.binding.EntityDefinitionBinding;
import com.pagesociety.bdb.binding.EntityRelationshipBinding;
import com.pagesociety.bdb.binding.EntitySecondaryIndexBinding;
import com.pagesociety.bdb.binding.FieldBinding;
import com.pagesociety.bdb.index.EntityIndexDefinition;
import com.pagesociety.bdb.index.query.QueryManager;
import com.pagesociety.bdb.index.query.QueryManagerConfig;
import com.pagesociety.bdb.locker.AdminLocker;
import com.pagesociety.bdb.locker.Locker;
import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.EntityIndex;
import com.pagesociety.persistence.EntityRelationshipDefinition;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.pagesociety.persistence.PersistentStore;
import com.pagesociety.persistence.Query;
import com.pagesociety.persistence.QueryResult;
import com.pagesociety.persistence.Types;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.collections.PrimaryKeyAssigner;
import com.sleepycat.db.Cursor;
import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseConfig;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DatabaseType;
import com.sleepycat.db.DeadlockException;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.db.LockDetectMode;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.Transaction;
import com.sleepycat.db.TransactionConfig;

public class BDBStore implements PersistentStore
{
	private static final Logger logger = Logger.getLogger(BDBStore.class);
	//environment
	private Environment 				environment;
	// entity def db and binding
	private Database 					entity_def_db;
	private Database 					entity_index_db;
	private Database 					entity_relationship_db; 
	private EntityBinding 				entity_binding;
	private EntityDefinitionBinding 	entity_def_binding;
	private EntitySecondaryIndexBinding entity_index_binding;
	private EntityRelationshipBinding 	entity_relationship_binding;

	// modules
	protected BDBEntityIndexDefinitionManager _entity_index_definition_manager;
	
	/*deadlocking stuff */
	public static final int MAX_DEADLOCK_RETRIES 		= 10;
	private volatile boolean _deadlock_monitor_running  = false;
	//private int _deadlocking_scheme;
	Thread _deadlock_monitor;
	private static final int DEFAULT_MONITOR_INTERVAL_FOR_MONITORING_SCHEME = 3000;
	

	// primary and secondary databases handles
	private Map<String,BDBPrimaryIndex> 		  			entity_primary_indexes_as_map;
	private List<BDBPrimaryIndex> 		  					entity_primary_indexes_as_list;
	private Map<String, Map<String, BDBSecondaryIndex>> 	entity_secondary_indexes_as_map;
	private Map<String, List<BDBSecondaryIndex>> 		  	entity_secondary_indexes_as_list;

	private Map<String, Map<String,EntityRelationshipDefinition>> entity_relationship_map;


	/* Database operations, used by resolve relationship */

	public static final int INSERT = 0;
	public static final int UPDATE = 1;
	public static final int DELETE = 2;

	private Locker _store_locker = null;
	private CheckpointPolicy checkpoint_policy;
	private Properties _db_env_props;
	private File _db_env_props_file;
	private HashMap<Object, Object> _config;
	
	/* BEGIN INTERFACE ***************************************************************************/
	public void init(HashMap<Object, Object> config) throws PersistenceException
	{
		_config = config;
		
		entity_primary_indexes_as_map 	 		 = new HashMap<String, BDBPrimaryIndex>();
		entity_primary_indexes_as_list 	 		 = new ArrayList<BDBPrimaryIndex>();
		entity_secondary_indexes_as_map 		 = new HashMap<String, Map<String, BDBSecondaryIndex>>();
		entity_secondary_indexes_as_list 		 = new HashMap<String,List<BDBSecondaryIndex>>();
		entity_relationship_map 		 	 	 = new HashMap<String, Map<String,EntityRelationshipDefinition>>();
		entity_binding					 		 = new EntityBinding();
		
		/* order is important */
		init_shutdown_hook();
		init_locker(config);
		init_checkpoint_policy(config);
		init_environment(config);
		init_entity_definition_db(config);
		init_entity_secondary_index_db(config);
		init_entity_relationship_db(config);
		init_entity_index_definition_manager();
		init_deadlock_resolution_scheme(config);
		init_query_manager();
		
		bootstrap_existing_entity_definitions();
		bootstrap_existing_indices();
		bootstrap_existing_entity_relationships();
		
		init_field_binding();		
		
		
		logger.debug("Init - Complete");
		
	}
	
	private void init_shutdown_hook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread()
		{
			public void run()
			{
				try{
					close();
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
	}
	
	private void init_locker(HashMap<Object, Object> config)
	{
		try {
			_store_locker = (Locker)Class.forName((String)config.get(BDBStoreConfigKeyValues.KEY_STORE_LOCKER_CLASS)).newInstance();
		} catch (Exception e) {
			//e.printStackTrace();
			_store_locker = new AdminLocker();
		}
		logger.debug("init_store_locker(HashMap<Object,Object>) - SETTING STORE LOCKER TO INSTANCE OF " + _store_locker.getClass().getName());
		_store_locker.init(config);
	}
	
	private void init_checkpoint_policy(HashMap<Object,Object> config)
	{
		try
		{
			checkpoint_policy = (CheckpointPolicy) Class.forName((String) config.get(BDBStoreConfigKeyValues.KEY_STORE_CHECKPOINT_POLICY_CLASS)).newInstance();
		}
		catch (Exception e)
		{
			checkpoint_policy = new DefaultCheckpointPolicy();
		}

		logger.debug("init_checkpoint_policy(HashMap<Object,Object>) - SETTING CHECKPOINT POLICY TO INSTANCE OF " + checkpoint_policy.getClass().getName());
		checkpoint_policy.init(config);
	}
	

	private void init_entity_index_definition_manager() throws PersistenceException
	{
		_entity_index_definition_manager = new BDBEntityIndexDefinitionManager();			
		_entity_index_definition_manager.loadDefinitions();
	}
	
	public void addEntityDefinition(EntityDefinition entity_def) throws PersistenceException
	{
		_store_locker.enterLockerThread();
		try{
			logger.debug("addEntityDefinition(EntityDefinition) - ADDING ENTITY DEF " + entity_def.getName());
			if(entity_primary_indexes_as_map.get(entity_def.getName()) != null)
				throw new PersistenceException("Entity Definition already exists for: "+entity_def.getName()+".Delete existing entity definition first.");			
			add_entity_definition_to_db(entity_def);
			BDBPrimaryIndex pidx = new BDBPrimaryIndex();
			pidx.setup(environment, entity_def);
			
			/*do runtime cacheing*/
			entity_primary_indexes_as_map.put(entity_def.getName(), pidx);
			entity_primary_indexes_as_list.add(pidx);
			
			/* initialize maps for holding indexes bound to entity*/
			List<BDBSecondaryIndex> sec_indexes_list 	  = new ArrayList<BDBSecondaryIndex>();
			Map<String,BDBSecondaryIndex> sec_indexes_map = new HashMap<String,BDBSecondaryIndex>();
			entity_secondary_indexes_as_list.put(entity_def.getName(),sec_indexes_list);
			entity_secondary_indexes_as_map.put(entity_def.getName(),sec_indexes_map);
			calculate_query_cache_dependencies(entity_def);

			//TODO talk to registry instead of directly to field binding
			// or resolve definitions here...
			FieldBinding.addToPrimaryIndexMap(entity_def.getName(),pidx);
			
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitLockerThread();	
		}
	}

	/* this just does the insert on the entity def table. the rest of the initialization of other
	 * entity handles etc happens in init_entity_db. see above.
	 */
	private void add_entity_definition_to_db(EntityDefinition entity_def) throws PersistenceException
	{
		String ename 	  = entity_def.getName();	
	
		Transaction txn = null;		
		OperationStatus op_status = null;
		try
		{			
			txn = environment.beginTransaction(null, null);
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();

			FieldBinding.valueToEntry(Types.TYPE_STRING, ename, key);
			entity_def_binding.objectToEntry(entity_def, data);		
			op_status = entity_def_db.put(txn, key, data);
			
			if(op_status != OperationStatus.SUCCESS)
			{
				abortTxn(txn);
				throw new PersistenceException("Failed Saving Entity Definition: "+ename);
			}
			txn.commit();
		}
		catch (Exception e)
		{
			abortTxn(txn);
			logger.error("add_entity_definition_to_db(EntityDefinition)", e);
			throw new PersistenceException("Unable to add entity definition " + entity_def + " "
					+ e.getMessage());
		}
	}
	
	public void deleteEntityDefinition(String entity_def_name) throws PersistenceException
	{
		_store_locker.enterLockerThread();
		try{
			do_delete_entity_definition(entity_def_name);
		
		}catch(PersistenceException p)
		{
			throw p;
		}
		finally
		{
			_store_locker.exitLockerThread();	
		}
	}
	
	private void do_delete_entity_definition(String name) throws PersistenceException
	{
		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(name);
		if(pidx == null)
			throw new PersistenceException("DELETE ENTITY DEF: no entity definition for "+name);
		
		for(BDBPrimaryIndex p:entity_primary_indexes_as_list)
		{
			EntityDefinition d = p.getEntityDefinition();
			if(d.getName().equals(name))
				continue;
			for(FieldDefinition f:d.getFields())
			{
				if(f.getBaseType() == Types.TYPE_REFERENCE)
				{
					if(f.getReferenceType().equals(name))
						throw new PersistenceException("ENTITY FIELD "+f.getName()+" IN "+d.getName()+
								" IS A REFERENCE TO TYPE "+name+". REMOVE THIS FIELD IN ORDER TO DELETE "+name);
				}
			}
		}
		
		EntityDefinition def = pidx.getEntityDefinition();
		String ename = def.getName();
		try{
			logger.debug("do_delete_entity_definition(String) - DELETEING ENTITY DEF " + ename);
			delete_entity_definition_from_db(ename);
			entity_primary_indexes_as_map.remove(ename);
			entity_primary_indexes_as_list.remove(pidx);
			remove_query_cache_dependencies(def);
			pidx.delete();
			
			/* right now entityindexes are always attached to primaryindexes so we
			 * blast all of them away too
			 */
			List<BDBSecondaryIndex> sec_indexes = entity_secondary_indexes_as_list.get(ename);
			for(int i = 0; i < sec_indexes.size();i++)
			{
				BDBSecondaryIndex s_idx = sec_indexes.get(i);
				s_idx.delete();
				/*remove it from the entity_index table */
				delete_entity_index_from_db(ename, s_idx.getEntityIndex());
			}
			entity_secondary_indexes_as_list.remove(ename);
			entity_secondary_indexes_as_map.remove(ename);
		}catch(DatabaseException de)
		{
			logger.error("do_delete_entity_definition(String)", de);
			throw new PersistenceException("UNABLE TO DELETE ENTITY DEF FOR "+ename+".INTERNAL ERROR. SEE LOGS");
		}
	}
	
	public void renameEntityDefinition(String ename,String new_name) throws PersistenceException
	{	
		_store_locker.enterLockerThread();
		try{
			do_rename_entity_definition(ename, new_name);
		}
		finally
		{	
			_store_locker.exitLockerThread();	
		}
	}

	private void do_rename_entity_definition(String ename,String new_name) throws PersistenceException
	{
		
		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(ename);
		if(pidx == null)
			throw new PersistenceException("NO ENTITY DEF FOR "+ename);
		BDBPrimaryIndex pidx2 = entity_primary_indexes_as_map.get(new_name);
		if(pidx2 != null)
			throw new PersistenceException("CANT RENAME TO  "+new_name+". ENTITY "+new_name+" ALREADY EXISTS");

		
		
		EntityDefinition olddef = pidx.getEntityDefinition();
		EntityDefinition newdef = olddef.clone();
		newdef.setName(new_name);
		try{
			/* this will move the indexes too */
			redefine_entity_definition(olddef,newdef);	
		}catch(PersistenceException pe)
		{
			logger.error("do_rename_entity_definition(String, String)", pe);
			throw new PersistenceException("UNABLE TO RENAME ENTITY DEF FOR "+ename+".INTERNAL ERROR. SEE LOGS");
		}
	
	}
		
	private void redefine_entity_definition(EntityDefinition olddef,EntityDefinition redef) throws PersistenceException
	{
		logger.debug("redefine_entity_definition(EntityDefinition, EntityDefinition) - tREDEFINITING ENTITY DEF FOR " + olddef.getName() + " " + redef.getName());
		BDBPrimaryIndex pidx = null;
		String old_ename = olddef.getName();
		String new_ename = redef.getName();
		
		/*update entity def in db */
		delete_entity_definition_from_db(old_ename);
		/* TODO: this is due to the way we are looking up entity defs */
		/* we look them up through the primary index map. if we */
		/* dont remove the key it wont lets us add it because it
		 * will think that it exists.*/
		
		pidx = entity_primary_indexes_as_map.remove(old_ename);
		add_entity_definition_to_db(redef);
		entity_primary_indexes_as_map.put(new_ename,pidx);

		//this method is called by addfield and rename field and delete field//
		//so this should be fine //
		calculate_query_cache_dependencies(olddef);
		remove_query_cache_dependencies(redef);
		
		if(!olddef.getName().equals(redef.getName()))
		{		
			pidx.renameDb(olddef, redef);
			/*attach the indexes to the new entity name*/
			move_entity_indexes(olddef, redef);
			
			/*update the other entity definitions reftypes to the new type*/
			for(BDBPrimaryIndex p:entity_primary_indexes_as_list)
			{
				EntityDefinition d = p.getEntityDefinition();
				if(d.getName().equals(old_ename))
					continue;
				for(FieldDefinition f:d.getFields())
				{
					if(f.getBaseType() == Types.TYPE_REFERENCE)
					{
						if(f.getReferenceType().equals(old_ename))
						{
							f.setReferenceType(new_ename);
							redefine_entity_definition(d, d);//make sure the ref type is saved in the new def too//
						}
					}
				}
			}

		}
		
		pidx.setEntityDefinition(redef);		
	}
	
	/*this just deletes the entity definition in the entity def table. not exposed publicly.*/	
	protected void delete_entity_definition_from_db(String ename) throws PersistenceException
	{
		if(entity_primary_indexes_as_map.get(ename) == null)
		{
			logger.error("delete_entity_definition_from_db(String) - DELETING ENTITY WHICH DOESNT EXIST!!!", null);
			throw new PersistenceException("Entity Definition does not exist for: "+ename+".Delete failed.");
		}
		Transaction txn = null;		
		OperationStatus op_status = null;
		try
		{			
			txn = environment.beginTransaction(null, null);
			DatabaseEntry key = new DatabaseEntry();
			FieldBinding.valueToEntry(Types.TYPE_STRING, ename, key);

			op_status = entity_def_db.delete(txn, key);
			if(op_status != OperationStatus.SUCCESS)
			{
				abortTxn(txn);
				throw new PersistenceException("Failed Deleting Entity Definition: "+ename);
			}
			txn.commit();
		}
		catch (Exception e)
		{
			abortTxn(txn);
			logger.error("delete_entity_definition_from_db(String)", e);
			throw new PersistenceException("Unable to delete definition " + ename + " "
					+ e.getMessage());
		}
	}
	
	
	public Entity saveEntity(Entity e) throws PersistenceException
	{	

		_store_locker.enterAppThread();
		String entity_type 		  = e.getType();
		if(!e.isDirty())
		{
			try{
				_store_locker.exitAppThread();
				throw new Exception("WARNING: SAVING UNDIRTY ENTITY");
			}catch(Exception ee)
			{
				logger.error("saveEntity(Entity)", ee);
			}
			return e;
		}
		BDBPrimaryIndex pi = entity_primary_indexes_as_map.get(entity_type);
		if(pi == null)
		{
			_store_locker.exitAppThread();
			throw new PersistenceException("ENTITY OF TYPE "+entity_type+" DOES NOT EXIST");
		}
		
		boolean update 		= true;
		DatabaseEntry pkey 	= null;
		int retry_count = 0;
		Transaction txn = null;
		if(e.getId() == Entity.UNDEFINED)
			update = false;
		try{
			
				while(true)
				{
					try{
					txn = environment.beginTransaction(null, null);			
			/* resolve side effects first so we still have handle to old value */
			if (update)
			{
				resolve_relationship_sidefx(txn,e, UPDATE);
				pkey = pi.saveEntity(txn,e);
			} 
			else 
			{
				pkey = pi.saveEntity(txn,e);
				resolve_relationship_sidefx(txn,e,INSERT);
			}
			save_to_secondary_indexes(txn, pkey, e, update);						
			e.undirty();
			txn.commitNoSync();
			break;
				}catch(DatabaseException dbe)
				{
					abortTxn(txn);
					retry_count++;
					if(retry_count >= BDBStore.MAX_DEADLOCK_RETRIES)
					{
						System.err.println(Thread.currentThread().getId()+" FAILING HORRIBLY HERE!!!!!!!!!");
						throw dbe;
					}
				}
				}//end while

			if (checkpoint_policy.isCheckpointNecessary())
				do_checkpoint();
			
			//we might want a more elaborate policy here//
			clean_query_cache(entity_type);
		
		}
		catch(DatabaseException pe)
		{
			//abortTxn(txn);
			pe.printStackTrace();
			throw new PersistenceException("SAVE OF ENTITY FAILED. TRY AGAIN.\n"+e);
		}
		finally
		{
			_store_locker.exitAppThread();
		}	
		return e;
	}
	
	private void save_to_secondary_indexes(Transaction parent_txn,DatabaseEntry pkey,Entity e,boolean update) throws DatabaseException
	{
		List<BDBSecondaryIndex>sec_indexes = entity_secondary_indexes_as_list.get(e.getType());
		List<String> dirty_fields;
		if(update)
			dirty_fields = e.getDirtyAttributes();
		else
		{
			//TODO: i dont like the fact that getFieldNames calls keySet();
			dirty_fields = e.getEntityDefinition().getFieldNames();
		}
		
		int ss 					  = dirty_fields.size();
		int s 					  = sec_indexes.size();
		BDBSecondaryIndex sidx = null;
		for(int i=0;i < s;i++)
		{					
			sidx = sec_indexes.get(i);
			//System.out.println("\nSEC INDEX NAME IS "+sidx.getName());
			//System.out.println("DIRTY FIELDS ARE "+dirty_fields);
			for(int ii = 0; ii < ss;ii++ )
			{
				if(sidx.indexesField(dirty_fields.get(ii)))
				{
					save_to_secondary_index(parent_txn,pkey, sidx, e, update);
					break;/*we break here because we only want to update an index
							//	once if it is a multifield index*/
				}
			}
		}
	}
	
	private void save_to_secondary_index(Transaction parent_txn,DatabaseEntry pkey,BDBSecondaryIndex sidx,Entity e,boolean update) throws DatabaseException
	{	
		//need to update index
		if(update)
		{
			//System.out.println(">>>>DELETEING "+LongBinding.entryToLong(pkey)+" FROM "+sidx.getName());
			sidx.deleteIndexEntry(parent_txn,pkey);
		}				
		//System.out.println(">>>>INSERTING "+LongBinding.entryToLong(pkey)+" TO "+sidx.getName());
		sidx.insertIndexEntry(parent_txn,e,pkey);						

	}
	
	/* BEGIN resolve relationship side effects */
	/**
	 * Takes an entity that has been updated and finds relationships for dirty fields.
	 * If deleting or inserting, all fields are used in the search for relationships.
	 * If any relationships are found, work is passed on to resolve_relationship_sidefekt.
	 * @param parent_txn
	 * @param e The entity that has just be operated on.
	 * @param operation The entity operation (INSERT, UPDATE, DELETE)
	 * @throws DatabaseException
	 */
	private void resolve_relationship_sidefx(Transaction parent_txn,Entity e,int operation) throws DatabaseException
	{
		// TODO get references only
		List<String> dirty_fields = operation == DELETE ? e.getEntityDefinition().getFieldNames() : e.getDirtyAttributes();
		int s = dirty_fields.size();
		
		String dirty_fieldname = null;
		EntityRelationshipDefinition r = null;
		String entity_type = e.getType();
		Map<String,EntityRelationshipDefinition> ofmap = entity_relationship_map.get(entity_type);
		if(ofmap == null)
			return;
		for(int i = 0; i < s;i++)
		{
			dirty_fieldname = dirty_fields.get(i);
			r = ofmap.get(dirty_fieldname);
			if(r == null)
				continue;
			
			resolve_relationship_sidefekt(parent_txn,e,dirty_fieldname,operation,r);
		}
	}
	

	/**
	 * Delegates work by relationship type: 1 to 1, 1 to many, many to 1, many to many
	 * @param ptxn
	 * @param e The entity that has just be operated on.
	 * @param dirty_field The name of a field in the entity that has already been modified (but not saved yet if the operation is UPDATE).
	 * @param opertation INSERT, UPDATE, or DELETE
	 * @param relationship The dirty field is a part of this relationship.
	 * @throws DatabaseException
	 */
	public void resolve_relationship_sidefekt(Transaction ptxn,Entity e,String dirty_field,int opertation,EntityRelationshipDefinition relationship) throws DatabaseException
	{
		
		if (e.getType().equals(relationship.getTargetEntity()) && dirty_field.equals(relationship.getTargetEntityField())) 
		{
			relationship = relationship.flip();
		}
		
		String relation = relationship.getTargetEntity();
		String relation_field_to_e = relationship.getTargetEntityField();
				
		switch(relationship.getType())
		{
			case EntityRelationshipDefinition.TYPE_ONE_TO_ONE:
				resolve_one_to_one(ptxn, opertation, e, dirty_field, relation, relation_field_to_e);
				break;
			case EntityRelationshipDefinition.TYPE_ONE_TO_MANY:
				resolve_one_to_many(ptxn, opertation, e, dirty_field, relation, relation_field_to_e);
				break;
			case EntityRelationshipDefinition.TYPE_MANY_TO_ONE:
				resolve_many_to_one(ptxn, opertation, e, dirty_field, relation, relation_field_to_e);
				break;
			case EntityRelationshipDefinition.TYPE_MANY_TO_MANY:
				resolve_many_to_many(ptxn, opertation, e, dirty_field, relation, relation_field_to_e);
				break;
			default:
				throw new DatabaseException("UNIMPLEMENTED ");
		}	
	}
	
	
	private void resolve_one_to_one(Transaction ptxn, int operation, Entity e, String dirty_field, String relation, String relation_field_to_e) throws DatabaseException
	{
		validate_entity_for_relationship((Entity)e.getAttribute(dirty_field));
		
		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(e.getType());
		BDBPrimaryIndex rel_pidx = entity_primary_indexes_as_map.get(relation);
		
		if (operation==UPDATE || operation==DELETE)
		{
			Entity old_rel = (Entity)pidx.getById(ptxn,e.getId()).getAttribute(dirty_field);
			if(old_rel != null)
			{
				old_rel.getAttributes().put(relation_field_to_e, null);
				rel_pidx.saveEntity(ptxn, old_rel);			
			}
		}
		if (operation==INSERT || operation==UPDATE)
		{
			Entity new_rel   = (Entity)e.getAttribute(dirty_field);
			if(new_rel != null)
			{
				new_rel.getAttributes().put(relation_field_to_e, e);
				rel_pidx.saveEntity(ptxn, new_rel);
			}
		}
	}


	@SuppressWarnings("unchecked")
	private void resolve_one_to_many(Transaction ptxn, int operation, Entity e, String dirty_field, String relation, String relation_field_to_e) throws DatabaseException
	{
		validate_entity_for_relationship((Entity)e.getAttribute(dirty_field));
		
		BDBPrimaryIndex child_pidx = entity_primary_indexes_as_map.get(e.getType());
		BDBPrimaryIndex father_pidx = entity_primary_indexes_as_map.get(relation);
		
		if (operation==UPDATE || operation==DELETE)
		{
			Entity old_child_record = /* fill e */ (Entity)child_pidx.getById(ptxn,e.getId());
			Entity old_father = (Entity)old_child_record.getAttribute(dirty_field);
			if(old_father != null)
			{
				/* remove e from the old father */
				old_father = /* fill */ father_pidx.getById(ptxn, old_father.getId());
				List<Entity> old_fathers_children = (List<Entity>)old_father.getAttribute(relation_field_to_e);
				if (old_fathers_children==null)
					throw new DatabaseException("RESOLVE RELATIONSHIP INTEGRITY ERROR resolve_one_to_many - father of child must have children");
				old_fathers_children.remove(old_child_record);
				father_pidx.saveEntity(ptxn, old_father);
			}
		}
		if (operation==INSERT || operation==UPDATE)
		{
			Entity new_father   = (Entity)e.getAttribute(dirty_field);
			if(new_father != null)
			{
				List<Entity> new_fathers_children = (List<Entity>)new_father.getAttribute(relation_field_to_e);
				if(new_fathers_children == null)
				{
					new_fathers_children = new ArrayList<Entity>();
					new_father.getAttributes().put(relation_field_to_e, new_fathers_children);
				}
				/* add e to the new relation */
				new_fathers_children.add(e);
				father_pidx.saveEntity(ptxn,new_father);
			}
		}
	}
	

	@SuppressWarnings("unchecked")
	private void resolve_many_to_one(Transaction ptxn, int operation, Entity e, String dirty_field, String relation, String relation_field_to_e) throws DatabaseException
	{
		validate_entities_for_relationship((List<Entity>)e.getAttribute(dirty_field));
		
		BDBPrimaryIndex father_pidx = entity_primary_indexes_as_map.get(e.getType());
		BDBPrimaryIndex child_pidx = entity_primary_indexes_as_map.get(relation);

		Map<Long,Entity> removed_children_map = new HashMap<Long,Entity>();
		List<Entity> added_children = new ArrayList<Entity>();
		calc_added_and_removed(ptxn, e, dirty_field, father_pidx, child_pidx, operation, added_children, removed_children_map);
		
		if (operation==UPDATE || operation==DELETE)
		{
			Iterator<Entity> i = removed_children_map.values().iterator();
			while (i.hasNext())
			{
				Entity c = i.next();
				c.getAttributes().put(relation_field_to_e, null);
				child_pidx.saveEntity(ptxn, c);				
			}
			
			int s = added_children.size();
			for(int j = 0; j < s;j++)
			{
				Entity c = added_children.get(j);
				Entity old_father = (Entity)c.getAttribute(relation_field_to_e);
				if (old_father!=null) {
					expand_entity(ptxn, father_pidx, old_father);
					List<Entity> old_fathers_children = (List<Entity>) old_father.getAttribute(dirty_field);
					old_fathers_children.remove(c);
					father_pidx.saveEntity(ptxn, old_father);
				}
			}
		}
	
		if (operation==INSERT || operation==UPDATE)
		{
			int s = added_children.size();
			for(int i = 0; i < s;i++)
			{
				Entity c = added_children.get(i);
				c.getAttributes().put(relation_field_to_e, e);
				child_pidx.saveEntity(ptxn, c);
			}
		}
		
	}

	@SuppressWarnings("unchecked")
	private void resolve_many_to_many(Transaction ptxn, int operation, Entity e, String dirty_field, String relation, String relation_field_to_e) throws DatabaseException
	{
		validate_entities_for_relationship((List<Entity>)e.getAttribute(dirty_field));
		
		BDBPrimaryIndex orig_pidx = entity_primary_indexes_as_map.get(e.getType());
		BDBPrimaryIndex targ_pidx = entity_primary_indexes_as_map.get(relation);
		
		Map<Long,Entity> removed_children_map = new HashMap<Long,Entity>();
		List<Entity> added_children = new ArrayList<Entity>();
		calc_added_and_removed(ptxn, e, dirty_field, orig_pidx, targ_pidx, operation, added_children, removed_children_map);
		//
		if (operation==DELETE)
		{
			int s = added_children.size();
			for(int j = 0; j < s;j++)
			{
				Entity c = added_children.get(j);
				Entity old_father = (Entity)c.getAttribute(relation_field_to_e);
				old_father = orig_pidx.getById(ptxn, old_father.getId());
				List<Entity> old_fathers_children = (List<Entity>) old_father.getAttribute(dirty_field);
				old_fathers_children.remove(c);
				orig_pidx.saveEntity(ptxn, old_father);
			}
		}
		
		if (operation==UPDATE)
		{
			Iterator<Entity> i = removed_children_map.values().iterator();
			while (i.hasNext())
			{
				Entity t = i.next();
				List<Entity> tc = (List<Entity>)t.getAttribute(relation_field_to_e);
				tc.remove(e); //satisfies condition because entity equals method matches by type & id (not field vals)
				targ_pidx.saveEntity(ptxn, t);			
			}			
		}
	
		if (operation==INSERT || operation==UPDATE)
		{
			int s = added_children.size();
			for(int i = 0; i < s;i++)
			{
				Entity t = added_children.get(i);
				List<Entity> tc = (List<Entity>)t.getAttribute(relation_field_to_e);
				if (tc == null)
				{
					tc = new ArrayList<Entity>();
					t.getAttributes().put(relation_field_to_e, tc);
				}
				tc.add(e);
				targ_pidx.saveEntity(ptxn, t);			
			}
		}
		
	}
	


	/*
	 *  utilities for relationship resolution
	 */
	private void validate_entity_for_relationship(Entity e) throws DatabaseException
	{
		if(e != null)
		{
			if(e.getId() == Entity.UNDEFINED)
				throw new DatabaseException("SAVE DEEP IS NOT SUPPORTED. SAVE CHILD REFERENCES BEFORE SAVING THEM IN A RELATIONSHIP CONTEXT");		
			if(e.isDirty())
				throw new DatabaseException("CANT SAVE DIRTY MEMBER REFERENCE");
		}
	}
	
	private void validate_entities_for_relationship(List<Entity> new_children) throws DatabaseException
	{
		if(new_children != null) 
		{	
			int s = new_children.size();
			for(int i = 0; i < s;i++)
			{
				validate_entity_for_relationship( new_children.get(i) );
			}
		}		
	}
	
	@SuppressWarnings("unchecked")
	private void calc_added_and_removed(Transaction ptxn, Entity e, String dirty_field, BDBPrimaryIndex father_pidx, BDBPrimaryIndex child_pidx, int op, List<Entity> added_children, Map<Long, Entity> removed_children_map) throws DatabaseException
	{
		// e is originating
		Entity old_orig = (Entity)father_pidx.getById(ptxn, e.getId());
		List<Entity> old_children = (List<Entity>)old_orig.getAttribute(dirty_field);
		List<Entity> new_children = (List<Entity>)e.getAttribute(dirty_field);//must already filled
		if (op==DELETE || op==INSERT)
		{
			// in the case of a delete the added children act as all remove
			if (old_children!=null)
				added_children.addAll(old_children);
		}
		else if (op==UPDATE)
		{
			// compare the old & new lists to determine what has been added & removed
			get_map_from_list(old_children,removed_children_map);
			if (new_children!=null)
			{
				for (int i=0; i<new_children.size(); i++)
				{
					Entity c = new_children.get(i);
					if (removed_children_map.containsKey(c.getId()))
						removed_children_map.remove(c.getId());
					else 
						added_children.add(c);
				}
			}
		}
		// fill the attributes of relevant info
		for (int i=0; i<added_children.size(); i++)
		{
			expand_entity(ptxn, child_pidx, added_children.get(i));
		}
		Iterator<Long> i=removed_children_map.keySet().iterator();
		while(i.hasNext())
		{
			expand_entity(ptxn, child_pidx, removed_children_map.get(i.next()));
		}
	}

	private void expand_entity(Transaction ptxn, BDBPrimaryIndex pidx,Entity entity) throws DatabaseException
	{
		if (!entity.getAttributes().isEmpty())
			return;
		if (entity.getId()==Entity.UNDEFINED)
			throw new DatabaseException("INTEGRITY PROBLEM - all remove & added children is relationships have to be weakly filled at least");
		Entity e = pidx.getById(ptxn, entity.getId());
		entity.setAttributes(e);
	}

	private Map<Long, Entity> get_map_from_list(List<Entity> entities, Map<Long, Entity> x)
	{
		if (entities==null || x==null)
			return null;
		int s = entities.size();
		for (int i=0; i<s; i++)
		{
			Entity e = entities.get(i);
			x.put(e.getId(),e);
		}
		return x;
	}
	
	
	/*
	 * END resolve relationship side effects
	 */

	
	
	
	public void deleteEntity(Entity e) throws PersistenceException
	{	
		_store_locker.enterAppThread();
		String entity_type 		  = e.getType();
		BDBPrimaryIndex pi = entity_primary_indexes_as_map.get(entity_type);
		if(pi == null)
			throw new PersistenceException("ENTITY OF TYPE "+entity_type+" DOES NOT EXIST");
		Transaction txn = null;
		DatabaseEntry pkey;
		try{
			txn = environment.beginTransaction(null, null);	
			resolve_relationship_sidefx(txn,e, DELETE);
			pkey = pi.deleteEntity(txn,e);
			if(pkey == null)
			{
				txn.commitNoSync();
				throw new PersistenceException("ENTITY "+e.getType()+" "+e.getId()+" DOES NOT EXIST." +
												"YOU CANNOT DELETE THAT WHICH DOES NOT EXIST.");
			}
			delete_from_secondary_indexes(txn, pkey, e);
			txn.commitNoSync();
			/* have a more robust cache expiration policy at some point...probably
			 * just blow away complex cache */
			clean_query_cache(entity_type);
		}catch(DatabaseException de)
		{
			abortTxn(txn);
			logger.error("deleteEntity(Entity)", de);
			throw new PersistenceException("DELETE FAILED FOR ENTITY "+e+". ABORTING");
		}
		finally
		{
			_store_locker.exitAppThread();
		}	

	}
	

	private void delete_from_secondary_indexes(Transaction parent_txn,DatabaseEntry pkey,Entity e) throws DatabaseException
	{
		List<BDBSecondaryIndex>sec_indexes = entity_secondary_indexes_as_list.get(e.getType());
		int s 					  = sec_indexes.size();
		BDBSecondaryIndex sidx = null;
		for(int i=0;i < s;i++)
		{	
			sidx = sec_indexes.get(i);
			delete_from_secondary_index(parent_txn,pkey, sidx, e);
		}
		
	}
	
	private void delete_from_secondary_index(Transaction parent_txn,DatabaseEntry pkey,BDBSecondaryIndex sidx,Entity e) throws DatabaseException
	{	
			//System.out.println(">>>>DELETEING "+seqnum+" FROM "+sidx.getName());
		sidx.deleteIndexEntry(parent_txn,pkey);
	}

	
	public EntityDefinition getEntityDefinition(String entity_name) throws PersistenceException
	{
		_store_locker.enterAppThread();	
		EntityDefinition def;
		def = do_get_entity_definition(entity_name);
		_store_locker.exitAppThread();	
		
		if(def == null)
			throw new PersistenceException("CANT FIND ENTITY DEFINITION FOR "+entity_name);
		else
			return def;	
	}

	private EntityDefinition do_get_entity_definition(String entity_name)
	{
		BDBPrimaryIndex pidx;
		if((pidx = entity_primary_indexes_as_map.get(entity_name)) != null)
			return pidx.getEntityDefinition();	
		else
			return null;
	}

	public List<EntityDefinition> getEntityDefinitions()throws PersistenceException
	{
		_store_locker.enterAppThread();	
		List<EntityDefinition> ret = new ArrayList<EntityDefinition>();
		int s = entity_primary_indexes_as_list.size();
		try{
		
			for(int i = 0; i < s;i++)
				ret.add(entity_primary_indexes_as_list.get(i).getEntityDefinition());

		return ret;
		}catch(Exception e)
		{
			throw new PersistenceException("FAILED GETTING ENTITY DEFINITIONS FROM MAP.");
		}
		finally
		{
			_store_locker.exitAppThread();
		}
	}
	
	/* get the entity defs from the db and bootstrap the entity_def cache */
	protected List<EntityDefinition> get_entity_definitions_from_db() throws PersistenceException
	{
		List<EntityDefinition> defs = new ArrayList<EntityDefinition>();
		Cursor cursor = null;
		try
		{
			cursor = entity_def_db.openCursor(null, null);
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
			{
				EntityDefinition e = (EntityDefinition) entity_def_binding.entryToObject(this,data);
				defs.add(e);
			}
			close_cursors(cursor);
			return defs;
		}
		catch (Exception de)
		{
			logger.error("get_entity_definitions_from_db()", de);
			close_cursors(cursor);
			throw new PersistenceException("ERROR READING ENTITY DEFINITIONS FROM DATABASE.");
		}
	}
	
	/* get the entity defs from the db and bootstrap the entity_def cache */
	protected List<EntityIndex> get_entity_indices_from_db(String entity_name) throws PersistenceException
	{
		List<EntityIndex> indices = new ArrayList<EntityIndex>();
		Cursor cursor = null;
		try
		{
			cursor = entity_index_db.openCursor(null, null);
			DatabaseEntry key  = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			
			FieldBinding.valueToEntry(Types.TYPE_STRING, entity_name, key);				
			OperationStatus op_stat = cursor.getSearchKey(key, data, LockMode.DEFAULT);
			while (op_stat == OperationStatus.SUCCESS)
			{
				EntityIndex e = (EntityIndex) entity_index_binding.entryToObject(this,data);
				indices.add(e);
				op_stat = cursor.getNextDup(key, data, LockMode.DEFAULT);
			}
			close_cursors(cursor);
			return indices;
		}
		catch (Exception de)
		{
			logger.error("get_entity_indices_from_db(String)", de);
			close_cursors(cursor);
			throw new PersistenceException("ERROR READING ENTITY INDICES FROM DATABASE.");
		}
	}

	/* get the entity defs from the db and bootstrap the entity_def cache */
	protected List<EntityRelationshipDefinition> get_entity_relationships_from_db() throws PersistenceException
	{
		List<EntityRelationshipDefinition> rels = new ArrayList<EntityRelationshipDefinition>();
		Cursor cursor = null;
		try
		{
			cursor = entity_relationship_db.openCursor(null, null);
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
			{
				EntityRelationshipDefinition r = (EntityRelationshipDefinition) entity_relationship_binding.entryToObject(data);
				rels.add(r);
			}
			close_cursors(cursor);
			return rels;
		}
		catch (Exception de)
		{
			logger.error("get_entity_relationships_from_db()", de);
			close_cursors(cursor);
			throw new PersistenceException("ERROR READING ENTITY DEFINITIONS FROM DATABASE.");
		}		
	}

	//!!! NOT PART OF PERSISTENCE INTERFACE ANYMORE
	public List<EntityIndexDefinition> getEntityIndexDefinitions() throws PersistenceException
	{
		return _entity_index_definition_manager.getDefinitions();
	}
	
	//!!! NOT PART OF PERSISTENCE INTERFACE ANYMORE	
	public EntityIndexDefinition getEntityIndexDefinition(String name)
	{
		return _entity_index_definition_manager.getDefinition(name);
	}
	
	public List<EntityIndex> getEntityIndices(String entity) throws PersistenceException
	{
		_store_locker.enterAppThread();	
		
		BDBPrimaryIndex pi = entity_primary_indexes_as_map.get(entity);
		if(pi == null)
			throw new PersistenceException("ENTITY OF TYPE "+entity+" DOES NOT EXIST");
		try{
			List<EntityIndex> indices = new ArrayList<EntityIndex>();
			List<BDBSecondaryIndex> bdb_idx = entity_secondary_indexes_as_list.get(entity);
			for (int i=0; i<bdb_idx.size(); i++)
				indices.add(bdb_idx.get(i).getEntityIndex());
			return indices;
		}catch(Exception e)
		{
			throw new PersistenceException("PROBLEM GETTING INDICES FOR "+entity);
		}
		finally
		{
			_store_locker.exitAppThread();
		}
	}

	public void addEntityRelationship(EntityRelationshipDefinition r) throws PersistenceException
	{
		_store_locker.enterLockerThread();
		try{
			logger.debug("addEntityRelationship(EntityRelationshipDefinition) - ADD ENTITY RELATIONSHIP " + r);
			do_add_entity_relationship(r);	
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			logger.debug("addEntityRelationship(EntityRelationshipDefinition) - LOCKER THREAD IS EXITING");
			_store_locker.exitLockerThread();	
		}	
	}
	


	public void do_add_entity_relationship(EntityRelationshipDefinition r) throws PersistenceException
	{
		String oe  = r.getOriginatingEntity();
		String oef = r.getOriginatingEntityField();
		String te  = r.getTargetEntity();
		String tef = r.getTargetEntityField();
		
		EntityDefinition od = do_get_entity_definition(oe);
		EntityDefinition td = do_get_entity_definition(te);
		FieldDefinition of;
		FieldDefinition tf;
		if(entity_primary_indexes_as_map.get(oe) == null)
			throw new PersistenceException("BAD ENTITY RELATIONSHIP. NO SUCH ORIGINATING ENTITY TYPE "+oe);
		if(entity_primary_indexes_as_map.get(te) == null)	
			throw new PersistenceException("BAD ENTITY RELATIONSHIP. NO SUCH TARGET ENTITY TYPE "+te);


		if((of = od.getField(oef)) == null || of.getBaseType() != Types.TYPE_REFERENCE)
			throw new PersistenceException("BAD ENTITY RELATIONSHIP. NO REFERENCE FIELD NAMED "+oef+" IN "+oe);
		if((tf = td.getField(tef)) == null || tf.getBaseType() != Types.TYPE_REFERENCE)
			throw new PersistenceException("BAD ENTITY RELATIONSHIP. NO REFERENCE FIELD NAMED "+tef+" IN "+te);

		switch(r.getType())
		{
			case EntityRelationshipDefinition.TYPE_ONE_TO_ONE:
				if(of.isArray())
					throw new PersistenceException("BAD ENTITY RELATIONSHIP. ONE TO ONE RELATIONSHIP CANNOT EXIST" +
													" WITH ORIGINATING FIELD "+oe+"."+oef+" OF TYPE ARRAY");
				if(tf.isArray())
					throw new PersistenceException("BAD ENTITY RELATIONSHIP. ONE TO ONE RELATIONSHIP CANNOT EXIST" +
													" WITH TARGET FIELD "+te+"."+tef+" OF TYPE ARRAY");
				break;
			case EntityRelationshipDefinition.TYPE_MANY_TO_ONE:
				if(!of.isArray())
					throw new PersistenceException("BAD ENTITY RELATIONSHIP. MANY TO ONE RELATIONSHIP CANNOT EXIST" +
													" WITH ORIGINATING FIELD "+oe+"."+oef+" NOT OF TYPE ARRAY");
				if(tf.isArray())
					throw new PersistenceException("BAD ENTITY RELATIONSHIP. MANY TO ONE RELATIONSHIP CANNOT EXIST" +
													" WITH TARGET FIELD "+te+"."+tef+" OF TYPE ARRAY");
				break;
			case EntityRelationshipDefinition.TYPE_ONE_TO_MANY:
				if(of.isArray())
					throw new PersistenceException("BAD ENTITY RELATIONSHIP. ONE TO MANY RELATIONSHIP CANNOT EXIST" +
													" WITH ORIGINATING FIELD "+oe+"."+oef+" OF TYPE ARRAY");
				if(!tf.isArray())
					throw new PersistenceException("BAD ENTITY RELATIONSHIP. ONE TO MANY RELATIONSHIP CANNOT EXIST" +
													" WITH TARGET FIELD "+te+"."+tef+" NOT OF TYPE ARRAY");
				break;
			case EntityRelationshipDefinition.TYPE_MANY_TO_MANY:
				if(!of.isArray())
					throw new PersistenceException("BAD ENTITY RELATIONSHIP. MANY TO MANY RELATIONSHIP CANNOT EXIST" +
													" WITH ORIGINATING FIELD "+oe+"."+oef+" NOT OF TYPE ARRAY");
				if(!tf.isArray())
					throw new PersistenceException("BAD ENTITY RELATIONSHIP. MANY TO ONE RELATIONSHIP CANNOT EXIST" +
													" WITH TARGET FIELD "+te+"."+tef+" NOT OF TYPE ARRAY");
				break;
		}
		
		/* different views of entity relationship map */
		Map<String,EntityRelationshipDefinition> ofmap; 
		Map<String,EntityRelationshipDefinition> tfmap; 
		
		/* create them lazily */
		ofmap = entity_relationship_map.get(oe);
		if(ofmap == null)
		{
			ofmap = new HashMap<String,EntityRelationshipDefinition>();
			entity_relationship_map.put(oe, ofmap);
		}
		if(ofmap.get(oef) != null)
			throw new PersistenceException("ENTITY RELATIONSHIP ALREADY DEFINED FOR REF FIELD NAMED "+oef+" IN "+oe);

		tfmap = entity_relationship_map.get(te);
		if(tfmap == null)
		{
			tfmap = new HashMap<String,EntityRelationshipDefinition>();
			entity_relationship_map.put(te, tfmap);
		}
		if(tfmap.get(tef) != null)
			throw new PersistenceException("ENTITY RELATIONSHIP ALREADY DEFINED FOR REF FIELD NAMED "+tef+" IN "+te);
		
		try{
			add_entity_relationship_to_db(r);
			/*put in cache */
			ofmap.put(of.getName(),r);
			tfmap.put(tf.getName(),r);
			logger.debug("do_add_entity_relationship(EntityRelationshipDefinition) - ADDED ENTITY RELATIONSHIP " + r);
		}catch(DatabaseException de)
		{
			logger.error("do_add_entity_relationship(EntityRelationshipDefinition)", de);
			throw new PersistenceException("UNABLE TO ADD ENTITY RELATIONSHIP.INTERNAL ERROR. SEE LOGS");
		}

	}

	private  void add_entity_relationship_to_db(EntityRelationshipDefinition r)throws DatabaseException
	{
		StringBuffer buf = new StringBuffer();
		buf.append(r.getOriginatingEntity());
		buf.append(r.getOriginatingEntityField());
		buf.append(r.getTargetEntity());
		buf.append(r.getTargetEntityField());
		
		DatabaseEntry key = new DatabaseEntry();
		DatabaseEntry data = new DatabaseEntry(new byte[256]);
		FieldBinding.valueToEntry(Types.TYPE_STRING, buf.toString(), key);

		entity_relationship_binding.objectToEntry(r,data);		
		entity_relationship_db.put(null, key, data); 

	}
	
	protected EntityRelationshipDefinition get_entity_relationship(String entity_name,String fieldname)
	{
		try{
			return entity_relationship_map.get(entity_name).get(fieldname);
		}catch(Exception e)
		{
			logger.error("get_entity_relationship(String, String)", e);
			return null;
		}
	}

	public List<EntityRelationshipDefinition> getEntityRelationships() throws PersistenceException
	{
		_store_locker.enterAppThread();	
		try{
			List<EntityRelationshipDefinition> rels = new ArrayList<EntityRelationshipDefinition>();
			Iterator<String> ei = entity_relationship_map.keySet().iterator();
			HashMap<EntityRelationshipDefinition,Object> already_added = new HashMap<EntityRelationshipDefinition,Object>();
			
			while(ei.hasNext())
			{
				String entity_name = ei.next();
				Map<String,EntityRelationshipDefinition> frmap = entity_relationship_map.get(entity_name);
				Iterator<String> fi = frmap.keySet().iterator();
				while(fi.hasNext())
				{
					EntityRelationshipDefinition r = frmap.get(fi.next());
					if(already_added.get(r) == null)
					{
						rels.add(r);
						already_added.put(r,null);	
					}
					else
						continue;
				}
			}
			return rels;
		}catch(Exception e)
		{
			throw new PersistenceException("FAILED GETTING ENTITY RELATIONSHIPS FROM MAP.");
		}
		finally
		{
			_store_locker.exitAppThread();
		}
	}
	

	protected List<EntityRelationshipDefinition> do_get_entity_relationships() throws DatabaseException
	{
		List<EntityRelationshipDefinition> rels = new ArrayList<EntityRelationshipDefinition>();
		Cursor cursor = null;
		try
		{
			cursor = entity_relationship_db.openCursor(null, null);
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
			{
				EntityRelationshipDefinition e = (EntityRelationshipDefinition) entity_relationship_binding.entryToObject(data);
				rels.add(e);
			}
			close_cursors(cursor);
		}
		catch (Exception de)
		{
			close_cursors(cursor);
			logger.error("getEntityDefinitions() - Error accessing database." + de, de);
		}
		return rels;
	}

	
	/**
	 * The safe way to checkpoint the database
	 * @throws PersistenceException
	 */
	public void checkpoint() throws PersistenceException
	{
		_store_locker.enterLockerThread();
		try
		{
			do_checkpoint();
		}
		catch (PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitLockerThread();
		}
	}
	
	/**
	 * Forces a checkpoint without a lock on the store. This should be done with caution. 
	 * 
	 * @throws PersistenceException
	 */
	public void do_checkpoint() throws PersistenceException
	{
		try
		{
			environment.checkpoint(null);
			logger.debug("C H E C K P O I N T");
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
			throw new PersistenceException("Can't checkpoint database", e);
		}
	}
	
	/**
	 * ALERT! this will allow all remaining transactions to complete. then it will refuse
	 * all incoming requests. remember to unlock!
	 */
	public void lock()
	{
		_store_locker.enterLockerThread();
	}

	/**
	 * thanks for unlocking the store.
	 */
	public void unlock()
	{
		_store_locker.exitLockerThread();
	}
	
	public void archive(File destination_directory) throws PersistenceException
	{
		checkpoint();
		try
		{
			lock();
			File[] archive_dbs = environment.getArchiveDatabases();
			File[] archive_logs = environment.getArchiveLogFiles(true);
			for (int i=0; i<archive_dbs.length; i++)
			{
				copy(archive_dbs[i], destination_directory);
			}
			copy(archive_logs[archive_logs.length-1], destination_directory);
		}
		catch (DatabaseException e)
		{
			e.printStackTrace();
			throw new PersistenceException("Can't archive store", e);
		}
		finally
		{
			unlock();
		}
		
	}
	
	//http://www.javalobby.org/java/forums/t17036.html
	private void copy(File file, File destination_directory) throws PersistenceException
	{
		FileChannel ic = null;
		FileChannel oc =  null;
		try
		{
			ic = new FileInputStream(file).getChannel();
			oc = new FileOutputStream(new File(destination_directory, file.getName())).getChannel();
			ic.transferTo(0, ic.size(), oc);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			throw new PersistenceException("Cant make archive copy!",e);
		}
		finally
		{
			try
			{
				if (ic!=null)
					ic.close();
				if (oc!=null)
					oc.close();
			} 
			catch (Exception e)
			{
				e.printStackTrace();
				throw new PersistenceException("Cant make archive copy!",e);
			}
		}

	}


	public void useEnvironment(String environment_name) throws PersistenceException
	{
		close();
		set_active_env_prop(environment_name);
		init(_config);
	}
	
	
	
	private void set_active_env_prop(String relative_path) throws PersistenceException
	{
		_db_env_props.put(BDBConstants.KEY_ACTIVE_ENVIRONMENT, relative_path);
    	try
		{
    		_db_env_props.store(new FileOutputStream(_db_env_props_file), "PS PERSISTENCE BERKELEY DB ENVIRONMENT PROPERTIES");
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
			throw new PersistenceException("SELECT ENVIRONMENT ERROR - CANNOT INITIALIZE PROPERTIES FILE IN ENV ROOT",e1);
		}
	}

	private boolean _closed = false;
	public void close() throws PersistenceException
	{
		_store_locker.enterLockerThread();	
		try{
			if(!_closed)
			{
				do_close();
				_closed = true;
			}
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitLockerThread();
		}	
	}
	
	private void do_close() throws PersistenceException
	{
		long t = System.currentTimeMillis();
		try
		{			
			for(int i = 0; i < entity_primary_indexes_as_list.size();i++)
			{
				BDBPrimaryIndex pidx = entity_primary_indexes_as_list.get(i);
				String ename 		 = pidx.getEntityDefinition().getName();
				pidx.close(entity_secondary_indexes_as_list.get(ename));
			}
			if (entity_index_db != null)
			{
				entity_index_db.sync();		
				entity_index_db.close();		
			}
			if (entity_def_db != null)
			{
				entity_def_db.sync();		
				entity_def_db.close();		
			}
			if(entity_relationship_db != null)
			{
				entity_relationship_db.sync();
				entity_relationship_db.close();
			}
			if (environment != null)
				environment.close();	

		}
		catch (Exception e)
		{
			logger.error("do_close()", e);
			e.printStackTrace();
			throw new PersistenceException("FIALED TO CLOSE PERSISTENT STORE");
		}
		if(_deadlock_monitor_running)
			stop_deadlock_monitor();		


		

		logger.debug("do_close() - CLOSED EVERYTHING IN " + (System.currentTimeMillis() - t) + " MS");		
	}

	/******************SUPPORT FUNCTIONS**************************************************/

	private static SimpleDateFormat timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss");
	public static String timestamp()
	{
		return timestamp.format(new Date());
	}
	
	private void init_environment(HashMap<Object, Object> config) throws PersistenceException
	{
		String path = (String)config.get(BDBStoreConfigKeyValues.KEY_STORE_ROOT_DIRECTORY);
		if(path == null)
			throw new PersistenceException("PLEASE SPECIFY BASE_DB_ENV IN CONFIG");		

		File db_env_home = null;
		File db_env_root = new File(path);
		_db_env_props_file = new File(db_env_root, BDBConstants.ENVIRONMENT_PROPERTIES_FILE_NAME);
		_db_env_props = new Properties();
		try 
		{
			_db_env_props.load(new FileInputStream(_db_env_props_file));
            db_env_home = new File(db_env_root, _db_env_props.getProperty(BDBConstants.KEY_ACTIVE_ENVIRONMENT));
        } 
		catch (Exception e) 
		{
        	db_env_home = new File(db_env_root, timestamp());
        	db_env_home.mkdir();
        	set_active_env_prop(db_env_home.getName());
        }
        if (db_env_home == null || !db_env_home.exists())
        	throw new PersistenceException("INVALID BASE_DB_ENV. "+db_env_home+" DOES NOT EXIST");
		if(!db_env_home.isDirectory())
			throw new PersistenceException("INVALID BASE_DB_ENV. "+db_env_home+" NOT A DIRECTORY");
		
logger.debug("init_environment(HashMap<Object,Object>) - INITIALIZING ENVIRONMENTntPATH=" + db_env_home + "n");		
		try
		{
			EnvironmentConfig env_cfg = get_tds_default_config();
			
			Integer val = (Integer)config.get(BDBStoreConfigKeyValues.KEY_DEADLOCK_RESOLUTION_SCHEME);
			if(val == null || val  == BDBStoreConfigKeyValues.VALUE_DEADLOCK_RESOLUTION_SCHEME_ALWAYS_CRAWL_LOCKTABLE) 
				env_cfg.setLockDetectMode(LockDetectMode.OLDEST);
	
			environment = new Environment(db_env_home, env_cfg);
			
		}
		catch (Exception e)
		{
			logger.error("init_environment(HashMap<Object,Object>)", e);
			throw new PersistenceException("COULD NOT INITIALIZE BDB ENVIRONMENT AT "+db_env_home);
		}		
	}

	private void init_entity_definition_db(HashMap<Object,Object> config) throws PersistenceException
	{

		DatabaseConfig cfg = get_default_primary_db_config();
		try{
			entity_def_db = environment.openDatabase(null, BDBConstants.ENTITY_DEFINITION_DB_NAME, null, cfg);
		}catch (Exception e){	

			logger.error("init_entity_definition_db(HashMap<Object,Object>)", e);
			throw new PersistenceException("UNABLE TO OPEN ENTITY DEFINITION DB "+BDBConstants.ENTITY_DEFINITION_DB_NAME);
		}		

		entity_def_binding = new EntityDefinitionBinding();
		logger.debug("init_entity_definition_db(HashMap<Object,Object>) - OPENED ENTITY DEFINITION DATABASE ");
		
	}
	
	private void init_entity_secondary_index_db(HashMap<Object,Object> config) throws PersistenceException
	{

		DatabaseConfig cfg = get_entity_index_db_config();
		try{
			entity_index_db = environment.openDatabase(null, BDBConstants.ENTITY_INDEX_DB_NAME, null, cfg);
		}catch (Exception e){	

			logger.error("init_entity_secondary_index_db(HashMap<Object,Object>)", e);
			throw new PersistenceException("UNABLE TO OPEN ENTITY INDEX DB "+BDBConstants.ENTITY_INDEX_DB_NAME);
		}		

		entity_index_binding = new EntitySecondaryIndexBinding();
		logger.debug("init_entity_secondary_index_db(HashMap<Object,Object>) - OPENED ENTITY INDEX DATABASE ");
		
	}
	
	private void init_entity_relationship_db(HashMap<Object,Object> config) throws PersistenceException
	{
		DatabaseConfig cfg = get_default_primary_db_config();
		try{	
			entity_relationship_db = environment.openDatabase(null, BDBConstants.ENTITY_RELATIONSHIP_DB_NAME, null, cfg);
		}catch(Exception e){
			logger.error("init_entity_relationship_db(HashMap<Object,Object>)", e);
			throw new PersistenceException("UNABLE TO OPEN ENTITY RELATIONSHIPS DB "+BDBConstants.ENTITY_RELATIONSHIP_DB_NAME);
		}
		entity_relationship_binding = new EntityRelationshipBinding();	

		logger.info("init_entity_relationship_db(HashMap<Object,Object>) - OPENED ENTITY RELATIONSHIP DATABASE ");
	}
	
	
	private DatabaseConfig get_default_primary_db_config()
	{
		DatabaseConfig cfg = get_primary_db_config_btree();
		return cfg;
	}
	
	private DatabaseConfig get_entity_index_db_config()
	{
		DatabaseConfig cfg = new DatabaseConfig();
		cfg.setErrorStream(System.err);
		cfg.setErrorPrefix("DB FOR ENTITY INDEXES");
		cfg.setType(DatabaseType.BTREE);
		cfg.setAllowCreate(true);
		cfg.setSortedDuplicates(true);
		cfg.setTransactional(true);
		return cfg;
	}
	
	@SuppressWarnings("unused")
	private DatabaseConfig get_primary_db_config_hash()
	{
		DatabaseConfig cfg = new DatabaseConfig();
		cfg.setType(DatabaseType.HASH);
		cfg.setAllowCreate(true);
		cfg.setTransactional(true);
		cfg.setReadUncommitted(true);
		return cfg;
	}
	
	private DatabaseConfig get_primary_db_config_btree()
	{
		
		DatabaseConfig cfg = new DatabaseConfig();
		cfg.setType(DatabaseType.BTREE);
		cfg.setAllowCreate(true);
		cfg.setTransactional(true);
		cfg.setReadUncommitted(true);
		return cfg;
	}	
	
	private void bootstrap_existing_entity_definitions() throws PersistenceException
	{
		List<EntityDefinition> defs = get_entity_definitions_from_db();
		BDBPrimaryIndex pidx;
		for (int i = 0; i < defs.size(); i++)
		{
			EntityDefinition def = defs.get(i); 
			pidx = new BDBPrimaryIndex();
			pidx.setup(environment,def);
			String entity_name = def.getName();
			entity_primary_indexes_as_map.put(entity_name, pidx);
			entity_primary_indexes_as_list.add(pidx);
			calculate_query_cache_dependencies(def);
		}
	}
	
	private void bootstrap_existing_indices() throws PersistenceException
	{
		Set<String> entity_types = entity_primary_indexes_as_map.keySet();
		for (String entity_name : entity_types){
			List<BDBSecondaryIndex> 		sec_indexes_list = new ArrayList<BDBSecondaryIndex>();
			Map<String, BDBSecondaryIndex> 	sec_indexes_map  = new HashMap<String, BDBSecondaryIndex>();
			
			BDBPrimaryIndex pidx 		= entity_primary_indexes_as_map.get(entity_name);
			List<EntityIndex> indicies 	= get_entity_indices_from_db(entity_name);
			EntityIndex eii;
			for (int ii = 0; ii < indicies.size(); ii++)
			{
				eii = indicies.get(ii);
				Class<?> c = null;
				BDBSecondaryIndex index = null;
				
				int index_type = eii.getEntityIndexType();
				String classname = null;
				switch(index_type)
				{
				case EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX:
					classname = "com.pagesociety.bdb.index.SimpleSingleFieldIndex";
					break;
				case EntityIndex.TYPE_SIMPLE_MULTI_FIELD_INDEX:
					classname = "com.pagesociety.bdb.index.SimpleMultiFieldIndex";
					break;
				case EntityIndex.TYPE_ARRAY_MEMBERSHIP_INDEX:
					classname = "com.pagesociety.bdb.index.ArrayMembershipIndex";
					break;
				case EntityIndex.TYPE_MULTIFIELD_ARRAY_MEMBERSHIP_INDEX:
					classname = "com.pagesociety.bdb.index.MultiFieldArrayMembershipIndex";
					break;
				default:
					throw new PersistenceException("UNKNOWN INDEX TYPE 0x"+Integer.toHexString(index_type));
				}
				
				try {
					c = Class.forName(classname);
					index = (BDBSecondaryIndex)c.newInstance();
				} catch (Exception e) {
					logger.error("bootstrap_existing_indices()", e);
				}
				index.setup(pidx, eii);
				sec_indexes_map.put(eii.getName(),index);
				/*this is just so we can close them easily*/
				sec_indexes_list.add(index);
			}
			entity_secondary_indexes_as_list.put(entity_name,sec_indexes_list);
			entity_secondary_indexes_as_map.put(entity_name,sec_indexes_map);

		}//end for each entity
	}
	
	private void bootstrap_existing_entity_relationships() throws PersistenceException
	{
		List<EntityRelationshipDefinition> rels = get_entity_relationships_from_db();


		for (int i = 0; i < rels.size(); i++)
		{
			EntityRelationshipDefinition r = rels.get(i);
			String oe = r.getOriginatingEntity();
			String of = r.getOriginatingEntityField();
			String te = r.getTargetEntity();
			String tf = r.getTargetEntityField();
			/* put it in going both ways */
			if(entity_relationship_map.get(oe) == null)
				entity_relationship_map.put(oe, new HashMap<String,EntityRelationshipDefinition>());
			if(entity_relationship_map.get(te) == null)
				entity_relationship_map.put(te, new HashMap<String,EntityRelationshipDefinition>());
			
			entity_relationship_map.get(oe).put(of, r);
			entity_relationship_map.get(te).put(tf, r);
		}

	}

	private QueryManager 		_query_manager;
	private QueryManagerConfig 	_query_manager_config;
	private void init_query_manager()
	{
		_query_manager_config = new QueryManagerConfig();
		_query_manager_config.setPrimaryIndexMap(entity_primary_indexes_as_map);
		_query_manager_config.setSecondaryIndexMap(entity_secondary_indexes_as_map);
		_query_manager_config.setEntityCacheInitialSize(64);
		_query_manager_config.setEntityCacheLoadFactor(0.75f);
		_query_manager_config.setEntityCacheMaxSize(128);
		_query_manager = new QueryManager(_query_manager_config);
		_query_manager.init();
	}
	
	private void init_field_binding() throws PersistenceException
	{
		FieldBinding.initWithPrimaryIndexMap(entity_primary_indexes_as_map);
	}
	
	private void init_deadlock_resolution_scheme(HashMap<Object,Object> config) throws PersistenceException
	{
		Integer val = (Integer)config.get(BDBStoreConfigKeyValues.KEY_DEADLOCK_RESOLUTION_SCHEME); 
		if(val != null && val == BDBStoreConfigKeyValues.VALUE_DEADLOCK_RESOLUTION_SCHEME_MONITOR_DEADLOCKS)
		{
			//_deadlocking_scheme = BDBStoreConfigKeyValues.VALUE_DEADLOCK_RESOLUTION_SCHEME_MONITOR_DEADLOCKS;
			int interval = (Integer)config.get(BDBStoreConfigKeyValues.KEY_DEADLOCK_RESOLUTION_SCHEME_MONITOR_DEADLOCKS_INTERVAL);
			if(interval <= 0)
				interval = DEFAULT_MONITOR_INTERVAL_FOR_MONITORING_SCHEME;
			
			start_deadlock_monitor(interval);
			logger.debug("init_deadlock_resolution_scheme(HashMap<Object,Object>) - DEADLOCK RESOLUTION SCHEME IS MONITOR WITH AND INTERVAL OF " + interval);
		}
		else
		{
			//_deadlocking_scheme = BDBStoreConfigKeyValues.VALUE_DEADLOCK_RESOLUTION_SCHEME_ALWAYS_CRAWL_LOCKTABLE;
			_deadlock_monitor_running = false;
			logger.debug("init_deadlock_resolution_scheme(HashMap<Object,Object>) - DEADLOCK RESOLUTION SCHEME IS ALWAYS CRAWL");
		}
	}
	
	private int     _total_num_deadlocks = 0;
	private void start_deadlock_monitor(final int interval)
	{
		
		if(_deadlock_monitor_running)
			return;
		logger.debug("start_deadlock_monitor(int) - Starting deadlock monitor.");
		_deadlock_monitor_running = true;
		_deadlock_monitor = new Thread()
		{
			public void run()
			{
				while(_deadlock_monitor_running)
				{
					try{
						logger.debug("run() - STARTING LOCK DETECTION");
						int num_deadlocks = environment.detectDeadlocks(LockDetectMode.MINWRITE);
						_total_num_deadlocks += num_deadlocks;
						logger.debug("run() - NUM KILLED DEADLOCKS IS " + num_deadlocks);
						logger.debug("run() - ENDING LOCK DETECTION");
						Thread.sleep(interval);
						logger.debug("run() - WAKING UP");
					}catch(Exception e)
					{
						logger.error("run()", e);
					}	
				}
			}
		};
		_deadlock_monitor.start();
	}
	
	private void stop_deadlock_monitor()
	{
		if(_deadlock_monitor_running == false)
			return;
		
		_deadlock_monitor_running = false;
		try {
			
			_deadlock_monitor.join();
			logger.debug("stop_deadlock_monitor() - STOPPING DEADLOCK MONITOR ");
			logger.debug("stop_deadlock_monitor() - tTOTAL DEADLOCKS " + _total_num_deadlocks);
		} catch (InterruptedException e) {
			logger.error("stop_deadlock_monitor()", e);
		}
	}
	

	/* TDS CONFIG */
	private EnvironmentConfig get_tds_default_config()
	{
		EnvironmentConfig env_cfg = new EnvironmentConfig();
		env_cfg.setAllowCreate(true);
		env_cfg.setInitializeCache(true);
		env_cfg.setInitializeLocking(true);
		env_cfg.setInitializeLogging(true);
		env_cfg.setRunRecovery(true);
		env_cfg.setTransactional(true);
		env_cfg.setErrorStream(System.err);		
		//1 megabytes = 1 048 576 bytes
		env_cfg.setCacheSize(1048576 * 500);
		// we need enough transactions for the number of 
		// simultaneous threads per environment
		env_cfg.setTxnMaxActive(1000);
		// locks
		env_cfg.setMaxLocks(2000);
		env_cfg.setMaxLockObjects(2000);
		env_cfg.setMaxLockers(2000);

		//env_cfg.setLockDetectMode(LockDetectMode.MINWRITE);
		//env_cfg.setVerbose(VerboseConfig.FILEOPS_ALL, true);
		return env_cfg;
	}


	public int addEntityField(String entity, FieldDefinition entity_field_def,Object default_value)throws PersistenceException
	{
		_store_locker.enterLockerThread();
		try{
			logger.debug("addEntityField(String, FieldDefinition, Object) - ADD ENTITY FIELD " + entity_field_def);
			return do_add_entity_field(entity,entity_field_def,default_value);	
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitLockerThread();	
		}
	}


	protected int do_add_entity_field(String entity,FieldDefinition field,Object default_value) throws PersistenceException
	{
		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entity);
		EntityDefinition old_def = pidx.getEntityDefinition();
		if(pidx == null)
			throw new PersistenceException("ADD ENTITY FIELD: ENTITY "+entity+" DOES NOT EXIST!");
		
		String fieldname = field.getName();		
		if(old_def.getField(field.getName())!= null)
			throw new PersistenceException("ADD ENTITY FIELD: FIELD "+fieldname+" ALREADY EXISTS IN ENTITY");
		
		EntityDefinition new_def = old_def.clone();
		/*add the field to the new def */
		new_def.addField(field);

		Cursor cursor = null;
		Transaction txn = null;
		int count = 0;
		try
		{	
			txn = environment.beginTransaction(null, null);
			cursor = pidx.getDbh().openCursor(txn, null);			
			DatabaseEntry foundKey = new DatabaseEntry();
			DatabaseEntry data     = new DatabaseEntry();
			
			/* we dont need to update the indexes here since we are just adding a field*/
			/* when we delete a field we need to delete asociated indexes if the index only
			 * has one field and it is the field being deleted
			 */
			while (cursor.getNext(foundKey, data, LockMode.RMW) == OperationStatus.SUCCESS)
			{	
				Entity e = entity_binding.entryToEntity(old_def,data);
				cursor.delete();
				e.setEntityDefinition(new_def);
				e.setAttribute(fieldname,default_value);
				entity_binding.entityToEntry(e, data);
				cursor.put(foundKey,data);
				count++;	
			}
			
			cursor.close();			
			txn.commit();
			
			/*update entity definition record */
			/*update entity def */
			redefine_entity_definition(old_def,new_def);
		}
		catch (DatabaseException de)
		{
			abortTxn(txn);
			logger.error("do_add_entity_field(String, FieldDefinition, Object) - Error accessing database." + de, de);
		}
		
		return count;
	}
		
	public int deleteEntityField(String entity, String fieldname)throws PersistenceException 
	{
		_store_locker.enterLockerThread();
		try{
			logger.debug("deleteEntityField(String, String) - DELETE ENTITY FIELD " + entity + "." + fieldname);
			return do_delete_entity_field(entity,fieldname);
	
			}catch(PersistenceException pe)
			{
				throw pe;
			}
			finally
			{
				_store_locker.exitLockerThread();	
			}
	}
			
	protected int do_delete_entity_field(String entity,String field_name) throws PersistenceException
	{
		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entity);
		EntityDefinition old_def = pidx.getEntityDefinition();
		FieldDefinition f = old_def.getField(field_name);
		if(pidx == null)
			throw new PersistenceException("DELETE ENTITY FIELD: ENTITY "+entity+" DOES NOT EXIST!");
		if(f == null)
			throw new PersistenceException("DELETE ENTITY FIELD: FIELD "+field_name+" DOES NOT EXIST IN ENTITY");
		
		List<BDBSecondaryIndex> sec_indexes = entity_secondary_indexes_as_list.get(entity);
		for(int i = 0; i < sec_indexes.size();i++)
		{
			BDBSecondaryIndex sec_idx = sec_indexes.get(i);
			if(sec_idx.invalidatedByFieldDelete(f))
				throw new PersistenceException("DELETE ENTITY FIELD: "+field_name+" has a dependent index( "+sec_idx.getName()+" ). Delete the index first.");
		}
		
		EntityDefinition new_def = old_def.clone();
		/* remove field from cloned def */
		ArrayList<FieldDefinition> fields =  new_def.getFields();
		f = null;
		int s = fields.size();
		for(int i = 0; i < s;i++)
		{
			f = fields.get(i);
			if(f.getName().equals(field_name))
			{
				fields.remove(i);
				logger.debug("do_delete_entity_field(String, String) - !!! REMOVING " + f.getName());
				break;
			}
		}

		/*rewrite the primary table */
		Cursor cursor 	= null;
		Transaction txn = null;
		int count 		= 0;
		try
		{			
			txn = environment.beginTransaction(null, null);
			cursor = pidx.getDbh().openCursor(txn, null);			
			DatabaseEntry foundKey = new DatabaseEntry();
			DatabaseEntry data     = new DatabaseEntry();
			
			while (cursor.getNext(foundKey, data, LockMode.RMW) == OperationStatus.SUCCESS)
			{			
				Entity e = entity_binding.entryToEntity(old_def,data);
				cursor.delete();
				e.setEntityDefinition(new_def);
				entity_binding.entityToEntry(e, data);
				cursor.put(foundKey,data);
				count++;
			}
			cursor.close();			
			txn.commit();
			/*update entity def */
			redefine_entity_definition(old_def,new_def);
			
			/* delete any dependent secondary indexes and free resources associated with it */
			/* however this will never be called since we are enforcing a guard on the index above.
			 * an exception is thrown if you try to delete a field which has dependent indexes */
			List<BDBSecondaryIndex> all_entity_sec_indexes = entity_secondary_indexes_as_list.get(entity);
			for(int i = 0; i < all_entity_sec_indexes.size();i++)
			{
				BDBSecondaryIndex s_idx = all_entity_sec_indexes.get(i);
				if(s_idx.invalidatedByFieldDelete(f))
				{
					/*remove from caches */
					all_entity_sec_indexes.remove(i);
					entity_secondary_indexes_as_map.get(entity).remove(s_idx.getName());	
					
					/*delete from disk*/
					s_idx.delete();
					delete_entity_index_from_db(entity, s_idx.getEntityIndex());
				}
			}
		}
		catch (DatabaseException de)
		{
			abortTxn(txn);
			logger.error("do_delete_entity_field(String, String) - Error accessing database." + de, de);

		}

		return count;
	}
	

	public FieldDefinition renameEntityField(String entity, String old_field_name,String new_field_name) throws PersistenceException
	{
		_store_locker.enterLockerThread();
		try{
			logger.debug("renameEntityField(String, String, String) - RENAME " + entity + " ENTITY FIELD " + old_field_name + " to " + new_field_name);
			return do_rename_entity_field(entity, old_field_name, new_field_name);
			
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitLockerThread();	
		}
		
	}

	protected FieldDefinition do_rename_entity_field(String entity, String old_field_name,String new_field_name) throws PersistenceException
	{
		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entity);
		if(pidx == null)
			throw new PersistenceException("RENAME ENTITY FIELD: ENTITY "+entity+" DOES NOT EXIST!");
		
		EntityDefinition old_def = pidx.getEntityDefinition();
		if(old_def.getField(old_field_name)== null)
			throw new PersistenceException("RENAME ENTITY FIELD: FIELD "+old_field_name+" DOES NOT EXIST IN ENTITY");
		if(old_def.getField(new_field_name)!= null)
			throw new PersistenceException("RENAME ENTITY FIELD: FIELD "+new_field_name+" ALREADY EXISTS IN ENTITY");
		
		EntityDefinition new_def 			= old_def.clone();
		ArrayList<FieldDefinition> fields 	= new_def.getFields();
		int s 				= fields.size();
		FieldDefinition f 	= null;
		for(int i = 0;i < s;i++)
		{
			f = fields.get(i);
			if(f.getName().equals(old_field_name))
			{
				f.setName(new_field_name);
				break;
			}
		}
		
		/* here we make sure the indexes update their file names on disk appropriately
		 * and force them to be rewritten in the entity_index_db with the new field names*/

		/*update entity def */
		redefine_entity_definition(old_def,new_def);	 

		List<BDBSecondaryIndex> all_indexes_for_entity = entity_secondary_indexes_as_list.get(entity);
		for(int i = 0; i < all_indexes_for_entity.size();i++)
		{
			
			BDBSecondaryIndex index 		= all_indexes_for_entity.get(i);
			/* this is in case it is using the name of the field for some reason 
			 * in the filename of its db
			 */
			index.fieldChangedName(old_field_name, new_field_name);			

			EntityIndex   idx 				= index.getEntityIndex();
			List<FieldDefinition> ifields	= index.getFields();
			for(FieldDefinition fd:ifields)
			{
				if(fd.getName().equals(old_field_name))
				{
					try{
						
						/*persist change..we need to do the delete with the idx still referring to the old
						 * fieldname. this is because we use getSearchBoth to delete it. see delete_entity_index_from_db
						 * for details.*/
						delete_entity_index_from_db(entity, idx);
						fd.setName(new_field_name);
						add_entity_index_to_db(entity, idx);
					}catch(DatabaseException e)
					{
						logger.error("do_rename_entity_field(String, String, String)", e);
						throw new PersistenceException("UNABLE TO COMMIT FIELD RENAME TO DEPENDEDNT INDEX");
					}
				}
			}
		}
		return f; 
	}

	
	
	public void addEntityIndex(String entity,String field_name,int index_type,String index_name, Map<String,String> attributes) throws PersistenceException
	{
		_store_locker.enterLockerThread();
		try{
			logger.debug("addEntityIndex(String, String, String, String, Map<String,Object>) - ADD ENTITY INDEX " + index_name + " ON " + entity + " OF TYPE " + index_type);
			do_add_entity_index(entity,new String[]{field_name},index_type,index_name,attributes);
	
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitLockerThread();	
		}		
	}
	
	public void addEntityIndex(String entity,String[] field_names,int index_type,String index_name, Map<String,String> attributes) throws PersistenceException
	{
		_store_locker.enterLockerThread();
		try{
			logger.debug("addEntityIndex(String, String[], String, String, Map<String,Object>) - ADD ENTITY INDEX " + index_name + " ON " + entity + " OF TYPE " + index_type);
			do_add_entity_index(entity,field_names,index_type,index_name,attributes);	
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitLockerThread();	
		}		
	}
	
	
	protected void do_add_entity_index(String entity,String[] field_names,int index_type,String index_name,Map<String,String>attributes) throws PersistenceException
	{
		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entity);
		if(pidx == null)
			throw new PersistenceException("ADD ENTITY INDEX: ENTITY "+entity+" DOES NOT EXIST!");

		if (entity_secondary_indexes_as_map.get(entity).get(index_name)!= null)
			throw new PersistenceException("ADD ENTITY INDEX: INDEX "+index_name+" ALREADY EXISTS IN ENTITY");

		/* this check is now done in the switch below */
		//EntityIndexDefinition eid = _entity_index_definition_manager.getDefinition(index_type);
		//if(eid == null)
		//	throw new PersistenceException("NO INDEX TYPE "+index_type+"\n"+_entity_index_definition_manager.getDefinitions());
		
		EntityDefinition def 	= pidx.getEntityDefinition();
		EntityIndex   eii 		= new EntityIndex(index_name,index_type);
		eii.setEntity(entity);
		for(int i = 0; i < field_names.length;i++)
		{
			FieldDefinition field 	= def.getField(field_names[i]);
			if (field==null)
				throw new PersistenceException("ADD ENTITY INDEX: FIELD "+field_names[i]+" DOES NOT EXIST IN ENTITY "+entity);
			eii.addField(field);
		}
		
		if (attributes!=null)
		{
			Iterator<String> keys 	 = attributes.keySet().iterator();
			while(keys.hasNext())
			{
				String att_name = keys.next();
				String att_value = attributes.get(att_name);
				eii.setAttribute(att_name, att_value);
			}
		}
		Class<?> c = null;
		
		BDBSecondaryIndex index = null;
		String classname = null;		
		switch(index_type)
		{
			case EntityIndex.TYPE_SIMPLE_SINGLE_FIELD_INDEX:
				classname = "com.pagesociety.bdb.index.SimpleSingleFieldIndex";
				break;
			case EntityIndex.TYPE_SIMPLE_MULTI_FIELD_INDEX:
				classname = "com.pagesociety.bdb.index.SimpleMultiFieldIndex";
				break;
			case EntityIndex.TYPE_ARRAY_MEMBERSHIP_INDEX:
				classname = "com.pagesociety.bdb.index.ArrayMembershipIndex";
				break;
			case EntityIndex.TYPE_MULTIFIELD_ARRAY_MEMBERSHIP_INDEX:
				classname = "com.pagesociety.bdb.index.MultiFieldArrayMembershipIndex";
				break;
			default:
				throw new PersistenceException("UNKNOWN INDEX TYPE 0x"+Integer.toHexString(index_type));
		}
		
		
		try {
			c = Class.forName(classname);
			index = (BDBSecondaryIndex)c.newInstance();
			try{
				index.validateFields(eii.getFields());
			}catch(PersistenceException pe)
			{
				throw new PersistenceException(pe.getMessage());
			}

		} catch (Exception ee) {
			logger.error("do_add_entity_index(String, String[], String, String, Map<String,Object>)", ee);
			throw new PersistenceException("FAILED INSTANTIATING INSTANCE OF INDEX "+eii.getEntityIndexType());
		}
		index.setup(pidx, eii);
	
		/*** AUTO POPULATE ON INDEX CREATION*******************************/
		/*** when the index is created fill it up with data from ptable ***/
		/*** maybe make this optional for the case where you just want to index ***/
		/*** data from now ***/
		long t1 = System.currentTimeMillis();
		Transaction txn = null;
		Cursor cursor   = null;
		try{
			txn = environment.beginTransaction(null, null);
			DatabaseEntry pkey = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			cursor = pidx.getDbh().openCursor(null, null);
			Entity e = null;
			int i = 0;
			while(cursor.getNext(pkey, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
			{	
				i++;
				e = entity_binding.entryToEntity(def, data);
				//System.out.println(index.getName()+">>>> AUTO POPULATE WITH"+e.getType()+" "+e.getId());
				index.insertIndexEntry(txn, e, pkey);
			}
			cursor.close();
			txn.commit();
			
		}catch(DatabaseException de)
		{
			close_cursors(cursor);
			abortTxn(txn);
			logger.error("do_add_entity_index(String, String[], String, String, Map<String,Object>)", de);
			throw new PersistenceException("ADD INDEX POPULATE FAILED FOR "+index.getName());
		}
		long t2 = System.currentTimeMillis();
		logger.debug("do_add_entity_index(String, String[], String, String, Map<String,Object>) - INITIAL POPULATE OF INDEX TOOK " + (t2 - t1) + " (ms)");
		/****END AUTO POPULATE**********************************************************/
		
		/* maintain runtime cache */
		entity_secondary_indexes_as_list.get(entity).add(index);
		entity_secondary_indexes_as_map.get(entity).put(eii.getName(),index);
		try
		{
			add_entity_index_to_db(entity, eii);
		}
		catch (DatabaseException e)
		{
			logger.error("do_add_entity_index(String, String[], String, String, Map<String,Object>)", e);
			throw new PersistenceException("Couldn't add index "+eii.getName()+" for entity "+entity);
		}		
	}
				
	public void deleteEntityIndex(String entity,String index_name) throws PersistenceException
	{
		_store_locker.enterLockerThread();
		try{
			logger.debug("deleteEntityIndex(String, String) - DELETE ENTITY INDEX ON " + entity + " OF TYPE " + index_name);
			do_delete_entity_index(entity,index_name);
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitLockerThread();	
		}		
	}
	
	protected void do_delete_entity_index(String entity,String index_name) throws PersistenceException
	{
		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entity);
		if(pidx == null)
			throw new PersistenceException("DELETE ENTITY INDEX: entity "+entity+" does not exist");
		if(entity_secondary_indexes_as_map.get(entity).get(index_name)== null)
			throw new PersistenceException("DELETE ENTITY INDEX: index "+index_name+" does not exists in "+entity);
		
		List<BDBSecondaryIndex> sec_idxs = entity_secondary_indexes_as_list.get(entity);
		for(int i = 0; i < sec_idxs.size();i++)
		{
			BDBSecondaryIndex s_idx = sec_idxs.get(i);
			if(s_idx.getName().equals(index_name))
			{
				try{
					s_idx.delete();
					delete_entity_index_from_db(entity, s_idx.getEntityIndex());
				}catch(DatabaseException e)
				{
					logger.error("do_delete_entity_index(String, String)", e);
					throw new PersistenceException("UNABLE TO DELETE INDEX "+index_name);
				}
				
				/* maintain runtime cache */
				entity_secondary_indexes_as_list.get(entity).remove(i);
				entity_secondary_indexes_as_map.get(entity).remove(s_idx.getName());
				break;
			}
		}
		
	}
	
	public void renameEntityIndex(String entity,String old_name,String new_name) throws PersistenceException
	{
		_store_locker.enterLockerThread();
		try{
			logger.debug("renameEntityIndex(String, String, String) - RENAME ENTITY INDEX ON " + entity + " FROM " + old_name + " TO " + new_name);
			do_rename_entity_index(entity,old_name,new_name);
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitLockerThread();	
		}		
	}
	
	protected void do_rename_entity_index(String entity,String old_name,String new_name) throws PersistenceException
	{
		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entity);
		if(pidx == null)
			throw new PersistenceException("RENAME ENTITY INDEX: entity "+entity+" does not exist");
		if(entity_secondary_indexes_as_map.get(entity).get(old_name)== null)
			throw new PersistenceException("RENAME ENTITY INDEX: index "+old_name+" does not exists in "+entity);
		if(entity_secondary_indexes_as_map.get(entity).get(new_name)!= null)
			throw new PersistenceException("RENAME ENTITY INDEX: index "+new_name+" already exists in "+entity);
		
		/*update runtime caches */
		List<BDBSecondaryIndex> sec_idxs = entity_secondary_indexes_as_list.get(entity);
		BDBSecondaryIndex s_idx;
		for(int i = 0; i < sec_idxs.size();i++)
		{
			s_idx = sec_idxs.get(i);
			if(s_idx.getName().equals(old_name))
			{
				EntityIndex idx = s_idx.getEntityIndex();
				try{
					delete_entity_index_from_db(entity, idx);
					s_idx = entity_secondary_indexes_as_map.get(entity).remove(old_name);				
					idx.setName(new_name);
					add_entity_index_to_db(entity, idx);
					entity_secondary_indexes_as_map.get(entity).put(new_name,s_idx);
				}catch(DatabaseException de)
				{
					logger.error("do_rename_entity_index(String, String, String)", de);
				}
				break;
			}
		}
	}


	private EntityIndex delete_entity_index_from_db(String entity,EntityIndex idx) throws DatabaseException
	{
		DatabaseEntry key  = new DatabaseEntry();
		FieldBinding.valueToEntry(Types.TYPE_STRING, entity, key);
		
		Transaction txn = null;
		try{
			DatabaseEntry data = entity_index_binding.objectToEntry(idx);
			txn = environment.beginTransaction(null, null);
			Cursor cursor 	   = entity_index_db.openCursor(txn, null);
		
			OperationStatus op_stat = cursor.getSearchBoth(key, data, LockMode.DEFAULT);
			if(op_stat == OperationStatus.NOTFOUND)
				throw new DatabaseException("NOTFOUND: COULDNT DELETE IDX "+entity+" "+idx.getName());
			op_stat = cursor.delete();
			cursor.close();
			txn.commit();
			if(op_stat != OperationStatus.SUCCESS)
				throw new DatabaseException("DELETE FAILED: COULDNT DELETE IDX "+entity+" "+idx.getName());
		}catch(DatabaseException dbe)
		{
			try{
				txn.abort();
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			dbe.printStackTrace();
			throw new DatabaseException("DATABASE FAILURE FOR DELETE ENTITY INDEX");
		}
		return idx; 
	}
	
	private void add_entity_index_to_db(String entity,EntityIndex idx) throws DatabaseException
	{
		DatabaseEntry key = new DatabaseEntry();
		FieldBinding.valueToEntry(Types.TYPE_STRING, entity,key);
		OperationStatus op_stat = entity_index_db.put(null, key, entity_index_binding.objectToEntry(idx));
		if(op_stat != OperationStatus.SUCCESS)
				throw new DatabaseException("");
	}
	
	
	/* set count to true if you want the number of records deleted returned */
	public int truncate(String entity_type,boolean count) throws PersistenceException
	{
		_store_locker.enterLockerThread();
		try{
			logger.debug("truncate(String, boolean) - TUNCATE ENTITY " + entity_type);
			return do_truncate_entity(entity_type,count);
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitLockerThread();	
		}		
		
	}
	
	private int do_truncate_entity(String entity,boolean count_records) throws PersistenceException
	{
		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entity);
		if(pidx == null)
			throw new PersistenceException("TRUNCATE ENTITY: entity "+entity+" does not exist");
			
			int n = -1;
			try {
				n = pidx.truncate(null,count_records);
				List<BDBSecondaryIndex> lsec_indexes = entity_secondary_indexes_as_list.get(entity);
				if(lsec_indexes != null)
				{
					BDBSecondaryIndex sidx = null;
					for(int i = 0; i < lsec_indexes.size();i++)
					{
						sidx = lsec_indexes.get(i);
						sidx.truncate(null);
					}
						
				}
				return n;
			} catch (DatabaseException e) {
				throw new PersistenceException("TRUNCATE FAILED FOR "+entity);
			}
			
	}
	
	private void move_entity_indexes(EntityDefinition old_entity_def,EntityDefinition new_entity_def) throws PersistenceException
	{
		String old_entity_name = old_entity_def.getName();
		String new_entity_name = new_entity_def.getName();
		
		List<BDBSecondaryIndex> lsec_indexes = entity_secondary_indexes_as_list.remove(old_entity_name);
		if(lsec_indexes != null)
		{
			for(int i = 0; i < lsec_indexes.size();i++)
			{
				try{
					BDBSecondaryIndex s_idx = lsec_indexes.get(i);
					s_idx.primaryIndexNameChanged(old_entity_name,new_entity_name);
					
					EntityIndex idx = s_idx.getEntityIndex();
					delete_entity_index_from_db(old_entity_name, idx);
					idx.setEntity(new_entity_name);
					add_entity_index_to_db(new_entity_name, idx);
				
				}catch(DatabaseException de)
				{
					logger.error("move_entity_indexes(EntityDefinition, EntityDefinition)", de);
					throw new PersistenceException("FAILED MOVING ENTITY INDEXES FROM "+old_entity_name+" TO "+new_entity_name);
				}
			}
			entity_secondary_indexes_as_list.put(new_entity_name,lsec_indexes);
		}
		
		Map<String,BDBSecondaryIndex> sec_indexes = entity_secondary_indexes_as_map.remove(old_entity_name);
		if(sec_indexes != null)
			entity_secondary_indexes_as_map.put(new_entity_name,sec_indexes);
		else
		{
			throw new PersistenceException("UNABLE TO GET INDEXES FROM MAP FOR ENTITY NAME "+old_entity_name);
		}

	}
		
	/****************QUERY***********************************/
	
	public Entity getEntityById(String type, long id) throws PersistenceException
	{
		_store_locker.enterAppThread();	
		BDBPrimaryIndex pi = entity_primary_indexes_as_map.get(type);
		Entity e;
		if(pi == null)
			throw new PersistenceException("ENTITY OF TYPE "+type+" DOES NOT EXIST");
		try{
			e =  pi.getById(null,id);
		}catch(DatabaseException de)
		{
			logger.error("getEntityById(String, long)", de);
			throw new PersistenceException("GET BY ID FAILED ");
		}
		finally
		{
			_store_locker.exitAppThread();
		}
		return e;
	}
	
	
	
	public QueryResult getEntitiesOrderedById(String type, int start, long number_of_records) throws PersistenceException
	{ 
		_store_locker.enterAppThread();
		try{
			return do_get_entities_ordered_by_id(type,start,number_of_records);
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitAppThread();
		}
	}
	
	protected QueryResult do_get_entities_ordered_by_id(String type, int start, long number_of_records) throws PersistenceException
	{
		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(type);
		if(pidx == null)
			throw new PersistenceException("GET ENTITIES BY ID: no such entity "+type);
		return pidx.getEntitiesOrderedById(type, start, number_of_records);
	}

	public QueryResult executeQuery(Query q) throws PersistenceException
	{ 
		_store_locker.enterAppThread();
		try{
			//return do_query_index(q);
			com.pagesociety.persistence.Query qq = (com.pagesociety.persistence.Query)q;
			return _query_manager.executeQuery(qq);
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		catch(ClassCastException cce)
		{
			cce.printStackTrace();
			throw new PersistenceException("Query "+q+" is not a bdb query");
		}
		finally
		{
			_store_locker.exitAppThread();
		}
	}
	
	public QueryResult executePSSqlQuery(String pssql) throws PersistenceException
	{ 
		_store_locker.enterAppThread();
		try{
			return _query_manager.executePSSqlQuery(pssql);
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitAppThread();
		}
	}
	

	
	public QueryResult getNextResults(Object nextresults_token) throws PersistenceException
	{ 
		_store_locker.enterAppThread();
		try{
			return do_get_next_results(nextresults_token);
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitAppThread();
		}
	}
	
	protected QueryResult do_get_next_results(Object nextresults_token) throws PersistenceException
	{
		throw new PersistenceException("no more do_get_next_reults");
	}

	public List<Object> getDistinctKeys(String entityname,String indexname) throws PersistenceException
	{
		_store_locker.enterAppThread();
		try{
			return do_get_distinct_keys(entityname,indexname);
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitAppThread();
		}	
	}
	
	
	private List<Object> do_get_distinct_keys(String entityname,String indexname) throws PersistenceException
	{
		BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(entityname);
		if(pidx == null)
			throw new PersistenceException("GET DISTINCT KEYS: no such entity "+entityname);
		
		BDBSecondaryIndex ei = entity_secondary_indexes_as_map.get(entityname).get(indexname);
		if(ei == null)
			throw new PersistenceException("GET DISTINCT KEYS: no such index "+indexname+" on entity "+entityname);
			
		return ei.getDistinctKeys();
	}
	
	public int count(Query q) throws PersistenceException
	{
		_store_locker.enterAppThread();
		try{
			//return do_query_index(q);
			com.pagesociety.persistence.Query qq = (com.pagesociety.persistence.Query)q;
			return _query_manager.executeCount(qq);
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		catch(ClassCastException cce)
		{
			cce.printStackTrace();
			throw new PersistenceException("Query "+q+" is not a bdb query");
		}
		finally
		{
			_store_locker.exitAppThread();
		}
	}
	

	/* we assume the entities are homogenous within the list*/
	public void fillReferenceFields(List<Entity> es) throws PersistenceException
	{
		_store_locker.enterAppThread();
		if(es.size() == 0)
			return;
		Entity e = es.get(0);
	    BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(e.getEntityDefinition().getName());
	    if(pidx == null)
	    	throw new PersistenceException("UNKNOWN ENTITY TYPE "+e.getEntityDefinition().getName());
		EntityDefinition ed = e.getEntityDefinition();
		int s = es.size();
		try{
			for(FieldDefinition fd:ed.getFields())
			{
				if(fd.getBaseType() != Types.TYPE_REFERENCE)
					continue;

				for (int i = 0;i < s;i++)
					do_fill_reference_field(es.get(i),fd);    

			}
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitAppThread();
		}		
	}
	
	public void fillReferenceField(List<Entity> es,String fieldname) throws PersistenceException
	{
		_store_locker.enterAppThread();
		if(es.size() == 0)
			return;
		Entity e = es.get(0);
	    BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(e.getEntityDefinition().getName());
	    if(pidx == null)
	    	throw new PersistenceException("UNKNOWN ENTITY TYPE "+e.getEntityDefinition().getName());
		EntityDefinition ed = e.getEntityDefinition();
		FieldDefinition fd = ed.getField(fieldname);
		if(fd == null)
			throw new PersistenceException("FIELD "+fieldname+" DOES NOT EXIST IN "+e.getType());
		if(fd.getBaseType() != Types.TYPE_REFERENCE)
			throw new PersistenceException("FIELD "+fieldname+" IS NOT A REFERENCE TYPE IN "+e.getType());
		int s = es.size();
		try{
			for (int i = 0;i < s;i++)
				do_fill_reference_field(es.get(i),fd);    
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitAppThread();
		}	
	}

	public void fillReferenceFields(Entity e) throws PersistenceException
	{
		_store_locker.enterAppThread();
	    BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(e.getEntityDefinition().getName());
	    if(pidx == null)
	    	throw new PersistenceException("UNKNOWN ENTITY TYPE "+e.getEntityDefinition().getName());
		EntityDefinition ed = e.getEntityDefinition();
	    try{
		    for (FieldDefinition f : ed.getFields())
		    {
		    	if(f.getBaseType() != Types.TYPE_REFERENCE)
		    		continue;
		    	do_fill_reference_field(e, f);
		    }  
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitAppThread();
		}		
	}
	 
	public void fillReferenceField(Entity e,String field_name) throws PersistenceException
	{
		_store_locker.enterAppThread();
	    BDBPrimaryIndex pidx = entity_primary_indexes_as_map.get(e.getEntityDefinition().getName());
	    if(pidx == null)
	    	throw new PersistenceException("UNKNOWN ENTITY TYPE "+e.getEntityDefinition().getName());
		FieldDefinition fd = e.getEntityDefinition().getField(field_name);
	    try{
		    do_fill_reference_field(e, fd);   
		}catch(PersistenceException pe)
		{
			throw pe;
		}
		finally
		{
			_store_locker.exitAppThread();
		}	
	}
	
	@SuppressWarnings("unchecked")
	protected void do_fill_reference_field(Entity e, FieldDefinition f) throws PersistenceException
	{
       if (f == null || f.getBaseType() != Types.TYPE_REFERENCE)
           throw new PersistenceException("REF FIELD CANT BE FILLED.IT IS EITHER DOES NOT EXIST IN ENTITY OR IS NOT A REFERNCE TYPE");


       BDBPrimaryIndex ref_pidx = entity_primary_indexes_as_map.get(f.getReferenceType());
       if (ref_pidx==null)
    	   throw new PersistenceException("STORE DOES NOT RECOGNIZE REFERENCE TYPE "+f.getReferenceType());
       try 
       {
	       if (f.isArray())
	       {
	           List<Entity> refs = (List<Entity>) e.getAttribute(f.getName());
	           if (refs == null || refs.size() == 0)
	               return;
	           
	           List<Entity> filled_refs = new ArrayList<Entity>();
	           for (int i=0; i<refs.size(); i++)
	           {
	              Entity r = refs.get(i);
	              if(r == null)
	            	  filled_refs.add(null);
	              else
	        	   filled_refs.add(ref_pidx.getById(null,r.getId()));
	           }
	            e.getAttributes().put(f.getName(), filled_refs);
	       }
	       else
	       {
	           Entity ref = (Entity) e.getAttribute(f.getName());
	           if (ref == null)
	               return;
	           e.getAttributes().put(f.getName(),ref_pidx.getById(null,ref.getId()));
	       }
		}catch(DatabaseException de)
		{
			logger.error("do_fill_reference_field(Entity, FieldDefinition)", de);
			throw new PersistenceException("FILL FAILED BECAUSE OF DB EXCEPTION");
		}
   }

	private void clean_query_cache(String entity_name)
	{
		_query_manager.cleanCache(entity_name);
	}
	
	private void calculate_query_cache_dependencies(EntityDefinition def)
	{
		_query_manager.calculateCacheDependencies(def);
	}
	
	private void remove_query_cache_dependencies(EntityDefinition def)
	{
		_query_manager.removeCacheDependencies(def);
	}
	
	private void close_cursors(Cursor... cursors)
	{
		try
		{
			for (Cursor c : cursors)
			{
				if (c != null)
				{
					c.close();
				}
			}
		}
		catch (DatabaseException e)
		{
			logger.error("close_cursors(Cursor)", e);
		}
	}


	private void abortTxn(Transaction txn)
	{
		if (txn != null)
		{
			try
			{
				txn.abort();
			}
			catch (DatabaseException e)
			{
				logger.error("abortTxn(Transaction)", e);
			}
			txn = null;
		}
	}
	public Environment getEnvironment()
	{
		return environment;
	}


}
