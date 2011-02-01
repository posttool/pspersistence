package com.pagesociety.bdb.index.iterator;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.Transaction;

public class EQIndexIterator extends PredicateIndexIterator implements RespositionableIndexIterator
{	
	public void open(Transaction txn,IterableIndex index,Object... user_arg) throws DatabaseException
	{
		super.open(txn,index,user_arg);
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
	//	System.out.println("\tMOVE TO KEY"+new String(key.getData())+" DATA "+new String(newdata.getData()));
	//	System.out.println("NEWDATA SIZE IS "+newdata.getSize());
		key  = newkey;
		data = newdata;
		last_opstat	=	index_cursor.getSearchBoth(key, data, LockMode.DEFAULT);	
	//	if(isValid())
	//		System.out.println("\t\tMOVE OK "+new String(key.getData())+" | "+LongBinding.entryToLong(newdata));
	//	else
	//		System.out.println("\t\tFAILED MOVE "+new String(key.getData())+" | "+LongBinding.entryToLong(newdata));
	}
	
	
	/* used in freetext index stuff since the row data is not just a pkey. it also has a
	 * pos index after it e.g. 42-pkey:pos
	 */
	public void moveWithPartialData(DatabaseEntry newkey,DatabaseEntry partial_data) throws DatabaseException
	{
		key  = newkey;
		data = partial_data;
		DatabaseEntry original_data = IteratorUtil.cloneDatabaseEntry(partial_data);
		int s = original_data.getSize();
		

		last_opstat	=	index_cursor.getSearchBothRange(key, data, LockMode.DEFAULT);	

		

		if(IteratorUtil.compareDatabaseEntries(original_data, 0, s, data, 0, s) != 0)
			last_opstat = OperationStatus.NOTFOUND;

	//	if(isValid())
	//		System.out.println("\t\tMOVE OK "+new String(key.getData())+" | "+LongBinding.entryToLong(newdata));
	//	else
	//		System.out.println("\t\tFAILED MOVE "+new String(key.getData())+" | "+LongBinding.entryToLong(newdata));
	}
	
	
	
}	
