package com.pagesociety.bdb.index.iterator;


import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Transaction;

public abstract class IndexIterator 
{
	public abstract void open(Transaction txn,IterableIndex index,Object... user_args) throws DatabaseException;
	public abstract void resume(IterableIndex index,Object token) throws DatabaseException;
	public abstract void close() throws DatabaseException;
	public abstract Object encode() throws DatabaseException;
	public abstract boolean isDone(); 
	public abstract DatabaseEntry currentKey();
	public abstract DatabaseEntry currentData();
	public abstract boolean isValid();
	public abstract void next() throws DatabaseException;
	
}
