package com.pagesociety.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * <p>
 * The root interface of the persistence package. The store specifies methods
 * for schema definition and evolution, saving and deleting objects, making
 * queries, as well as maintaining graph relationships and lazy object graph
 * expansion. It is defined without regards to an underlying storage mechanism.
 * A store implementation can be based on SQL, memory cached data, flat files,
 * b-trees, other object stores, etc.
 * </p>
 * 
 * <h3>Entity Definitions</h3>
 * <p>
 * The store is required to maintain detailed object/field meta data. This is
 * represented by <code>EntityDefinition</code>. Entity definitions indicate
 * what kind of entities can be saved in the store. Calls to the following
 * methods modify the store entity definitions: <code>addEntityDefintion</code>,
 * <code>deleteEntityDefintion</code>, <code>renameEntityDefinition</code>,
 * <code>addEntityField</code>, <code>deleteEntityField</code>,
 * <code>renameEntityField</code>.
 * </p>
 * 
 * <h3>Indices</h3>
 * <p>
 * Many storage mechanisms use entity field value indexing to simplify data
 * retrieval. Most SQL implementations, for example, automatically create an
 * index for each field of a table. Implementations of the store may choose to
 * define and expose entity index definitions. These definitions are defined by
 * the store implementation and retrieved by calling
 * <code>getEntityIndexDefinitions</code>. Store users can add entity index
 * instances of type(s) defined by the store.
 * </p>
 * 
 * <h3>Relationships</h3>
 * <p>
 * In data modeling, the cardinality of one data table with respect to another
 * data table is a critical aspect of the design. Relationships between entity
 * fields define how one field links to another. For example, consider the
 * following entity definition: Node has a field parent of its own type and a
 * field children, an array of its type. The relationship is between parent and
 * child, one-to-many. Entity fields can be related as: one-to-one, one-to-many,
 * many-to-one, many-to-many.
 * </p>
 * 
 * <h3>Saving and Deleting Entities</h3>
 * <p>
 * Once entity definitions (as well as optional indices and relationships) have
 * been added to the store, the store is capable of saving and deleting entity
 * instances.
 * </p>
 * 
 * <h3>Object Graph Expansion</h3>
 * <p>
 * Reads from the store via <code>executeQuery</code> or
 * <code>getEntityById</code> do not expect that the object graph to be
 * filled. This means that references to other entities should not be retrieved
 * by default. The purpose for this is to quantify the reads from the store. It
 * requires the store user to call <code>fillReferenceFields</code> to expand
 * references.
 * </p>
 * 
 * <h3>Queries</h3>
 * <p>
 * The store assumes that all queries return homogeneous lists of entities. The
 * store defines a method to execute a query, get results in pages, get distinct
 * keys for a index and count the results of a query. Because the there are so
 * many approaches to querying data stores, there is no formal query language
 * specified by the persistence package. This allows the extent of the query
 * possibilities to vary between implementations.
 * </p>
 * 
 * 
 * @author Topher LaFata
 * @author David Karam
 * 
 */
public interface PersistentStore
{
	/**
	 * The store must be initialized with configuration parameters defined by
	 * the implementation. For SQL, this could indicate the connection
	 * information. For a flat file, it might indicate a base path of the data
	 * directory. The configuration HashMap has no formal rules.
	 * 
	 * @param config
	 *            Name/value configuration parameters
	 * @throws PersistenceException
	 *             If the config parameters don't meet the requiremens of the
	 *             store initialization.
	 */
	public abstract void init(HashMap<Object, Object> config) throws PersistenceException;

	/**
	 * Ends the life cycle of the store.
	 * 
	 * @throws PersistenceException
	 *             If the store does not close or shutdown properly
	 */
	public abstract void close() throws PersistenceException;

	/**
	 * Adds a new entity definition to the store.
	 * 
	 * @param entity_def
	 *            The new <code>EntityDefintion</code>.
	 * @throws PersistenceException
	 *             If the store is unable to add the entity definition. This
	 *             might occur because a definition by that name already exists.
	 * @see EntityDefinition
	 */
	public abstract void addEntityDefinition(EntityDefinition entity_def)
			throws PersistenceException;

	/**
	 * Deletes an entity definition from the store. This implies that all
	 * entities of the provided type will be deleted.
	 * 
	 * @param entity_def_name
	 *            The name of the entity definition to delete.
	 * @throws PersistenceException
	 *             If the entity definition does not exist or the store fails to
	 *             delete the entity definition.
	 */
	public abstract void deleteEntityDefinition(String entity_def_name)
			throws PersistenceException;

	/**
	 * Renames and entity definition.
	 * 
	 * @param old_name
	 *            The name of the entity definition.
	 * @param new_name
	 *            The new name.
	 * @throws PersistenceException
	 */
	public abstract void renameEntityDefinition(String old_name, String new_name)
			throws PersistenceException;

