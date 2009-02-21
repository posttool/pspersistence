package com.pagesociety.persistence;

import java.util.ArrayList;

/**
 * The possible field types that the store will handle. All types (except
 * undefined) can be mixed with the array type. Arrays are specified by or-ing
 * the types: for example <code>TYPE_ARRAY | TYPE_STRING</code>.
 * 
 * @author Topher LaFata
 * @author David Karam
 * 
 * @see FieldDefinition
 */
public class Types
{
	/**
	 * Unused.
	 */
	public static final int TYPE_UNDEFINED = 0x000;
	/**
	 * The array type must be used in conjunction with any other type. For
	 * example, a date array is indicated by the following
	 * <code>TYPE_ARRAY | TYPE_DATE</code>.
	 */
	public static final int TYPE_ARRAY = 0x001;
	/**
	 * Corresponds to the Java Boolean type.
	 */
	public static final int TYPE_BOOLEAN = 0x002;
	/**
	 * Corresponds to the Java Integer type.
	 */
	public static final int TYPE_INT = 0x004;
	/**
	 * Corresponds to the Java Long type.
	 */
	public static final int TYPE_LONG = 0x008;
	/**
	 * Corresponds to the Java Double type.
	 */
	public static final int TYPE_DOUBLE = 0x010;
	/**
	 * Corresponds to the Java Float type.
	 */
	public static final int TYPE_FLOAT = 0x020;
	/**
	 * Corresponds to the Java String type. This type as well as the 'text' type
	 * both represent Strings. The difference between the two is that this
	 * should be of a known (or maximum) length, where as the text type is of an
	 * unknown maximum length.
	 * 
	 * @see #TYPE_TEXT
	 */
	public static final int TYPE_STRING = 0x040;
	/**
	 * Corresponds to the Java String type. This type of string is specified
	 * when a maximum length of the number of characters in the string cannot be
	 * safely determined.
	 * 
	 * @see #TYPE_STRING
	 */
	public static final int TYPE_TEXT = 0x080;
	/**
	 * Corresponds to the Java Date type.
	 */
	public static final int TYPE_DATE = 0x100;
	/**
	 * Corresponds to the Java byte[] type.
	 */
	public static final int TYPE_BLOB = 0x200;
	/**
	 * A reference to another entity type. Any field that is typed as a
	 * reference or a reference array must specify the name of the entity
	 * definition to which it refers.
	 */
	public static final int TYPE_REFERENCE 		   = 0x400;
	
	public static int parseType(String s) throws PersistenceException
	{
		s = s.toUpperCase();
		int type=0;
		
		if(s.startsWith("TYPE_STRING") || s.startsWith("STRING"))
			type =  TYPE_STRING;
		else if(s.startsWith("TYPE_REFERENCE") || s.startsWith("REFERENCE"))
			type =  TYPE_REFERENCE;
		else if(s.startsWith("TYPE_DATE") || s.startsWith("DATE"))
			type =  TYPE_DATE;
		else if(s.startsWith("TYPE_INT")  || s.startsWith("INT"))
			type =  TYPE_INT;
		else if(s.startsWith("TYPE_LONG") || s.startsWith("LONG"))
			type = TYPE_LONG;
		else if(s.startsWith("TYPE_FLOAT")|| s.startsWith("FLOAT"))
			type =  TYPE_FLOAT;
		else if(s.startsWith("TYPE_DOUBLE")  || s.startsWith("DOUBLE"))
			type =  TYPE_DOUBLE;		
		else if(s.startsWith("TYPE_BOOLEAN") || s.startsWith("BOOLEAN"))
			type = TYPE_BOOLEAN;
		else if(s.startsWith("TYPE_BLOB") || s.startsWith("BLOB"))
			type =  TYPE_BLOB;
		else if(s.startsWith("TYPE_TEXT") || s.startsWith("TEXT"))
			type =  TYPE_TEXT;
		else
			throw new PersistenceException("UNKNOWN TYPE "+s);
		
		if(s.endsWith("[]"))
			type = (type | TYPE_ARRAY);
	
	
		return type;
	}
	
	public static Object parseDefaultValue(int type,String default_val) throws PersistenceException
	{
		if(default_val.equals("null"))
			return null;
		default_val.trim();
		
		boolean is_array = ((type & TYPE_ARRAY) == TYPE_ARRAY);
		if(is_array)
		{
			if(default_val.toUpperCase().equals("EMPTY_LIST"))
				return new ArrayList();
			else
				throw new PersistenceException("ARRAY DEFAULT ISNT SUPPORTED YET");
		}
		switch(type)
		{
			case TYPE_TEXT:
			case TYPE_STRING:
				return default_val;
			case TYPE_REFERENCE:
				 String[] e_parts = default_val.split(":");
				 String entity_name = e_parts[0];
				 Long entity_id = Long.parseLong(e_parts[1]);
				 Entity e = new Entity();
				 e.setType(entity_name);
				 e.setId(entity_id);
				 return e;
			case TYPE_INT:
				return Integer.parseInt(default_val);
			case TYPE_LONG:
				return Long.parseLong(default_val);
			case TYPE_FLOAT:
				return Float.parseFloat(default_val);
			case TYPE_DOUBLE:
				return Double.parseDouble(default_val);
			case TYPE_DATE:
				throw new PersistenceException("DATE DEFAULT ISNT SUPPORTED YET");
			case TYPE_BLOB:
				throw new PersistenceException("BLOB DEFAULT ISNT SUPPORTED YET");
			default:
					return null;
		}
	}
}
