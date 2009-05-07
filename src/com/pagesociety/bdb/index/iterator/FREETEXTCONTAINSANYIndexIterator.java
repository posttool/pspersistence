package com.pagesociety.bdb.index.iterator;


import java.util.Map;

import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Transaction;


@SuppressWarnings("unchecked")
public class FREETEXTCONTAINSANYIndexIterator extends SETCONTAINSANYIndexIterator
{

	
	public void open(Transaction txn,IterableIndex index,Object... user_list_of_db_entries) throws DatabaseException
	{
		super.open(txn,index,user_list_of_db_entries);

	}
		
	protected void advance_to_next() throws DatabaseException
	{
		long l;
		while(iter.isValid())
		{
			l = LongBinding.entryToLong(currentData());
			if(seen_results.contains(l))
			{
				iter.next();
				continue;
			}
			break;
		}
		
		while(iter.isDone())
		{
			if(++current_key > keys.size()-1)
				break;

			r_iter.move(keys.get(current_key));
			if(iter.isDone())
				continue;
			l = LongBinding.entryToLong(currentData());
			while(iter.isValid() && seen_results.contains(l))
			{
				iter.next();
				l = LongBinding.entryToLong(currentData());
			}
			
		}

		if(iter.isValid())
			seen_results.add(LongBinding.entryToLong(currentData()));
	}

	/*!!!remember that currentData() returns truncated row so it is just the id 
	 * and not id:pos
	 */ 


	public DatabaseEntry currentData()
	{
		iter.data.setSize(8);
		return iter.data;
	}
	/* maybe a future enhancement. basically have to stuff the meta data in at the right time since
	 * we are resizing they data and actually truncating pos information in a lot of cases because
	 * we only really need it for the phrase match.*
	 */
	public Map<String,Object> currentMetaData()
	{
		return null;
	}
	
	public Map<String,Object> resultMetaData()
	{
		return null;
	}

}
