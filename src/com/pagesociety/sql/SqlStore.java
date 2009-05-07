package com.pagesociety.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pagesociety.bdb.index.EntityIndexDefinition;
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

public class SqlStore implements PersistentStore
{
	public static final String JDBC_CLASS_KEY 		   = "jdbc-class";
	public static final String JDBC_CONNECTION_URL_KEY = "jdbc-connection-url";
	public static final String SQL_USER_KEY 		   = "sql-user";
	public static final String SQL_PASSWORD_KEY 	   = "sql-password";
	//
	private String jdbcClass;
	private String jdbcConnectionUrl;
	private String sqlUser;
	private String sqlPassword;
	private Object jdbcClassInstance;
	private String database = "";

	public void init(Map<String, Object> config) throws PersistenceException
	{
		jdbcClass = (String) config.get(JDBC_CLASS_KEY);
		jdbcConnectionUrl = (String) config.get(JDBC_CONNECTION_URL_KEY);
		if (!jdbcConnectionUrl.endsWith("/"))
			jdbcConnectionUrl = jdbcConnectionUrl.concat("/");
		sqlUser = (String) config.get(SQL_USER_KEY);
		sqlPassword = (String) config.get(SQL_PASSWORD_KEY);
	}




	public void addEntityDefinition(EntityDefinition entity_def)
			throws PersistenceException
	{
		StringBuffer b = new StringBuffer();
		b.append("CREATE TABLE IF NOT EXISTS ");
		b.append(entity_def.getName());
		b.append("(\n");
		b.append("id INT,\n");
		for (int i = 0; i < entity_def.getFields().size(); i++)
		{
			FieldDefinition f = entity_def.getFields().get(i);
			if (!f.isArray())
			{
				b.append(get_sql_name(f));
				b.append(" ");
				b.append(get_sql_type(f));
				b.append(",\n");
			}
		}
		b.append("PRIMARY KEY (id)");
		b.append(")\n");
		do_update(b.toString());
		//
		for (int i = 0; i < entity_def.getFields().size(); i++)
		{
			FieldDefinition f = entity_def.getFields().get(i);
			// TODO look for many to many relationship
			if (f.isArray() && f.getBaseType() != Types.TYPE_REFERENCE)
			{
				String tableName = entity_def.getName() + get_sql_name(f);
				b = new StringBuffer();
				b.append("CREATE TABLE IF NOT EXISTS ");
				b.append(tableName);
				b.append(" (\n");
				b.append("pid INT,\n");
				b.append("value ");
				b.append(get_sql_type(f));
				b.append(",\n");
				if (f.getBaseType() == Types.TYPE_REFERENCE)
				{
					b.append("FOREIGN KEY (value) REFERENCES ");
					b.append(f.getReferenceType());
					b.append("(id),\n");
				}
				b.append("FOREIGN KEY (pid) REFERENCES ");
				b.append(entity_def.getName());
				b.append("(id)\n");
				b.append(")\n");
				do_update(b.toString());
			}
		}
	}
	
