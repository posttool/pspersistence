package com.pagesociety.persistence;

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
	
	
}
