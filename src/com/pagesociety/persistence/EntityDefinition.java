package com.pagesociety.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * <p>
 * Defines an entity type including its name and fields. Entity definitions
 * indicate what kind of entities can be saved in the store. The following
 * example demonstrates the creation of an entity definition. Generally entity
 * definitions will be managed by the implementation of the persistent store.
 * </p>
 *
 * <code><pre>
 * EntityDefinition poll_def = new EntityDefinition(&quot;Poll&quot;);
 * poll_def.addField(new FieldDefinition(&quot;date created&quot;, Types.TYPE_DATE));
 * poll_def.addField(new FieldDefinition(&quot;title&quot;, Types.TYPE_STRING));
 * poll_def.addField(new FieldDefinition(&quot;description&quot;, Types.TYPE_STRING));
 * poll_def.addField(new FieldDefinition(&quot;resources&quot;, Types.TYPE_STRING | Types.TYPE_ARRAY));
 * poll_def.addField(new FieldDefinition(&quot;flags&quot;, Types.TYPE_INTG | Types.TYPE_ARRAY));
 * </pre></code>
 *
 * <p>
 * Entity definitions are also the most convenient was to create a new entity.
 * For example:
 * </p>
 *
 * <code><pre>
 * Entity poll_instance = poll_def.createInstance();
 * poll_instance.setAttribute(&quot;title&quot;, &quot;my poll&quot;);
 * store.save(poll_instance);
 * </pre></code>
 *
 * @author Topher LaFata
 * @author David Karam
 *
 * @see FieldDefinition
 * @see Types
 */
public class EntityDefinition implements java.io.Serializable
{
	private String _name;
	private ArrayList<FieldDefinition> _fields;//all fields for this entity
	private ArrayList<FieldDefinition> _reference_fields;//cache of reference fields for this entity
	private HashMap<String, FieldDefinition> _fields_cache = new HashMap<String, FieldDefinition>();

	/**
	 * Constructs a new entity definition with a name and no fields.
	 *
	 * @param name
	 *            The name of the entity definition.
	 */
	public EntityDefinition(String name)
	{
		_name = name;
		_fields = new ArrayList<FieldDefinition>();
		_reference_fields = new ArrayList<FieldDefinition>();
	}

	/**
	 * Adds a field to the definition.
	 *
	 * @param f
	 *            The field to add.
	 * @see FieldDefinition
	 */
	public void addField(FieldDefinition f)
	{
		if (getField(f.getName()) == null)
		{
			_fields.add(f);
			if((f.getBaseType() & Types.TYPE_REFERENCE) == Types.TYPE_REFERENCE)
				_reference_fields.add(f);
		}
	}

	/**
	 * Returns the name of this definition.
	 *
	 * @return The name of the entity definition.
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * Returns the field definitions of this entity definition.
	 *
	 * @return A list of all field definitions.
	 */
	public ArrayList<FieldDefinition> getFields()
	{
		return _fields;
	}
	
	/**
	 * Returns the reference field definitions of this entity definition.
	 *
	 * @return A list of all the reference field definitions.
	 */
	public ArrayList<FieldDefinition> getReferenceFields()
	{
		return _reference_fields;
	}

	/**
	 * Returns a list of the names of the fields in this entity definition.
	 *
	 * @return A list of names.
	 */
	public List<String> getFieldNames()
	{
		return new ArrayList<String>(_fields_cache.keySet());
	}

	
	
	/**
	 * Returns a field by name.
	 *
	 * @param name
	 *            The name of a field.
	 * @return The field definition.
	 * @see FieldDefinition
	 */
	public FieldDefinition getField(String name)
	{
		FieldDefinition f = null;
		if ((f = _fields_cache.get(name)) != null)
			return f;
		for (int i = 0; i < _fields.size(); i++)
		{
			f = _fields.get(i);
			if (f.getName().equals(name))
			{
				_fields_cache.put(name, f);
				return f;
			}
		}
		return null;
	}

	/**
	 * Creates a new instance of an entity based on this entity definition.
	 *
	 * @return A typed entity with no id.
	 */
	public Entity createInstance()
	{
		return new Entity(getName());
	}

	/**
	 * Returns a text representation of this object.
	 */
	public String toString()
	{
		StringBuffer ret = new StringBuffer();
		ret.append("ENTITY DEFINITION: " + getName() + "\n");
		for (int i = 0; i < _fields.size(); i++)
		{
			FieldDefinition f = _fields.get(i);
			ret.append("\t" + f.toString() + "\n");
		}
		return ret.toString();
	}

	/**
	 * Creates a complete copy of the definition.
	 */
	public EntityDefinition clone()
	{
		EntityDefinition d = new EntityDefinition(this.getName());
		FieldDefinition f;
		int s = _fields.size();
		for (int i = 0; i < s; i++)
		{
			f = _fields.get(i).clone();
			d.addField(f);
		}
		return d;
	}

	/**
	 * Sets the name of the definition.
	 *
	 * @param name
	 *            The name of the entity definition.
	 */
	public void setName(String name)
	{
		_name = name;
	}

	/**
	 * Compares entity definitions.
	 */
	public boolean equals(Object o)
	{
		if (!(o instanceof EntityDefinition))
			return false;
		EntityDefinition e = (EntityDefinition) o;
		if (!_name.equals(e._name))
			return false;
		if (_fields.size() != e._fields.size())
			return false;
		for (int i = 0; i < e._fields.size(); i++)
		{
			FieldDefinition this_field = getField(e._fields.get(i).getName());
			if (this_field == null)
				return false;
			if (!this_field.equals(e._fields.get(i)))
				return false;
		}
		return true;
	}
}
