package com.pagesociety.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
public class Entity implements Comparable<Entity>,java.io.Serializable
{
	private static final long serialVersionUID = 1L;

	private String _type;
	private long _id;
	private Map<String, Object> _attributes;
	private List<String> _dirty_attributes;
	public static final long UNDEFINED = -1;
	public static final String ID_ATTRIBUTE = "id";
	
//	private static final SimpleDateFormat date_format = new SimpleDateFormat("yyyy.MM.dd  HH:mm");

	/**
	 * Untyped. This is only used for object serialization.... unless we add
	 * something to Bean that will do private constructors.
	 * 
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
	protected Entity(String type)
	{
		this(type, UNDEFINED);
	}

	private Entity(String type, long id)
	{
		_type 		= type;
		_attributes = new HashMap<String,Object>();
		_dirty_attributes = new ArrayList<String>();
		setId(id);
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
		_attributes.put(ID_ATTRIBUTE, id);/* dont want to dirty it */
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

	public void setAttributes(Map<String,Object> o)
	{
		_attributes = o;
	}
	
	/**
	 * Links the attributes of this entity for the supplied entity. Note that
	 * this implies that changing either this entity or the supplied one will
	 * change both entities. This method does not copy attribute values.
	 * 
	 * @param e
	 *            The entity to link.
	 */
	public void copyAttributes(Entity e)
	{
		setId(e._id);
		_type = e._type;
		_attributes = e._attributes;
		_dirty_attributes = e._dirty_attributes;
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
	 * Sets a list of dirty attribute field names. Advanced or serialization use only! 
	 * 
	 * @return A list of field names
	 */
	public void setDirtyAttributes(List<String> attr)
	{
		_dirty_attributes = attr;
	}
	
	public void dirtyAllAttributes()
	{
		List<String> all_attributes = new ArrayList<String>();
		Iterator<String> i = _attributes.keySet().iterator();
		while(i.hasNext())
		{
			String a = i.next();
			if(!a.equals(ID_ATTRIBUTE))
				all_attributes.add(a);
		}
		_dirty_attributes = all_attributes;
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
		ret.append("ENTITY: ");
		ret.append(getType());
		ret.append(" ");
		ret.append(getId()+" ");

		Map<String,Object> att = getAttributes();
		Iterator<String> i = att.keySet().iterator();
		while(i.hasNext())
		{
			String att_name = i.next();
			Object o = getAttribute(att_name);
			if(o == null)
			{
				ret.append(att_name+": null ");
			}
			else if(o instanceof Entity)
			{
				Entity e = (Entity)o;
				ret.append(att_name+": {"+e.getType()+":"+e.getId()+"} ");

			}
			else if(o instanceof List)
			{
				List<?> ll = (List<?>)o;
				if(ll.size() == 0)
				{
					ret.append(att_name+": [] ");

				}
				else if(ll.get(0) instanceof Entity)
				{
					ret.append(att_name+": [");
					for(int j = 0;j < ll.size();j++)
					{
						Entity e = (Entity)ll.get(j);
						ret.append("{"+e.getType()+":"+e.getId()+"},");
					}
					ret.setLength(ret.length()-1);
					ret.append("] ");
				}
				else
				{
					ret.append(att_name+": "+o+" ");
				}
			}
			else
			{
				ret.append(att_name+": "+o+" ");
			}
		}
		//ret.append(getAttributes());
//		EntityDefinition def = getEntityDefinition();
//		List<FieldDefinition> fields = def.getFields();
//		for (int i = 0; i < fields.size(); i++)
//		{
//			FieldDefinition fd = fields.get(i);
//			Object val = _attributes.get(fd.getName());
//			String name = fd.getName();
//			if (val == null)
//			{
//				ret.append("\t\t" + name + " =  NULL");
//				continue;
//			}
//			switch (fd.getBaseType())
//			{
//			case Types.TYPE_REFERENCE:
//				if (fd.isArray())
//				{
//					ret.append("\t\t" + name + " = [ ");
//					List<Object> vals = (List<Object>) val;
//					for (int ii = 0; ii < vals.size(); ii++)
//					{
//						Entity ee = (Entity) vals.get(ii);
//						if(ee == null)
//							ret.append("null");
//						else
//							ret.append(ee.getType()+":"+ee.getId() + ",");
//					}
//					ret.setLength(ret.length() - 1);
//					ret.append(" ]");
//				}
//				else
//				{
//					Entity v = (Entity) val;
//					ret.append("\t\t" + name + " = " + fd.getReferenceType() + " " + v.getId());
//				}
//				break;
//			default:
//				ret.append("\t\t" + name + " = " + val);
//			}
//		}
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
		e.setId(UNDEFINED);
		e._attributes = (Map<String,Object>)((HashMap<String, Object>)_attributes).clone();
		//NOTE: we need to dirty all attributes here
		//so that if someone saved the clone
		//it wouldnt be populated with default
		//values. see BDBStore set_default_values
		//which is called by saveEntity
		e.dirtyAllAttributes(); 
		return e;
	}

	public boolean isLightReference()
	{
		return (_attributes.size() == 1);
	}
	
	
}
