package com.pagesociety.bdb.index.iterator;


import com.pagesociety.bdb.BDBSecondaryIndex;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;


@SuppressWarnings("unchecked")
public class FREETEXTCONTAINSALLIndexIterator extends SETCONTAINSALLIndexIterator
{
	public void open(IterableIndex index,Object... user_list_of_db_entries) throws DatabaseException
	{
		super.open(index,user_list_of_db_entries);
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
			//System.out.println(">>> ITER IS NOT DONE");

			for(int i = 1;i < keys_size;i++)
			{
				//System.out.println(">>>ABOUT TO MOVE DATA IS "+new String(iter.data.getData()));
				r_iter.moveWithPartialData(keys.get(i),currentData());
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
