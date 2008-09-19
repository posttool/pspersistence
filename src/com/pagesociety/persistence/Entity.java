package com.pagesociety.persistence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * Represents an instance of an object to be stored or one that has been
 * restored. Each entity has an id, attribute values accessible by name, a
 * definition, as well as a simple notion of field dirtiness.
 * 
 * <h3>Id</h3>
 * <p>
 * Ids will be managed by the store. Store users will probably never set an id.
 * It is assumed that the id is set when an entity is inserted into the store or
 * retrieved by query or id.
 * </p>
 * 
 * <h3>Attributes</h3>
 * <p>
 * Entity attributes should be set using the method
 * <code>entity.setAttribute(String, Object)</code>. Attributes can be
 * retrieved using <code>entity.getAttribute(String)</code>.
 * </p>
 * 
 * <h3>Definition</h3>
 * <p>
 * The entity definition is analogous to a class definition. It will generally
 * be used to construct entity instances.
 * </p>
 * 
 * <h3>Dirty</h3>
 * <p>
 * When setAttribute is called, the target attribute is marked 'dirty'. Users
 * can get a list of dirty attributes using
 * <code>entity.getDirtyAttributes()</code>. If you want to set a field
 * without marking it dirty, use
 * <code>entity.getAttributes().set(String,Object)</code> to change attribute
 * values. This should only happen in internal store implementation.
 * </p>
 * 
 * @author Topher LaFata
 * @author David Karam
 * 
 * @see EntityDefinition
 * 
 */
public class Entity implements Comparable<Entity>
{
	private String _type;
	private long _id;
	private Map<String, Object> _attributes;
	private List<String> _dirty_attributes;
	private EntityDefinition _entity_definition;
	public static final long UNDEFINED = -1;
	private static final SimpleDateFormat date_format = new SimpleDateFormat("yyyy.MM.dd  HH:mm");

	/**
	 * Untyped.
	 * 
	 * @see #createInstance()
	 */
	public Entity()
	{
		this(null, UNDEFINED);
	}

	/**
	 * Constructs an entity from a definition
	 * 
	 * @param def
	 *            The entity definition.
	 */
	public Entity(EntityDefinition def)
	{
		this(def, UNDEFINED);
	}

	/**
	 * Constructs an entity with a definition and an id
	 * 
	 * @param def
	 *            The entity definition.
	 * @param id
	 *            The entity id.
	 */
	public Entity(EntityDefinition def, long id)
	{
		_id = id;
		_attributes = new HashMap<String, Object>();
		_entity_definition = def;
		if (def != null)
			_type = _entity_definition.getName();
		_dirty_attributes = new ArrayList<String>();
	}

	/**
	 * Returns the id of the entity (or UNDEFINED).
	 * 
	 * @return The id of the entity.
	 */
	public long getId()
	{
		return _id;
	}

	/**
	 * Sets the id of the entity. It is assumed that this method will only need
	 * to be called from the store implementation itself as an entity is either
	 * saved or retrieved.
	 * 
	 * @param id
	 *            The primary identitfier of the identity.
	 */
	public void setId(long id)
	{
		_id = id;
	}

	/**
	 * The type of an entity is the name of its definition.
	 * 
	 * @return The type of this entity: the name of its entity definition.
	 */
	public String getType()
	{
		return _type;
	}

	/**
	 * Sets the type of this entity. It is assumed that this method is only used
	 * by implementations of the PersistentStore. The type is highly coupled
	 * with the EntityDefinition.
	 * 
	 * @param type
	 *            The name of the entity definition.
	 */
	public void setType(String type)
	{
		_type = type;
	}

	/**
	 * Sets the definition of this entity. This method is assumed to be used
	 * only by implementations of the persistent store. Generally an entity is
	 * either constructed with a definition or the definition is used to create
	 * an instance of an entity.
	 * 
	 * @param def
	 *            The definition
	 * @see EntityDefinition#createInstance()
	 */
	public void setEntityDefinition(EntityDefinition def)
	{
		_entity_definition = def;
	}

	/**
	 * Returns the appropriate entity definition.
	 * 
	 * @return The appropriate entity definition.
	 */
	public EntityDefinition getEntityDefinition()
	{
		return _entity_definition;
	}

	/**
	 * Returns the value of a named attribute.
	 * 
	 * @param name
	 *            The name of a field defined by the entity definition.
	 * @return A object of the type specified by the field definition.
	 */
	public Object getAttribute(String name)
	{
		return _attributes.get(name);
	}

	/**
	 * Returns the underlying map behind the entity.
	 * 
	 * @return A Map of Objects with String keys corresponding to the fields
	 *         defined by the entity definition.
	 */
	public Map<String, Object> getAttributes()
	{
		return _attributes;
	}

	/**
	 * Links the attributes of this entity for the supplied entity. Note that
	 * this implies that changing either this entity or the supplied one will
	 * change both entities. This method does not copy attribute values.
	 * 
	 * @param e
	 *            The entity to link.
	 */
	public void setAttributes(Entity e)
	{
		_id = e._id;
		_type = e._type;
		_attributes = e._attributes;
		_dirty_attributes = e._dirty_attributes;
		_entity_definition = e._entity_definition;
	}

