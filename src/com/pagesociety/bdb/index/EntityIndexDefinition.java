package com.pagesociety.bdb.index;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.PersistentStore;

/**
 * Defines index types available to store users. Implementations of the store
 * must define and expose entity index definitions within their implementation.
 * EntityIndexDefinitions are entirely store dependent and relate to queries.
 * Store users are only able to add entity index instances of type(s) that are
 * defined by the store.
 *
 * @author Topher LaFata
 * @author David Karam
 *
 * @see PersistentStore#getEntityIndexDefinitions()
 */
public class EntityIndexDefinition implements java.io.Serializable
{
	private String _name;
	private String _classname;
	private String _description;
	private List<FieldDefinition> _attributes;
	private Map<String, FieldDefinition> _attributes_as_map;
	private boolean _is_multi_field_index;

	/**
	 * Constructs an empty EntityIndexDefinition.
	 */
	public EntityIndexDefinition()
	{
		_attributes = new ArrayList<FieldDefinition>();
		_attributes_as_map = new HashMap<String, FieldDefinition>();
		_is_multi_field_index = false;
	}

	/**
	 * The index definition is multifield if it uses more than 1 field.
	 *
	 * @return true if the index definition uses multiple fields.
	 */
	public boolean isMultiField()
	{
		return _is_multi_field_index;
	}

	/**
	 * Set the multifield property.
	 *
	 * @param b
	 *            Set to true if this is a multifield index.
	 */
	public void setIsMultiField(boolean b)
	{
		_is_multi_field_index = b;
	}

	/**
	 * Returns the name of the index definition.
	 *
	 * @return The name.
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * Sets the name of the index definition.
	 *
	 * @param name
	 *            The name.
	 */
	public void setName(String name)
	{
		this._name = name;
	}

	/**
	 * Gets the class name of the index definition. Because the implementation
	 *
	 * @return The fully qualified class name of the index definition.
	 */
	public String getClassName()
	{
		return _classname;
	}

	/**
	 * Sets the fully qualified class name of the index definition.
	 *
	 * @param classname
	 *            The classname of the index definition.
	 */
	public void setClassName(String classname)
	{
		this._classname = classname;
	}

	/**
	 * Returns the description of this index definition.
	 *
	 * @return The description of the index definition.
	 */
	public String getDescription()
	{
		return _description;
	}

	/**
	 * Sets the description of this index definition.
	 *
	 * @param description
	 *            The description.
	 */
	public void setDescription(String description)
	{
		this._description = description;
	}

	/**
	 * Returns the settable attributes of this index definition.
	 *
	 * @return A list of field definitions.
	 */
	public List<FieldDefinition> getAttributes()
	{
		return _attributes;
	}

	/**
	 * Returns an attribute type by name.
	 *
	 * @param name
	 *            The name of the attribute.
	 * @return A field definition.
	 */
	public FieldDefinition getAttributeType(String name)
	{
		return _attributes_as_map.get(name);
	}

	/**
	 * Convenience method for getting attribute types as a map with the key
	 * being the attribute name.
	 *
	 * @return A map of fields keyed by name.
	 */
	public Map<String, FieldDefinition> getAttributeTypesAsMap()
	{
		return _attributes_as_map;
	}

	/**
	 * Adds a new attribute definition for this entity index definition. An
	 * example of an index attribute is 'ascending' which would be type boolean.
	 *
	 * @param attribute
	 *            The field definition that specifies the attribute.
	 */
	public void addAttribute(FieldDefinition attribute)
	{
		_attributes_as_map.put(attribute.getName(), attribute);
		_attributes.add(attribute);
	}
}
