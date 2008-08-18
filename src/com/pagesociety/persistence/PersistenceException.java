package com.pagesociety.persistence;

/**
 * A simple class for the various types of exceptions that occur within the
 * store.
 */
public class PersistenceException extends Exception
{
	private static final long serialVersionUID = -8935234016086617561L;

	/**
	 * Constructs a persistence exception with a message.
	 * 
	 * @param msg
	 */
	public PersistenceException(String msg)
	{
		super(msg);
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
		super(msg, e);
	}
}
