package com.pagesociety.bdb.index.iterator;

import java.util.HashSet;
import java.util.List;


import com.pagesociety.persistence.Query;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;


/* this class just does a union on one particualr type of freetext query across fields*/
public class FREETEXTMULTIWRAPPERIterator extends IndexIterator
{
	IndexIterator iterator;
	int current_idx;
	List<List<DatabaseEntry>> query_key_sets;
	int type;
	boolean globbing;
	IterableIndex index;
	HashSet<Long> seen_results;
	
	public void open(IterableIndex index,Object... user_args) throws DatabaseException
	{
		globbing = (Boolean)user_args[0];
		query_key_sets = (List<List<DatabaseEntry>>)user_args[1];
		type = (Integer)user_args[2];
		current_idx = 0;
		this.index = index;
		seen_results = new HashSet<Long>();
		iterator = get_and_open_next_iterator();
		advance_to_next();
	}
	
	//while(iter.isValid())
	//{
	//	Entity e = pidx.getByRow(iter.currentKey(),iter.currentData());
	//	results.add(e);
	//	if(++added == page_size)
	//	{
	//		break;
	//	}
	//	iter.next();
	//}
	//iter.close();
	
	private IndexIterator get_and_open_next_iterator() throws DatabaseException
	{
		
		switch(type)
		{
			case Query.FREETEXT_CONTAINS_ANY:
				iterator = new FREETEXTCONTAINSANYIndexIterator();
				break;
			case Query.FREETEXT_CONTAINS_ALL:
				iterator = new FREETEXTCONTAINSALLIndexIterator();
				break;
			case Query.FREETEXT_CONTAINS_PHRASE:
				iterator = new FREETEXTCONTAINSPHRASEIndexIterator();
				break;
		}
		List<DatabaseEntry> LL = query_key_sets.get(current_idx);
		//for(int i = 0;i < LL.size();i++)
		//{
			//System.out.println("QUERY KEY "+new String(LL.get(i).getData()));
		//}
		iterator.open(index, globbing,query_key_sets.get(current_idx));
		return iterator;
	}
	
	public void resume(IterableIndex index,Object token) throws DatabaseException
	{
		
	}
	
	public void close() throws DatabaseException
	{
		iterator.close();
	}
	
	protected void advance_to_next() throws DatabaseException
	{
		long l;
		while(iterator.isValid())
		{
			l = LongBinding.entryToLong(currentData());
			if(seen_results.contains(l))
			{
				iterator.next();
				continue;
			}
			break;
		}
		
		while(isDone())
		{
			if(++current_idx > query_key_sets.size()-1)
				break;
			iterator.close();
			iterator = get_and_open_next_iterator();
			if(iterator.isDone())
				continue;
			l = LongBinding.entryToLong(currentData());
			while(iterator.isValid() && seen_results.contains(l))
			{
				iterator.next();
				l = LongBinding.entryToLong(currentData());
			}
			
		}

		if(isValid())
			seen_results.add(LongBinding.entryToLong(currentData()));
	}

	
	public boolean isDone()
	{
		return iterator.isDone();
	}
	
	public DatabaseEntry currentKey()
	{
		return iterator.currentKey();
	}
	
	public DatabaseEntry currentData()
	{
		return iterator.currentData();
	}
	
	public boolean isValid()
	{
		return iterator.isValid();
	}
	
	public void next() throws DatabaseException
	{
		iterator.next();
		advance_to_next();
	}
	
	public Object encode() throws DatabaseException
	{
		return null;
	}



}
