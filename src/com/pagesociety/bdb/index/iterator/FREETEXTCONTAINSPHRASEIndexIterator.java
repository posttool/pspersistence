package com.pagesociety.bdb.index.iterator;


import com.pagesociety.bdb.BDBSecondaryIndex;
import com.pagesociety.bdb.index.freetext.SingleFieldFreeTextIndex;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;


@SuppressWarnings("unchecked")
public class FREETEXTCONTAINSPHRASEIndexIterator extends SETCONTAINSALLIndexIterator
{
	public void open(IterableIndex index,Object... user_list_of_db_entries) throws DatabaseException
	{
		super.open(index,user_list_of_db_entries);
	}

	protected void advance_to_next() throws DatabaseException
	{
		if(iter.isDone())
		{
			return;
		}
		do
		{		
			//System.out.println(">>> ITER IS NOT DONE");
			TupleInput ti = new TupleInput(iter.data.getData(),8,4);
			int pos = ti.readInt();
			DatabaseEntry search_data = new DatabaseEntry();
			for(int i = 1;i < keys_size;i++)
			{
				//System.out.println(">>>ABOUT TO MOVE DATA IS "+new String(iter.data.getData()));
				DatabaseEntry next_key = keys.get(i);
				//we preserve stop words in the query but ignore them
				// so as to preserve position
				if(next_key == SingleFieldFreeTextIndex.FREETEXT_STOP_WORD)
					continue;
				
				TupleOutput to = new TupleOutput();
				to.writeFast(currentData().getData(),0,8);//write the id
				to.writeInt(pos+i);//write the position
				search_data.setData(to.getBufferBytes(),0,12);
				r_iter.move(keys.get(i),search_data);
				
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