	public Entity saveEntity(Entity entity) throws PersistenceException
	{
		// TODO
		// this version of the SqlStore is only used in the context of a
		// migration from a bdb store to a sql store. so for now it doesn't
		// "handle" new entities or have any logic related to an insert vs an
		// update.
		if (entity.getId() == -1)
			throw new RuntimeException("SqlStore.saveEntity DONT HANDLE ENTITIES WITH ID=-1");
		//
		EntityDefinition entity_def =null;// entity.getEntityDefinition();
		StringBuffer b = new StringBuffer();
		b.append("INSERT INTO ");
		b.append(entity_def.getName());
		b.append(" (id,");
		StringBuffer b1 = new StringBuffer();
		for (int i = 0; i < entity_def.getFields().size(); i++)
		{
			FieldDefinition d = entity_def.getFields().get(i);
			if (!d.isArray())
			{
				b1.append(get_sql_name(d));
				b1.append(",");
			}
		}
		b.append(b1.substring(0, b1.length() - 1));
		b.append(")");
		b.append(" VALUES ");
		b.append("(");
		b.append(entity.getId());
		b.append(",");
		b1 = new StringBuffer();
		for (int i = 0; i < entity_def.getFields().size(); i++)
		{
			FieldDefinition d = entity_def.getFields().get(i);
			if (!d.isArray())
			{
				b1.append(get_db_value(entity.getAttribute(d.getName())));
				b1.append(",");
			}
		}
		b.append(b1.substring(0, b1.length() - 1));
		b.append(")");
		//
		Connection con = get_connection();
		Statement stmt;
		try
		{
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			stmt.executeUpdate(b.toString());
			ResultSet rs = stmt.getGeneratedKeys();
			while (rs.next())
				entity.setId(rs.getInt(1));
			stmt.close();
			con.close();
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + b);
			throw new PersistenceException("SQLException: " + e.getMessage());
		}
		//
		for (int i = 0; i < entity_def.getFields().size(); i++)
		{
			FieldDefinition d = entity_def.getFields().get(i);
			// TODO look for many to many relationship
			if (d.isArray() && d.getBaseType() != Types.TYPE_REFERENCE)
			{
				List<?> vals = (List<?>) entity.getAttribute(d.getName());
				if (vals == null || vals.isEmpty())
					continue;
				b = new StringBuffer();
				b.append("INSERT INTO ");
				b.append(entity_def.getName());
				b.append(get_sql_name(d));
				b.append(" VALUES ");
				for (int j = 0; j < vals.size(); j++)
				{
					Object o = vals.get(j);
					b.append("(");
					b.append(entity.getId());
					b.append(",");
					b.append(get_db_value(o));
					b.append(")");
					if (j != vals.size() - 1)
					{
						b.append(",");
					}
				}
				do_update(b.toString());
			}
		}
		return entity;
	}
	
	
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private static String get_db_value(Object val)
	{
		if (val == null)
		{
			return ("NULL");
		}
		if (val.getClass() == Boolean.class)
		{
			if (((Boolean) val).booleanValue())
			{
				return "\"yes\"";
			}
			else
			{
				return "\"no\"";
			}
		}
		if (val.getClass() == Integer.class || val.getClass() == Double.class)
		{
			return val.toString();
		}
		else if (val.getClass() == String.class)
		{
			return "\"" + fix_quotes(val.toString()) + "\"";
		}
		if (val.getClass() == Date.class)
		{
			return "\"" + dateFormat.format((Date) val) + "\"";
		}
		if (val.getClass() == Entity.class)
		{
			return Long.toString(((Entity) val).getId());
		}
		return null;
	}

	private static String fix_quotes(String s)
	{
		StringBuffer rs = new StringBuffer();
		if (s != null)
		{
			for (int i = 0; i < s.length(); i++)
			{
				char c = s.charAt(i);
				if ((c == 13) || ((c > 31) && (c < 128)) || c > 159)
				{
					char c1 = ' ';
					if (i > 0)
					{
						c1 = s.charAt(i - 1);
					}
					if ((c == '\'') && (c1 != '\\'))
					{
						rs.append("\\\'");
					}
					else
					{
						rs.append(c);
					}
				}
			}
		}
		return rs.toString();
	}

	private String get_sql_name(FieldDefinition d)
	{
		return "_" + d.getName().replace(' ', '_');
	}

	private String get_sql_type(FieldDefinition d)
	{
		switch (d.getBaseType())
		{
		case Types.TYPE_BOOLEAN:
			return "ENUM('yes','no')";
		case Types.TYPE_LONG:
			return "LONG";
		case Types.TYPE_INT:
			return "INT";
		case Types.TYPE_FLOAT:
			return "DOUBLE";
		// TODO varchar len
		case Types.TYPE_STRING:
			return "VARCHAR(255)";
		case Types.TYPE_TEXT:
			return "TEXT";
		case Types.TYPE_DATE:
			return "DATETIME";
		case Types.TYPE_REFERENCE:
			return "INT";
		}
		return null;
	}
	
	
	public void useDatabase(String db) throws PersistenceException
	{
		do_update("CREATE DATABASE IF NOT EXISTS " + db);
		database = db;
	}

	private Connection get_connection()
	{
		if (jdbcClassInstance == null)
		{
			try
			{
				jdbcClassInstance = Class.forName(jdbcClass).newInstance();
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		try
		{
			return DriverManager.getConnection(jdbcConnectionUrl + database, sqlUser, sqlPassword);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return null;
		}
	}

	private void do_update(String s) throws PersistenceException
	{
		Connection con = get_connection();
		Statement stmt;
		try
		{
			stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new PersistenceException("SQLException: " + e.getMessage());
		}
		try
		{
			if (s.startsWith("CREATE"))
				stmt.execute("SET FOREIGN_KEY_CHECKS=0");
			stmt.executeUpdate(s);
			if (s.startsWith("CREATE"))
				stmt.execute("SET FOREIGN_KEY_CHECKS=1");
			stmt.close();
			con.close();
		}
		catch (SQLException e)
		{
			System.out.println("SQLException: " + s);
			e.printStackTrace();
			throw new PersistenceException("SQLException: " + e.getMessage());
		}
	}


	public int addEntityField(String entity, FieldDefinition entity_field_def) throws PersistenceException
	{
		throw new PersistenceException("SqlStore.addEntityField NOT IMPLEMENTED");
	}

	public EntityIndex addEntityIndex(String entity, String field_name, int index_type,
			String index_name, Map<String, Object> attributes)
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.addEntityIndex NOT IMPLEMENTED");
	}

	public EntityIndex addEntityIndex(String entity, String[] field_names, int index_type,
			String index_name, Map<String, Object> attributes)
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.addEntityIndex NOT IMPLEMENTED");
	}

	public void addEntityRelationship(EntityRelationshipDefinition r)
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.addEntityRelationship NOT IMPLEMENTED");
	}

	public void close() throws PersistenceException
	{
		//
	}

	public int count(Query q)
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.count NOT IMPLEMENTED");
	}

	public void deleteEntity(Entity e) throws PersistenceException
	{
		throw new PersistenceException("SqlStore.deleteEntity NOT IMPLEMENTED");
	}

	public void deleteEntityDefinition(String entity_def_name)
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.deleteEntityDefinition NOT IMPLEMENTED");
	}

	public int deleteEntityField(String entity, String fieldname)
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.deleteEntityField NOT IMPLEMENTED");
	}

	public void deleteEntityIndex(String entity, String index_name)
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.deleteEntityIndex NOT IMPLEMENTED");
	}

	public QueryResult executeQuery(Query q) throws PersistenceException
	{
		throw new PersistenceException("SqlStore.executeQuery NOT IMPLEMENTED");
	}

	public void fillReferenceField(List<Entity> es, String fieldname)
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.fillReferenceField NOT IMPLEMENTED");
	}

	public void fillReferenceField(Entity e, String field_name)
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.fillReferenceField NOT IMPLEMENTED");
	}

	public void fillReferenceFields(List<Entity> es) throws PersistenceException
	{
		throw new PersistenceException("SqlStore.fillReferenceFields NOT IMPLEMENTED");
	}

	public void fillReferenceFields(Entity e) throws PersistenceException
	{
		throw new PersistenceException("SqlStore.fillReferenceFields NOT IMPLEMENTED");
	}

	public List<Object> getDistinctKeys(String entityname, String indexname)
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.getDistinctKeys NOT IMPLEMENTED");
	}

	

	public Entity getEntityById(String type, long id) throws PersistenceException
	{
		throw new PersistenceException("SqlStore.getEntityById NOT IMPLEMENTED");
	}

	public EntityDefinition getEntityDefinition(String entity_name)
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.getEntityDefinition NOT IMPLEMENTED");
	}

	public List<EntityDefinition> getEntityDefinitions() throws PersistenceException
	{
		throw new PersistenceException("SqlStore.getEntityDefinitions NOT IMPLEMENTED");
	}

	public EntityIndexDefinition getEntityIndexDefinition(String name)
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.getEntityIndexDefinition NOT IMPLEMENTED");
	}

	public List<EntityIndexDefinition> getEntityIndexDefinitions()
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.getEntityIndexDefinitions NOT IMPLEMENTED");
	}

	public List<EntityIndex> getEntityIndices(String entity) throws PersistenceException
	{
		throw new PersistenceException("SqlStore.getEntityIndices NOT IMPLEMENTED");
	}

	public List<EntityRelationshipDefinition> getEntityRelationships()
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.getEntityRelationships NOT IMPLEMENTED");
	}

	public QueryResult getNextResults(Object nextresults_token)
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.getNextResults NOT IMPLEMENTED");
	}

	

	public void renameEntityDefinition(String ename, String new_name)
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.renameEntityDefinition NOT IMPLEMENTED");
	}

	public FieldDefinition renameEntityField(String entity, String old_field_name,
			String new_field_name) throws PersistenceException
	{
		throw new PersistenceException("SqlStore.renameEntityField NOT IMPLEMENTED");
	}

	public void renameEntityIndex(String entity, String old_name, String new_name)
			throws PersistenceException
	{
		throw new PersistenceException("SqlStore.renameEntityIndex NOT IMPLEMENTED");
	}



	public int truncate(String entity_type, boolean count) throws PersistenceException
	{
		throw new PersistenceException("SqlStore.fillReferenceFields NOT IMPLEMENTED");
	}




	@Override
	public void checkpoint() throws PersistenceException {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void insertEntities(List<Entity> entities)
			throws PersistenceException {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void insertEntity(Entity e) throws PersistenceException {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void deleteBackup(String backup_identifier)
			throws PersistenceException {
		// TODO Auto-generated method stub
		
	}




	@Override
	public String doFullBackup() throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public String doIncrementalBackup(String backup_identifier)
			throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public String[] getBackupIdentifiers() throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public void restoreFromBackup(String backup_identifier)
			throws PersistenceException {
		// TODO Auto-generated method stub
		
	}




	@Override
	public boolean supportsFullBackup() throws PersistenceException {
		// TODO Auto-generated method stub
		return false;
	}




	@Override
	public boolean supportsIncrementalBackup() throws PersistenceException {
		// TODO Auto-generated method stub
		return false;
	}
	


	@Override
	public String createQueue(String name, int record_size,
			int num_records_in_extent) throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}
	public void deleteQueue(String name) throws PersistenceException{};
	public void enqueue(String queue_name,byte[] queue_item,boolean durable_commit) throws PersistenceException{};
	public byte[] dequeue(String queue_name,boolean durable_commit,boolean wait) throws PersistenceException{return null;}
	public List<String> listQueues() throws PersistenceException{return null;}




	



	@Override
	public void commitTransaction(int transaction_id)
			throws PersistenceException {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void rollbackTransaction(int transaction_id)
			throws PersistenceException {
		// TODO Auto-generated method stub
		
	}




	@Override
	public int startTransaction() throws PersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}




	@Override
	public void deleteEntity(int transaction_id, Entity e)
			throws PersistenceException {
		// TODO Auto-generated method stub
		
	}




	@Override
	public Entity getEntityById(int transaction_id, String entity, long id)
			throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public Entity saveEntity(int transaction_id, Entity e)
			throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}




	@Override
	public int count(int transaction_id, Query q) throws PersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}




	@Override
	public QueryResult executeQuery(int transaction_id, Query q)
			throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}






	@Override
	public int startTransaction(int parent_transaction_id)
			throws PersistenceException {
		// TODO Auto-generated method stub
		return 0;
	}




	@Override
	public void fillReferenceField(int transaction_id, List<Entity> es,
			String fieldname) throws PersistenceException {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void fillReferenceField(int transaction_id, Entity e,
			String field_name) throws PersistenceException {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void fillReferenceFields(int transaction_id, List<Entity> es)
			throws PersistenceException {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void fillReferenceFields(int transaction_id, Entity e)
			throws PersistenceException {
		// TODO Auto-generated method stub
		
	}




}
