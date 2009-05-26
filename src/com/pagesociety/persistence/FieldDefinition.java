package com.pagesociety.persistence;

import java.util.ArrayList;
import java.util.Date;

/**
 * Defines a field including its name, type and other attributes. Field
 * definition are used by entity definitions as well as entity indexes.
 * 
 * 
 * @author Topher LaFata
 * @author David Karam
 * 
 * @see EntityDefinition
 * @see EntityIndex
 */
public class FieldDefinition implements java.io.Serializable
{
	/* used if you want a reference field to be an untyped entity */
	/* fillReference methods will key off this value to deal with */
	/* singleton and list untyped entity references */
	public static final String REF_TYPE_UNTYPED_ENTITY = "*";
	// !!!!!! REMEMBER TO UPDATE CLONE METHOD IF YOU ADD OR REMOVE FIELDS
	// !!!!!!!//
	private String 	_name;
	private int 	_type;
	private String 	_ref_type;
	private Object 	_default_value;
	private boolean _cascade_on_delete;
	private boolean _required;
	private String  _comment;

	// private int _array_dimensionality;
	/**
	 * A default constructor. Usage requires that the name and type be set
	 * before the programmer considers the field definition valid.
	 */
	public FieldDefinition()
	{
	}

	/**
	 * A constructor for non reference typed fields.
	 * 
	 * @param name
	 *            The name of the field.
	 * @param type
	 *            The type of the field.
	 * @see Types
	 */
	public FieldDefinition(String name, int type)
	{
		if ((type & ~Types.TYPE_ARRAY) == Types.TYPE_REFERENCE)
			throw new RuntimeException("Field " + name + " must be defined with a reference type.");
		_name 			   = name;
		_type 			   = type;
		_default_value 	   = null;
		_ref_type 		   = null;
		_required 		   = false;
		_cascade_on_delete = false;
		_comment 		   = null;
		// _array_dimensionality = 0;
	}

	/**
	 * A constructor for field that are TYPE_REFERENCE.
	 * 
	 * @param name
	 *            The name of the field.
	 * @param type
	 *            The type of the field.
	 * @param ref_type
	 *            The name of the entity definition to which this field refers.
	 */
	public FieldDefinition(String name, int type, String ref_type)
	{
		_name = name;
		_type = type;
		_ref_type = ref_type;
		_default_value = null;
		_required 		   = false;
		_cascade_on_delete = false;
		_comment 		   = null;
		// _array_dimensionality = 0;
	}

	/**
	 * Set the name of the field.
	 * 
	 * @param name
	 *            The name of the field.
	 */
	public void setName(String name)
	{
		_name = name;
	}

	/**
	 * Return the name of the field.
	 * 
	 * @return The name of the field.
	 */
	public String getName()
	{
		return _name;
	}

	/**
	 * Return the type. This type might include an array flag. Use getBaseType
	 * and isArray to get the exploded typing information.
	 * 
	 * @return The type.
	 * @see #getBaseType()
	 * @see #isArray()
	 */
	public int getType()
	{
		return _type;
	}

	/**
	 * Sets the type of this field.
	 * 
	 * @param type
	 *            The type
	 * @see Types
	 */
	public void setType(int type)
	{
		_type = type;
	}

	/**
	 * Returns the 'base' type of this field. The base type masks the array flag
	 * from the type.
	 * 
	 * @return The base type.
	 */
	public int getBaseType()
	{
		return _type & ~Types.TYPE_ARRAY;
	}

	/**
	 * Returns whether the type information contains the array flag.
	 * 
	 * @return true if this field is an array.
	 */
	public boolean isArray()
	{
		return ((_type & Types.TYPE_ARRAY) == Types.TYPE_ARRAY);
	}

	/**
	 * Set this field to be an array.
	 * 
	 * @param b
	 *            true if this field should be an array.
	 */
	public void setIsArray(boolean b)
	{
		if (b)
		{
			if(isArray())
				return;
			_type = _type | Types.TYPE_ARRAY;
			// default to 1
			// _array_dimensionality = 1;
		}
		else
		{
			if(!isArray())
				return;
			_type = _type & ~Types.TYPE_ARRAY;
			// _array_dimensionality = 0;
		}
	}

	/**
	 * Set the reference type. The reference type is specified by the name of an
	 * entity definition. All references are to other entity definitions.
	 * 
	 * @param ref_type
	 *            The name of the entity definition to reference.
	 * @see Types#TYPE_REFERENCE
	 */
	public void setReferenceType(String ref_type)
	{
		_ref_type = ref_type;
	}

	/**
	 * Returns the reference type or null if the field does not have a reference
	 * base type.
	 * 
	 * @return The reference type or null.
	 */
	public String getReferenceType()
	{
		return _ref_type;
	}

	/*
	 * returns field definition so we can do DEF.addField(new
	 * FieldDefinition(FIELD_NOTES, Types.TYPE_TEXT).setDefault("Untitled"));
	 */
	public FieldDefinition setDefaultValue(Object default_value)
	{
		_default_value = default_value;
		return this;
	}

	public Object getDefaultValue()
	{
		return _default_value;
	}

