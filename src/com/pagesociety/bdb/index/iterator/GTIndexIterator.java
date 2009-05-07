package com.pagesociety.bdb.index.iterator;



import com.pagesociety.bdb.BDBSecondaryIndex;
import com.pagesociety.persistence.Query;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.Transaction;


public class GTIndexIterator extends PredicateIndexIterator {

	private DatabaseEntry original_search_key;
	
	public void open(Transaction txn,IterableIndex index,Object... user_arg) throws DatabaseException
	{
		super.open(txn,index,user_arg);
		original_search_key = IteratorUtil.cloneDatabaseEntry(key);
		last_opstat = index_cursor.getSearchKeyRange(key, data, LockMode.DEFAULT);
		if(last_opstat == OperationStatus.SUCCESS)
		{
			if(original_search_key.equals(key))
				last_opstat = index_cursor.getNextNoDup(key, data, LockMode.DEFAULT);
		}

	}
	
	public void resume(IterableIndex index,Object token) throws DatabaseException
	{
		super.resume(index,token);
		last_opstat	= index_cursor.getSearchBothRange(key, data, LockMode.DEFAULT);
	}
	
	public DatabaseEntry currentKey()
	{
		return key;
	}
	
	public DatabaseEntry currentData()
	{
		return data;
	}
	
	public void next() throws DatabaseException
	{
		last_opstat =  index_cursor.getNext(key, data, LockMode.DEFAULT);		
	}
	
	public boolean isValid() 
	{
		return (last_opstat == OperationStatus.SUCCESS);
	}
	
	public boolean isDone() 
	{
		return (last_opstat == OperationStatus.NOTFOUND);
	}
	
}
