package com.pagesociety.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

//import com.pagesociety.bdb.index.EntityIndexDefinition;

/**
 * Represents an instance of a single or multiple field index. All indexes must
 * be defined by an entity index definition as well as an entity definition and
 * at least one of its fields. Indices can be created using one or more fields,
 * providing the basis for both rapid random lookups and efficient ordering of
 * access to records.
 *
 * @author Topher LaFata
 * @author David Karam
 *
 * @see EntityIndexDefinition
 * @see PersistentStore#addEntityIndex(String, String, String, String, Map)
 */
public class EntityIndex
{

	public static final int TYPE_SIMPLE_SINGLE_FIELD_INDEX 			 = 0x01;
	public static final int TYPE_SIMPLE_MULTI_FIELD_INDEX  			 = 0x02;

	public static final int TYPE_ARRAY_MEMBERSHIP_INDEX    			 = 0x11;
	public static final int TYPE_MULTIFIELD_ARRAY_MEMBERSHIP_INDEX   = 0x12;
	
	public static final int TYPE_FREETEXT_INDEX   		 = 0x21;
	public static final int TYPE_MULTI_FIELD_FREETEXT_INDEX   		 = 0x22;

	private String 					_name;//the user name for the index. unique per entity type
	private int				 		_index_type;//the type of the index. must be one of the types above
	private String			 		_entity_type;//index is on instances of these types of entities
	private List<FieldDefinition> 	_fields;//the indexed field/fields
	private Map<String, Object> 	_attributes;//additional attributes of the index. currently unused

	/**
	 * Create a default untyped index.
	 */
	public EntityIndex()
	{
		_attributes = new HashMap<String, Object>();
		_fields 	= new ArrayList<FieldDefinition>();
	}

	/**
	 * Constructs an index with a name and a type.
	 *
	 * @param name
	 *            The name of the new index.
	 * @param eid
	 *            The definition for this index.
	 * @see PersistentStore#getEntityIndexDefinitions()
	 */
	public EntityIndex(String name, int index_type)
	{
		_index_type = index_type;
		_name = name;
		_attributes = new HashMap<String, Object>();
		_fields = new ArrayList<FieldDefinition>();
	}
	
	public EntityIndex(String name, int index_type,FieldDefinition ...fields)
	{
		_index_type = index_type;
		_name = name;
		_attributes = new HashMap<String, Object>();
		_fields = new ArrayList<FieldDefinition>();
		for(int i = 0;i < fields.length;i++)
			addField(fields[i]);
	}
	
	

	/**
	 * Returns the definition for this index.
	 *
	 * @return An entity index definition.
	 */
	public int getType()
	{
		return _index_type;
	}

	/**
	 * Sets a name for this index.
	 *
	 * @param name
	 *            The name of the index.
	 */
	public void setName(String name)
	{
		_name = name;
	}

	/**
	 * Returns the name of the index.
	 *
	 * @return The name of the index.
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * Returns the entity definition on which this index operates.
	 *
	 * @return The parent entity.
	 */
	public String getEntity()
	{
		return _entity_type;
	}

	/**
	 * Sets the entity definition on which this index operates.
	 *
	 * @param def
	 */
	public void setEntity(String entity_type)
	{
		_entity_type = entity_type;
	}

	/**
	 * Makes a reference to the fields within the entity definition on which
	 * this index operates.
	 *
	 * @param field
	 *            A field to index.
	 */
	public void addField(FieldDefinition field)
	{
		_fields.add(field);
	}

	/**
	 * Returns a list of the fields on which this index operates.
	 *
	 * @return A list of field definitions.
	 */
	public List<FieldDefinition> getFields()
	{
		return _fields;
	}

	/**
	 * Returns attributes of this index. These are arbitrary parameters that
	 * should be specified by the EntityIndexDefinition.
	 *
	 * @return Index attributes.
	 */
	public Map<String, Object> getAttributes()
	{
		return _attributes;
	}
	
	public Object getAttribute(String att_name)
	{
		return _attributes.get(att_name);
	}

	
	
	/**
	 * Sets attribute values for this index. Attributes are defined by the
	 * EntityIndexDefinition.Attributes are currently only String and List<String>
	 *
	 * @param key
	 *            The attribute name.
	 * @param value
	 *            The value.
	 */
	public void setAttribute(String key, Object value)
	{
		_attributes.put(key, value);
	}
	
	public void setAttributes(Map<String,Object> att)
	{
		_attributes = att;
	}

	/**
	 * Makes a copy of the index.
	 */
	public EntityIndex clone()
	{
		EntityIndex ii = new EntityIndex(_name, _index_type);
		ii.setEntity(_entity_type);
		for (int i = 0; i < _fields.size(); i++)
			ii.addField(_fields.get(i));
		Iterator<String> keys = _attributes.keySet().iterator();
		String key;
		while (keys.hasNext())
		{
			key = keys.next();
			ii.setAttribute(key, _attributes.get(key));
		}
		return ii;
	}

	
	public boolean equals(Object o)
	{
		EntityIndex idx = (EntityIndex)o;
		if(o==null)
			return false;
		
		return 		(_index_type == idx._index_type 	  &&
					_name.equals(idx._name) 			  &&
					_entity_type.equals(idx._entity_type) &&
					_fields.equals(idx._fields));
	}
	
	public FieldDefinition getField(String fieldname)
	{
		for(int i = 0;i < _fields.size();i++)
		{
			FieldDefinition f = _fields.get(i);
			if(f != null && f.getName().equals(fieldname))
				return f;
		}
		return null;
	}
	
	
	/**
	 * Returns a string representation of the index.
	 */
	public String toString()
	{
		return _name + " " + typeToString(_index_type) + " " + _fields;
	}


	public static String typeToString(int type)
	{
		switch(type)
		{
			case TYPE_SIMPLE_SINGLE_FIELD_INDEX:
				return "SimpleSingleFieldIndex";
			case TYPE_SIMPLE_MULTI_FIELD_INDEX:
				return "SimpleMutliFieldIndex";
			case TYPE_ARRAY_MEMBERSHIP_INDEX:
				return "ArrayMembershipIndex";
			case TYPE_MULTIFIELD_ARRAY_MEMBERSHIP_INDEX:
				return "MultiFieldArrayMembershipIndex";
			case TYPE_FREETEXT_INDEX:
				return "SingleFieldFreeTextIndex";	
			case TYPE_MULTI_FIELD_FREETEXT_INDEX:
				return "MultiFieldFreeTextIndex";					
			default:
				return "Unknown";
		}
	}
}
