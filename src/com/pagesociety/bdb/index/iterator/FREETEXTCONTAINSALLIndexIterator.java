package com.pagesociety.bdb.index.iterator;


import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Transaction;


@SuppressWarnings("unchecked")
public class FREETEXTCONTAINSALLIndexIterator extends SETCONTAINSALLIndexIterator
{
	public void open(Transaction txn,IterableIndex index,Object... user_list_of_db_entries) throws DatabaseException
	{
		super.open(txn,index,user_list_of_db_entries);
	}

	protected void advance_to_next() throws DatabaseException
	{
		if(iter.isDone())
		{
			//System.out.println(">>> ITER IS DONE");
			return;
		}
		do
		{		
			//System.out.println(">>> ITER IS NOT DONE "+keys_size);

			for(int i = 1;i < keys_size;i++)
			{
				//System.out.println(">>>ABOUT TO MOVE DATA IS "+new String(iter.data.getData())+" L "+LongBinding.entryToLong(currentData())+" key is "+new String(keys.get(i).getData())+" i is"+i+" keys_size "+keys_size);
				long t1 = System.currentTimeMillis();
				r_iter.moveWithPartialData(keys.get(i),currentData());
				//System.out.println("MOVE WITH PARTIAL TOOK "+(System.currentTimeMillis() - t1));
				if(((PredicateIndexIterator)r_iter).isDone())//failed so not a member of result set
				{
					//System.out.println("DIDNT FIND "+new String(keys.get(i).getData())+" | "+LongBinding.entryToLong(iter.currentData()));
					break;
				}
				//System.out.println("FOUND "+new String(keys.get(i).getData())+" | "+LongBinding.entryToLong(iter.currentData()));
			}
			
			if(keys_size > 1 && ((PredicateIndexIterator)r_iter).isDone())//try next dup result
				iter.next();
			else if(iter.isValid())
			{	//found one in the for loop above
				break;
			}
		}while(iter.isValid());
	}
		
	public DatabaseEntry currentData()
	{
		/* this is important. we are truncating the data to just the current records
		id as opposed to id and position */
		iter.data.setSize(8);
		return iter.data;
	}
}
