package com.pagesociety.bdb.index.iterator;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;

public interface RespositionableIndexIterator 
{
	public void move(DatabaseEntry newkey) throws DatabaseException;
	public void move(DatabaseEntry newkey,DatabaseEntry newdata) throws DatabaseException;
	/* move to newkey and data that starts with newdata */
	public void moveWithPartialData(DatabaseEntry newkey,DatabaseEntry newdata) throws DatabaseException;
}
