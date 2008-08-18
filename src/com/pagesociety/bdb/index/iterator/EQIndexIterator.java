package com.pagesociety.bdb.index.iterator;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;

public class EQIndexIterator extends PredicateIndexIterator implements RespositionableIndexIterator
{	
	public void open(IterableIndex index,Object... user_arg) throws DatabaseException
	{
		super.open(index,user_arg);
		last_opstat	=	index_cursor.getSearchKey(key, data, LockMode.DEFAULT);
	}
	
	public void resume(IterableIndex index,Object token) throws DatabaseException
	{
		super.resume(index,token);
		last_opstat	=	index_cursor.getSearchBothRange(key, data, LockMode.DEFAULT);
	}
	
	protected void resume(IterableIndex index,DatabaseEntry key,DatabaseEntry data) throws DatabaseException
	{
		super.resume(index, key, data);
		last_opstat	=	index_cursor.getSearchBothRange(key, data, LockMode.DEFAULT);
	}
	
	public DatabaseEntry currentKey()
	{
		return key;
	}
	
	public DatabaseEntry currentData()
	{
		return data;
	}
	
	public boolean isValid() 
	{
		return (last_opstat == OperationStatus.SUCCESS);
	}
	
	public void next() throws DatabaseException
	{
		last_opstat =  index_cursor.getNextDup(key, data, LockMode.READ_UNCOMMITTED);
	}
	
	public boolean isDone() 
	{
		return (last_opstat == OperationStatus.NOTFOUND);
	}
	
	protected int count() throws DatabaseException
	{
		if(last_opstat == OperationStatus.SUCCESS)
			return index_cursor.count();
		
		return 0;
	}
	
	public void move(DatabaseEntry newkey) throws DatabaseException
	{
		key = newkey;
		last_opstat	=	index_cursor.getSearchKey(key, data, LockMode.DEFAULT);	
	}
	
	public void move(DatabaseEntry newkey,DatabaseEntry newdata) throws DatabaseException
	{
		key = newkey;
		data = newdata;
	//	System.out.println("\tMOVING TO "+new String(key.getData())+" | "+LongBinding.entryToLong(data));
		last_opstat	=	index_cursor.getSearchBoth(key, data, LockMode.DEFAULT);	
	//	if(isValid())
	//		System.out.println("\t\tMOVE OK "+new String(key.getData())+" | "+LongBinding.entryToLong(newdata));
	//	else
	//		System.out.println("\t\tFAILED MOVE "+new String(key.getData())+" | "+LongBinding.entryToLong(newdata));
	}
	
}	
