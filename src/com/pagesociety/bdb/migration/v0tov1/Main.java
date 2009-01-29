package com.pagesociety.bdb.migration.v0tov1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.pagesociety.bdb.BDBConstants;
import com.pagesociety.bdb.binding.EntityDefinitionBinding;
import com.pagesociety.bdb.binding.FieldBinding;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.pagesociety.persistence.Types;
import com.sleepycat.db.Cursor;
import com.sleepycat.db.Database;
import com.sleepycat.db.DatabaseConfig;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DatabaseType;
import com.sleepycat.db.Environment;
import com.sleepycat.db.EnvironmentConfig;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.Transaction;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		if(args.length < 1)
			ERROR("usage: com.pagesociety.bdb.migration.MigrateV0ToV1 <path_to_base_env>");

		String path_to_base_env = args[0];
		File f = new File(path_to_base_env);
		if(!f.exists() || !f.isDirectory())
			ERROR("INVALID ENV BASE PATH "+path_to_base_env);
		
		check_version_file(path_to_base_env);
		File[] bdb_envs = f.listFiles();
		for(int i = 0;i < bdb_envs.length;i++)
		{
			File bdb_env = bdb_envs[i];
			if(bdb_env.isDirectory())
				migrate_entity_definitions(bdb_env);
		}
		update_version_file(path_to_base_env);
	}
	
	
	public static void migrate_entity_definitions(File bdb_env)
	{
		System.out.println("MIGRATING ENTITY DEFINITIONS");
		Environment env    = open_environment(bdb_env);
		DatabaseConfig cfg = get_primary_db_config_btree();
		try{
			Database entity_def_db = env.openDatabase(null, BDBConstants.ENTITY_DEFINITION_DB_NAME, null, cfg);
			List<EntityDefinition> defs = new ArrayList<EntityDefinition>();
			
			//get old entity defs
			EntityDefinitionBindingV0 def_v0 = new EntityDefinitionBindingV0();
			EntityDefinitionBindingV1 def_v1 = new EntityDefinitionBindingV1();
			Cursor cursor = entity_def_db.openCursor(null, null);
			DatabaseEntry key = new DatabaseEntry();
			DatabaseEntry data = new DatabaseEntry();
			while (cursor.getNext(key, data, LockMode.DEFAULT) == OperationStatus.SUCCESS)
			{
				EntityDefinition e = (EntityDefinition) def_v0.entryToObject(data);
				defs.add(e);
			}
			close_cursors(cursor);
			

			//delete old format entity defs
			for(int i = 0;i < defs.size();i++)
			{
				String ename = defs.get(i).getName();
				OperationStatus op_status = null;
				Transaction txn = env.beginTransaction(null, null);
				key = new DatabaseEntry();
				FieldBinding.valueToEntry(Types.TYPE_STRING, ename, key);
				op_status = entity_def_db.delete(txn, key);
				if(op_status != OperationStatus.SUCCESS)
					throw new PersistenceException("Failed Saving Entity Definition: "+ename);
				txn.commit();
			}
			
			//insert new format entity defs
			for(int i = 0;i < defs.size();i++)
			{
				EntityDefinition entity_def = defs.get(i);
				String ename              = entity_def.getName();
				System.out.println("\tMIGRATING "+ename);
				OperationStatus op_status = null;
				Transaction   txn = env.beginTransaction(null, null);
				key = new DatabaseEntry();
				data = new DatabaseEntry();

				FieldBinding.valueToEntry(Types.TYPE_STRING, ename, key);
				def_v1.objectToEntry(entity_def, data);		
				op_status = entity_def_db.put(txn, key, data);
				if(op_status != OperationStatus.SUCCESS)
					throw new PersistenceException("Failed Saving Entity Definition: "+ename);
				txn.commit();
			}

			entity_def_db.sync();
			entity_def_db.close();
			env.checkpoint(null);
			env.close();
			System.out.println("ENTITY DEFINITION MIGRATION COMPLETE.");
		}catch (Exception e){	
			ERROR("UNABLE TO OPEN ENTITY DEFINITION DB "+BDBConstants.ENTITY_DEFINITION_DB_NAME);
		}				
	}
	
	
	public static void check_version_file(String base_env_path)
	{
		System.out.println("CHECKING STORE VERSION");
		File f = new File(base_env_path+File.separator+BDBConstants.STORE_VERSION_FILENAME);
		if(f.exists())
		{	
			try{
				FileInputStream fis = new FileInputStream(f);
				int major_version = fis.read();
				int minor_version = fis.read();
				fis.close();
				if(major_version >= 1)
					System.out.println("STORE IS ALREADY AT VERSION "+major_version+"."+minor_version);
				System.exit(0);
			}catch(Exception e)
			{
				e.printStackTrace();
				ERROR("PROBLEM READING EXISTING VERSION FILE");
			}
		}
	}
	
	public static void update_version_file(String base_env_path)
	{
		System.out.println("UPDATING STORE VERSION FILE");
		File f = new File(base_env_path+File.separator+BDBConstants.STORE_VERSION_FILENAME);
		if(f.exists())
			f.delete();
		try{
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(1);
			fos.write(0);
			fos.close();
		}catch(Exception e)
		{
			e.printStackTrace();
			ERROR("FAILED WRITING VERSION FILE");
		}
		System.out.println("UPDATED STORE DATA FILES TO 1.0");
	}


	private static Environment open_environment(File f)
	{
		if(!f.exists() || !f.isDirectory())
			ERROR("INVALID BDB ENV PATH "+f.getAbsolutePath());
		EnvironmentConfig cfg = get_migration_env_config();
		
		Environment e = null;
		try{
			e = new Environment(f, cfg);
		}catch(Exception ee)
		{
			ee.printStackTrace();
			ERROR("OPEN ENVIRONMENT FAILED");
		}
		return e;
	}
	
	private static void ERROR(String message)
	{
		System.err.println(message);
		System.exit(0);
	}
	
	private static  EnvironmentConfig get_migration_env_config()
	{
		EnvironmentConfig env_cfg = new EnvironmentConfig();
		env_cfg.setAllowCreate(false);
		env_cfg.setRunRecovery(false);
		env_cfg.setTransactional(true);
		env_cfg.setErrorStream(System.err);		
		//1 megabytes = 1 048 576 bytes
		env_cfg.setCacheSize(1048576 * 500);
		// we need enough transactions for the number of 
		// simultaneous threads per environment
		env_cfg.setTxnMaxActive(1);
		// locks
		env_cfg.setMaxLocks(200);
		env_cfg.setMaxLockObjects(200);
		env_cfg.setMaxLockers(20);

		return env_cfg;
	}

	private static DatabaseConfig get_primary_db_config_btree()
	{	
		DatabaseConfig cfg = new DatabaseConfig();
		cfg.setType(DatabaseType.BTREE);
		cfg.setAllowCreate(false);
		cfg.setTransactional(true);
		cfg.setReadUncommitted(true);
		return cfg;
	}	
	
	private static void close_cursors(Cursor... cursors)
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
			e.printStackTrace();
			ERROR("BARFED ON CLOSING CURSORS");
		}
	}
}