	/**
	 * Returns an entity definition by name.
	 * 
	 * @param entity_name
	 *            The name of the entity.
	 * @return The definition for the requested entity
	 * @throws PersistenceException
	 *             If the entity doesn't exist
	 * @see EntityDefinition
	 */
	public abstract EntityDefinition getEntityDefinition(String entity_name)
			throws PersistenceException;

	/**
	 * Returns all of the entity definitions.
	 * 
	 * @return All entity definitions
	 * @throws PersistenceException
	 */
	public abstract List<EntityDefinition> getEntityDefinitions()
			throws PersistenceException;

	/**
	 * Adds a field to the specified entity.
	 * 
	 * @param entity
	 *            The name of the entity that will get the new field.
	 * @param entity_field_def
	 *            The definition of the new field.
	 * @param default_value
	 *            The default value of the field if unspecified.
	 * @return The number of records affected by the alteration.
	 * @throws PersistenceException
	 * @see FieldDefinition
	 */
	public abstract int addEntityField(String entity, FieldDefinition entity_field_def,
			Object default_value) throws PersistenceException;

	/**
	 * Deletes a field from the specified entity
	 * 
	 * @param entity
	 *            The name of the entity that contains the field.
	 * @param fieldname
	 *            The name of the field to delete.
	 * @return The number of rows affected by the alteration.
	 * @throws PersistenceException
	 */
	public abstract int deleteEntityField(String entity, String fieldname)
			throws PersistenceException;

	/**
	 * Renames a field.
	 * 
	 * @param entity
	 *            The name of the entity that contains the field.
	 * @param old_field_name
	 *            The name of the field to rename.
	 * @param new_field_name
	 *            The new name.
	 * @return The updated field definition
	 * @throws PersistenceException
	 * @see FieldDefinition
	 */
	public abstract FieldDefinition renameEntityField(String entity,
			String old_field_name, String new_field_name) throws PersistenceException;

	/**
	 * Returns entity indices associated with an entity. An
	 * <code>EntityIndex</code> is associated with an entity by calling
	 * <code>addEntityIndex</code>.
	 * 
	 * @param entity
	 *            The name of an entity definition
	 * @return The indices associated with the entity definition
	 * @throws PersistenceException
	 *             If the entity name does not exist.
	 * @see #addEntityIndex(String, String, String, String, Map)
	 * @see #addEntityIndex(String, String[], String, String, Map)
	 */
	public abstract List<EntityIndex> getEntityIndices(String entity)
			throws PersistenceException;

	/**
	 * Adds an index to the store. This add signature indexes one field of an
	 * entity.
	 * 
	 * @param entity
	 *            The name of the entity definition to be indexed.
	 * @param field_name
	 *            The name of the field within the entity to index.
	 * @param index_type
	 *            The type of index (an entity index definition).
	 * @param index_name
	 *            The name of the index.
	 * @param attributes
	 *            Optional configuration attributes.
	 * @throws PersistenceException
	 * @see #addEntityIndex(String, String[], String, String, Map)
	 */
	public abstract void addEntityIndex(String entity, String field_name,
			int index_type, String index_name, Map<String, String> attributes)
			throws PersistenceException;

	/**
	 * Adds an index with multiple fields.
	 * 
	 * @param entity
	 *            The name of the entity definition to be indexed.
	 * @param field_names
	 *            The name of the fields within the entity to index.
	 * @param index_type
	 *            The type of index (an entity index definition).
	 * @param index_name
	 *            The name of the index.
	 * @param attributes
	 *            Optional configuration attributes.
	 * @throws PersistenceException
	 * @see #addEntityIndex(String, String, String, String, Map)
	 */
	public abstract void addEntityIndex(String entity, String[] field_names,
			int index_type, String index_name, Map<String, String> attributes)
			throws PersistenceException;

	/**
	 * Deletes an index from the store.
	 * 
	 * @param entity
	 *            The name of the parent entity definition.
	 * @param index_name
	 *            The name of the index.
	 * @throws PersistenceException
	 */
	public abstract void deleteEntityIndex(String entity, String index_name)
			throws PersistenceException;

	/**
	 * Renames an index.
	 * 
	 * @param entity
	 *            The name of the parent entity definition.
	 * @param old_name
	 *            The name of the index.
	 * @param new_name
	 *            The new name for the index.
	 * @throws PersistenceException
	 */
	public abstract void renameEntityIndex(String entity, String old_name, String new_name)
			throws PersistenceException;

	/**
	 * Adds an entity relationship constraint to the store. The relationship
	 * constraint enforces object graph integrity.
	 * 
	 * @param r
	 *            The relationship.
	 * @throws PersistenceException
	 *             If the entities don't exist, if the fields don't exist, or if
	 *             the relationship is impossible.
	 * @see EntityRelationshipDefinition
	 */
	public abstract void addEntityRelationship(EntityRelationshipDefinition r)
			throws PersistenceException;

