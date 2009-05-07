package com.pagesociety.bdb.index.iterator;


import com.pagesociety.bdb.BDBSecondaryIndex;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.Transaction;

public class BETWEEN_ASC_EXCLUSIVEIndexIterator extends RangeIndexIterator
{
	private DatabaseEntry original_search_key;
	
	public void open(Transaction txn,IterableIndex index,Object... user_args) throws DatabaseException
	{
		super.open(txn,index,user_args);
		//?? figure this out. would be nice to have
		//if(user_arg == Query.VAL_MIN)
		//	last_opstat = index_cursor.getFirst(key, data, LockMode.DEFAULT);

		original_search_key = IteratorUtil.cloneDatabaseEntry(key);
		last_opstat 		= index_cursor.getSearchKeyRange(key, data, LockMode.DEFAULT);
		if(last_opstat == OperationStatus.SUCCESS)
		{
			int l1 = original_search_key.getSize();
			int l2 = key.getSize();
			//int l = (l1 < l2)?l1:l2;
			if(IteratorUtil.compareDatabaseEntries(original_search_key, 0, l1, key, 0, l2) == 0)
			{
				last_opstat = index_cursor.getNextNoDup(key, data, LockMode.DEFAULT);
				check_terminal_key();
			}
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
	
	public boolean isValid() 
	{
		return (last_opstat == OperationStatus.SUCCESS);
	}
	
	public void next() throws DatabaseException
	{
		last_opstat =  index_cursor.getNextDup(key, data, LockMode.DEFAULT);	
		if(last_opstat == OperationStatus.NOTFOUND)
		{
			last_opstat = index_cursor.getNextNoDup(key, data, LockMode.DEFAULT);
			check_terminal_key();
		}
	}
	
	public boolean isDone() 
	{
		return (last_opstat == OperationStatus.NOTFOUND);
	}
	
	//NOTE: THESE METHODS ARE SLIGHTLY DIFFERENT FOr ALL CASES!!!!DONT REFACtOR LIGHTLY
	protected void check_terminal_key()
	{
		if(last_opstat == OperationStatus.SUCCESS)
		{
			int l1 = key.getSize();
			int l2 = terminal_key_length;
			//int l = (l1 < l2)?l1:l2;
			if(IteratorUtil.compareDatabaseEntries(key, 0, l1, terminal_key, 0, l2) >= 0)
				last_opstat =  OperationStatus.NOTFOUND;
		}
	}
}
