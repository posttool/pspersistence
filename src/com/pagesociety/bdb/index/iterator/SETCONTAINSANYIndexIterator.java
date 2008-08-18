package com.pagesociety.bdb.index.iterator;


import com.pagesociety.bdb.BDBSecondaryIndex;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;


@SuppressWarnings("unchecked")
public class SETCONTAINSANYIndexIterator extends SetIndexIterator 
{
	// both pointers to the same iterator. just different views.
	protected RespositionableIndexIterator r_iter;
	protected PredicateIndexIterator iter;
	
	public void open(IterableIndex index,Object... user_list_of_db_entries) throws DatabaseException
	{
		super.open(index,user_list_of_db_entries);
		prepare_iterator();
		iter.open(index,keys.get(current_key));		
		advance_to_next();
	}
		
	private void prepare_iterator()
	{
		if(globbing)
		{
			iter =  new STARTSWITHDATAIndexIterator();
			r_iter = (RespositionableIndexIterator)iter;
		}
		else
		{
			iter = new EQIndexIterator();
			r_iter = (RespositionableIndexIterator)iter;
		}
	}
	
	public void resume(BDBSecondaryIndex index,Object token) throws DatabaseException
	{
		super.resume(index,token);
		prepare_iterator();
		iter.resume(index,keys.get(current_key),data);
		advance_to_next();

	}
	
	public DatabaseEntry currentKey()
	{		
		return iter.key;
	}
	public DatabaseEntry currentData()
	{
		return iter.data;
	}

	public boolean isValid()
	{
		return iter.isValid();
	}
	
	public boolean isDone()
	{
		return iter.isDone();
	}
	
	public void next() throws DatabaseException
	{
		iter.next();		
		advance_to_next();
	}

	public void close() throws DatabaseException
	{
		iter.close();	
	}

	private void advance_to_next() throws DatabaseException
	{

		long l;
		while(iter.isValid())
		{
			l = LongBinding.entryToLong(iter.data);
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
			l = LongBinding.entryToLong(iter.data);
			while(iter.isValid() && seen_results.contains(l))
			{
				iter.next();
				l = LongBinding.entryToLong(iter.data);
			}
			
		}

		if(iter.isValid())
			seen_results.add(LongBinding.entryToLong(iter.data));

			//for next token we want to stay in the future??
		this.data 		 = iter.data;
		this.last_opstat = iter.last_opstat;
		
	}
}