	/**
	 * Returns a list of entity relationships registered with the store.
	 * 
	 * @return Entity relationships.
	 * @throws PersistenceException
	 */
	public abstract List<EntityRelationshipDefinition> getEntityRelationships()
			throws PersistenceException;

	/**
	 * Saves an entity. If <code>entity.id == Entity.UNDEFINED</code>, it
	 * should be inserted into the store, and a unique id should be assigned to
	 * it. The returned entity should be populated by the store. If the entity
	 * does have an id, then the store should assume that the user is attempting
	 * to update or overwrite an existing record.
	 * 
	 * @param e
	 *            The entity to save.
	 * @return The saved entity.
	 * @throws PersistenceException
	 * @see Entity
	 */
	public abstract Entity saveEntity(Entity e) throws PersistenceException;

	/**
	 * Restores an entity by its id. If the id does not exist the method returns
	 * null.
	 * 
	 * @param entity
	 *            The name of an entity definition.
	 * @param id
	 *            The id of the entity.
	 * @return The populated entity or null if the id is invalid.
	 * @throws PersistenceException
	 */
	public abstract Entity getEntityById(String entity, long id)
			throws PersistenceException;

	/**
	 * Deletes an entity record from the store. This method is not recursive.
	 * The programmer must delete contained references explicitly, unless the
	 * contained reference is part of a relationship in which case the other
	 * side of the relationship will be managed automatically by the store.
	 * 
	 * @param e
	 *            The entity to delete.
	 * @throws PersistenceException
	 */
	public abstract void deleteEntity(Entity e) throws PersistenceException;

	/**
	 * Truncates or deletes all records from an object store of type
	 * entity_type. Set count to true if you want the number of records deleted
	 * returned.
	 * 
	 * @param entity_type
	 *            The name of an entity definition.
	 * @param count
	 *            Set to true if you want the delete count returned.
	 * @return 0 or the number of records deleted
	 * @throws PersistenceException
	 */
	public abstract int truncate(String entity_type, boolean count)
			throws PersistenceException;

	/**
	 * Fill all of the references within a list of entities.
	 * 
	 * @param es
	 *            The list of entities.
	 * @throws PersistenceException
	 * @see #fillReferenceField(Entity, String)
	 */
	public abstract void fillReferenceFields(List<Entity> es) throws PersistenceException;

	/**
	 * Fills the named reference within a list of entities.
	 * 
	 * @param es
	 *            The list of entities.
	 * @param fieldname
	 *            The name of the reference field
	 * @throws PersistenceException
	 *             If the entities or reference field name is invalid.
	 * @see #fillReferenceField(Entity, String)
	 */
	public abstract void fillReferenceField(List<Entity> es, String fieldname)
			throws PersistenceException;

	/**
	 * Fill all of the references within a single entity.
	 * 
	 * @param e
	 *            The entities.
	 * @throws PersistenceException
	 * @see #fillReferenceField(Entity, String)
	 */
	public abstract void fillReferenceFields(Entity e) throws PersistenceException;

	/**
	 * Fills the values of a reference field within an entity. By default,
	 * reference fields (references to other entity types or arrays of
	 * references) are not restored when executing a query or calling
	 * <code>getById</code>. Use this method afterwards to restore the
	 * reference field values.
	 * 
	 * @param e
	 *            The entity.
	 * @param field_name
	 *            The name of a reference field to fill.
	 * @throws PersistenceException
	 *             If the field is not a valid reference field.
	 * @see FieldDefinition
	 * @see Types#TYPE_REFERENCE
	 */
	public abstract void fillReferenceField(Entity e, String field_name)
			throws PersistenceException;

	/**
	 * Executes a query and return the results.
	 * 
	 * @param q
	 *            The query
	 * @return The query result which includes a list of entities.
	 * @throws PersistenceException
	 * @see QueryResult
	 */
	public abstract QueryResult executeQuery(Query q) throws PersistenceException;

	/**
	 * Returns the next page of entities. The next results token must be
	 * supplied by the QueryResult for this to work.
	 * 
	 * @param next_results_token
	 * @return The query result including the list of entities.
	 * @throws PersistenceException
	 * @see QueryResult#getNextResultsToken()
	 */
	public abstract QueryResult getNextResults(Object next_results_token)
			throws PersistenceException;

	/**
	 * Returns the distinct keys for any index.
	 * 
	 * @param entity
	 *            The name of the entity definition
	 * @param index
	 *            The name of the index.
	 * @return A list of distinct values for the specified index.
	 * @throws PersistenceException
	 */
	public abstract List<Object> getDistinctKeys(String entity, String index)
			throws PersistenceException;

	/**
	 * Returns the number of records for any query.
	 * 
	 * @param q
	 *            The query
	 * @return The number of records the query would return.
	 * @throws PersistenceException
	 */
	public abstract int count(Query q) throws PersistenceException;
}