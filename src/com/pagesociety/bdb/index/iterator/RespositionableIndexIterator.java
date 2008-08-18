package com.pagesociety.bdb.index.iterator;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;

public interface RespositionableIndexIterator 
{
	public void move(DatabaseEntry newkey) throws DatabaseException;
	public void move(DatabaseEntry newkey,DatabaseEntry newdata) throws DatabaseException;
}
