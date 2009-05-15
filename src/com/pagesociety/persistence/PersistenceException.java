package com.pagesociety.persistence;

/**
 * A simple class for the various types of exceptions that occur within the
 * store.
 */
public class PersistenceException extends Exception
{
	private static final long serialVersionUID = -8935234016086617561L;

	int code;
	public static final int NO_SPECIFIC_CODE	  = 0x0000;
	public static final int ENTITY_DOES_NOT_EXIST = 0x0001;
	public static final int UNABLE_TO_START_TRANSACTION = 0x0002;
	/**
	 * Constructs a persistence exception with a message.
	 * 
	 * @param msg
	 */
	public PersistenceException(String msg)
	{
		this(msg,NO_SPECIFIC_CODE);
	}
	
	
	public PersistenceException(String msg,int code)
	{
		this(msg,null,code);
	}

	/**
	 * Constructs a persistence exception with a message and a cause.
	 * 
	 * @param msg
	 *            the detail message.
	 * @param e
	 *            the cause. (A <tt>null</tt> value is permitted, and
	 *            indicates that the cause is nonexistent or unknown.)
	 */
	public PersistenceException(String msg, Exception e)
	{
		this(msg,e,NO_SPECIFIC_CODE);
	}
	
	
	public PersistenceException(String msg, Exception e,int code)
	{
		super(msg, e);
		this.code = code;
	}
	
	public int getErrorCode()
	{
		return code;
	}
	
}
