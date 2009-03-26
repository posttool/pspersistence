package com.pagesociety.persistence;

import java.util.List;
import java.util.Map;

//touched
/*An abstract adapter class for receiving mouse events. The methods in this 
class are empty. This class exists as convenience for creating subsets of store interface. */ 
public abstract class PersistenceAdapter implements PersistentStore
{

	public void init(Map<String, Object> config) throws PersistenceException{};
	public void close() throws PersistenceException{};
	public void addEntityDefinition(EntityDefinition entity_def) throws PersistenceException{}
	public void deleteEntityDefinition(String entity_def_name)throws PersistenceException{}
	public void renameEntityDefinition(String old_name, String new_name)throws PersistenceException{}
	public EntityDefinition getEntityDefinition(String entity_name)throws PersistenceException{return null;}
	public List<EntityDefinition> getEntityDefinitions()throws PersistenceException{return null;}
	public int addEntityField(String entity, FieldDefinition entity_field_def) throws PersistenceException{return 0;}
	public int deleteEntityField(String entity, String fieldname)throws PersistenceException{return 0;}
	public FieldDefinition renameEntityField(String entity,String old_field_name, String new_field_name) throws PersistenceException{return null;}
	public List<EntityIndex> getEntityIndices(String entity)throws PersistenceException {return null;}
	public EntityIndex addEntityIndex(String entity, String field_name,int index_type, String index_name, Map<String, Object> attributes)throws PersistenceException {return null;}
	public EntityIndex addEntityIndex(String entity, String[] field_names,int index_type, String index_name, Map<String, Object> attributes)throws PersistenceException{return null;}
	public void deleteEntityIndex(String entity, String index_name)throws PersistenceException{}
	public void renameEntityIndex(String entity, String old_name, String new_name)throws PersistenceException{}
	public void addEntityRelationship(EntityRelationshipDefinition r)throws PersistenceException{}
	public List<EntityRelationshipDefinition> getEntityRelationships()throws PersistenceException{return null;}
	public Entity saveEntity(Entity e) throws PersistenceException{return null;}
	public void insertEntity(Entity e) throws PersistenceException{}
	public void insertEntities(List<Entity> entities) throws PersistenceException{}
	public Entity getEntityById(String entity, long id)throws PersistenceException{return null;}
	public void deleteEntity(Entity e) throws PersistenceException{}
	public int truncate(String entity_type, boolean count)throws PersistenceException{return 0;}
	public void fillReferenceFields(List<Entity> es) throws PersistenceException{}
	public void fillReferenceField(List<Entity> es, String fieldname)throws PersistenceException{}
	public void fillReferenceFields(Entity e) throws PersistenceException{}
	public void fillReferenceField(Entity e, String field_name)throws PersistenceException{}
	public QueryResult executeQuery(Query q) throws PersistenceException{return null;}
	public List<Object> getDistinctKeys(String entity, String index)throws PersistenceException{return null;}
	public int count(Query q) throws PersistenceException{return 0;}
	public void checkpoint() throws PersistenceException{}
	public boolean supportsFullBackup() throws PersistenceException{return false;}	
	public boolean supportsIncrementalBackup() throws PersistenceException{return false;}
	public String doFullBackup() throws PersistenceException{return null;}
	public String doIncrementalBackup(String backup_identifier) throws PersistenceException{return null;}
	public String[] getBackupIdentifiers() throws PersistenceException{return null;}
	public void restoreFromBackup(String backup_identifier) throws PersistenceException{}
	public void deleteBackup(String backup_identifier) throws PersistenceException{}

}