	/**
	 * Returns any attribute as a String.
	 * 
	 * @param name
	 *            The name of a field.
	 * @return The attribute value as a String.
	 */
	public String getAttributeAsString(String name)
	{
		Object o = _attributes.get(name);
		if (o == null)
			return "NULL";
		FieldDefinition f = _entity_definition.getField(name);
		switch (f.getBaseType())
		{
		case Types.TYPE_DATE:
			return date_format.format((Date) o);
		default:
			break;
		}
		return String.valueOf(o);
	}

	/**
	 * Sets the value of an attribute. Marks the field as 'dirty'.
	 * 
	 * @param name
	 *            The name of the field.
	 * @param value
	 *            The value of the attribute.
	 */
	public void setAttribute(String name, Object value)
	{
		_attributes.put(name, value);
		if (!_dirty_attributes.contains(name))
			_dirty_attributes.add(name);
	}

	/**
	 * Returns a list of dirty attribute field names.
	 * 
	 * @return A list of field names
	 */
	public List<String> getDirtyAttributes()
	{
		return _dirty_attributes;
	}

	/**
	 * Clears the list of dirty attributes.
	 */
	public void undirty()
	{
		_dirty_attributes.clear();
	}

	/**
	 * Removes the field name from the list.
	 * 
	 * @param name
	 *            The name of the field.
	 */
	public void undirty(String name)
	{
		_dirty_attributes.remove(name);
	}

	/**
	 * Is this entity dirty? ie Have any of the fields been set?
	 * 
	 * @return true if their are dirty fields, otherwise false.
	 */
	public boolean isDirty()
	{
		return (_dirty_attributes.size() != 0);
	}

	/**
	 * Checks if a specific attribute is dirty.
	 * 
	 * @param name
	 *            The name of the field.
	 * @return true if its dirty, otherwise false.
	 */
	public boolean isDirty(String name)
	{
		return _dirty_attributes.contains(name);
	}

	/**
	 * Returns the entity name and a description of its fields.
	 */
	@SuppressWarnings("unchecked")
	public String toString()
	{
		StringBuffer ret = new StringBuffer();
		ret.append("ENTITY ");
		ret.append(getType());
		ret.append(" ");
		ret.append(getId());
		EntityDefinition def = getEntityDefinition();
		List<FieldDefinition> fields = def.getFields();
		for (int i = 0; i < fields.size(); i++)
		{
			FieldDefinition fd = fields.get(i);
			Object val = _attributes.get(fd.getName());
			String name = fd.getName();
			if (val == null)
			{
				ret.append("\t\t" + name + " =  NULL");
				continue;
			}
			switch (fd.getBaseType())
			{
			case Types.TYPE_REFERENCE:
				if (fd.isArray())
				{
					ret.append("\t\t" + name + " = [ ");
					List<Object> vals = (List<Object>) val;
					for (int ii = 0; ii < vals.size(); ii++)
					{
						Entity ee = (Entity) vals.get(ii);
						if(ee == null)
							ret.append("null");
						else
							ret.append(ee.getType()+":"+ee.getId() + ",");
					}
					ret.setLength(ret.length() - 1);
					ret.append(" ]");
				}
				else
				{
					Entity v = (Entity) val;
					ret.append("\t\t" + name + " = " + fd.getReferenceType() + " " + v.getId());
				}
				break;
			default:
				ret.append("\t\t" + name + " = " + val);
			}
		}
		return ret.toString();
	}

	/**
	 * This method creates an untyped entity. It is only used in odd
	 * circumstances when the application guarantees that it will set the
	 * definition and type before considering the entity valid.
	 * 
	 * @return An untyped entity.
	 */
	public static Entity createInstance()
	{
		return new Entity();
	}

	/**
	 * This method says that 2 entities are equal if their type and id match. It
	 * does not compare the attributes or their values. This is the way it
	 * should stay! Many methods rely of this equality test.
	 */
	public boolean equals(Object o)
	{
		if(o == null)
			return false;
		Entity e = (Entity) o;
		return (_id == e._id && _type.equals(e._type));
	}
	
	private int hash = 42;/* any arbitray constant will do */
	public int hashCode()
	{
		hash =  31 * hash + _type.hashCode();
		hash =  31 * hash + (int)(_id ^ (_id >>> 32));
		return hash;
	}

	/**
	 * Sorted by id.
	 */
	public int compareTo(Entity e)
	{
		if(e == null)
			return -1;
		if(_type.equals(e._type))
			return (int) (_id - e._id);
		else
		{
			/* this is for the case when we have a group of entities that are
			 * non homogenous and we wish to use comparable interface on them
			 * for instance when building an array equality index on an untyped
			 * entity reference. in this case they will be sorted by type by id
			 * it mainly matters that they sort the same because the index key
			 * will be a compound one of the whole list. other wise if we had list
			 * A:15 and B:23....C:15 and B:23 would produce the same key which isn't
			 * right...see get_equality_key_for_array in SimpleSingleFieldIndex
			 */
			String s1 = _type+String.valueOf(_id);
			String s2 = e._type+String.valueOf(e._id);
			return s1.compareTo(s2);
		}
	}
	
	public Entity cloneShallow()
	{
		Entity e = new Entity();
		e._type = _type;
		e._id = _id;
		e._attributes = _attributes;
		e._dirty_attributes = _dirty_attributes;
		e._entity_definition = _entity_definition;
		return e;
	}
}