	public boolean isCascadeOnDelete()
	{
		return _cascade_on_delete;
	}
	
	public void setIsCascadeOnDelete(boolean b)
	{
		_cascade_on_delete = b;
	}
	
	public boolean isRequired()
	{
		return _cascade_on_delete;
	}
	
	public void setIsRequired(boolean b)
	{
		_required = b;
	}

	public String getComment()
	{
		return _comment;
	}
	
	public void setComment(String comment)
	{
		_comment = comment;
	}
	
	/**
	 * Creates a clone of the field definition, copying all fields.
	 */
	public FieldDefinition clone()
	{
		FieldDefinition f = new FieldDefinition();
		f._name 			 = _name;
		f._type 			 = _type;
		f._ref_type 		 = _ref_type;
		f._default_value 	 = _default_value;
		f._required 		 = _required;
		f._cascade_on_delete = _cascade_on_delete;
		f._comment			 = _comment;
		// f._array_dimensionality = _array_dimensionality;
		return f;
	}

	/**
	 * Compares all properties of one field definition to another.
	 */
	public boolean equals(Object o)
	{
		if (!(o instanceof FieldDefinition))
			return false;
		FieldDefinition f = (FieldDefinition) o;
		return  (
				(_name.equals(f._name)) &&
				(_type == f._type) &&
				(_cascade_on_delete == f._cascade_on_delete) &&
				(_required == f._required) &&
				(_ref_type == f._ref_type || _ref_type.equals(f._ref_type)) &&
				(_default_value == f._default_value || _default_value.equals(f._default_value)) &&
				(_comment == f._comment || _comment.equals(f._comment))
				);		
	}

	/**
	 * Returns a text representation of this object.
	 */
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		buf.append("Field: " + getName() + " is ");
		int type = getType();
		if ((type & Types.TYPE_ARRAY) == Types.TYPE_ARRAY)
		{
			type = getBaseType();
			buf.append("an array of ");
			buf.append(toStringBaseType(type));
			buf.append(" Default value: "+getDefaultValue());
			return buf.toString();
		}
		else
		{
			String t = toStringBaseType(type);
			buf.append("a");
			if (t.startsWith("I"))
				buf.append("n");
			buf.append(" ");
			buf.append(t);
		}
		buf.append(" Default value: "+getDefaultValue());
		return buf.toString();
	}

	/**
	 * A utility function for decoding type directly from encoded type int.
	 * 
	 * @param type
	 * @return A text representation of a type.
	 */
	public static String typeAsString(int type)
	{
		if ((type & Types.TYPE_ARRAY) == Types.TYPE_ARRAY)
		{
			StringBuffer buf = new StringBuffer();
			buf.append("List<"+toStringBaseType(type & ~Types.TYPE_ARRAY)+">");
			return buf.toString();
		}
		else
		{
			return toStringBaseType(type);
		}
	}
	

	private static String toStringBaseType(int type)
	{
		switch (type)
		{
		case Types.TYPE_UNDEFINED:
			return "Undefined";
		case Types.TYPE_BOOLEAN:
			return "Boolean";
		case Types.TYPE_LONG:
			return "Long";
		case Types.TYPE_INT:
			return "Integer";
		case Types.TYPE_DOUBLE:
			return "Double";
		case Types.TYPE_FLOAT:
			return "Float";
		case Types.TYPE_STRING:
			return "String";
		case Types.TYPE_TEXT:
			return "Text";
		case Types.TYPE_DATE:
			return "Date";
		case Types.TYPE_BLOB:
			return "Blob";
		case Types.TYPE_REFERENCE:
			return "Entity";
		default:
			return "Unknown Type(" + type + ")";
		}
	}

	public boolean isValidValue(Object o)
	{
		if (o == null)
			return true;
		if (isArray() && o.getClass() == ArrayList.class)
			return true;
		switch (_type)
		{
		case Types.TYPE_BOOLEAN:
			if (o.getClass() == Boolean.class)
				return true;
			break;
		case Types.TYPE_INT:
			if (o.getClass() == Integer.class)
				return true;
			break;
		case Types.TYPE_LONG:
			if (o.getClass() == Long.class)
				return true;
			break;
		case Types.TYPE_DOUBLE:
			if (o.getClass() == Double.class)
				return true;
			break;
		case Types.TYPE_FLOAT:
			if (o.getClass() == Float.class)
				return true;
			break;
		case Types.TYPE_STRING:
		case Types.TYPE_TEXT:
			if (o.getClass() == String.class)
				return true;
			break;
		case Types.TYPE_DATE:
			if (o.getClass() == Date.class)
				return true;
			break;
		case Types.TYPE_BLOB:
			if (o.getClass() == byte.class && o.getClass().isArray())
				return true;
			break;
		case Types.TYPE_REFERENCE:
			if (o.getClass() != Entity.class)
				return false;
			if(!_ref_type.equals(REF_TYPE_UNTYPED_ENTITY))
			{
				Entity e = (Entity)o;
				if(!_ref_type.equals(e.getType()))
					return false;
			}
			return true;
		default:
			break;
		}
		return false;
	}


}