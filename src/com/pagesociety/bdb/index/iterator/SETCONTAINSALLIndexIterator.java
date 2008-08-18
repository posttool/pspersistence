package com.pagesociety.bdb.index.iterator;


import com.pagesociety.bdb.BDBSecondaryIndex;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;


@SuppressWarnings("unchecked")
public class SETCONTAINSALLIndexIterator extends SetIndexIterator 
{
	// both pointers to the same iterator. just different views.
	protected RespositionableIndexIterator r_iter;
	protected PredicateIndexIterator iter;
	protected DatabaseEntry  first_value_key;
	protected int		     keys_size;
	
	public void open(IterableIndex index,Object... user_list_of_db_entries) throws DatabaseException
	{
		super.open(index,user_list_of_db_entries);
		prepare_iterator();
		first_value_key = keys.get(0);
		keys_size = keys.size();		
		iter.open(index,first_value_key);
		DatabaseEntry first_value_copy = IteratorUtil.cloneDatabaseEntry(first_value_key);
		((PredicateIndexIterator)r_iter).open(index,first_value_copy);	
		
		advance_to_next();

	}
	
	private void prepare_iterator()
	{
		if(globbing)
		{
			iter 	=  new STARTSWITHDATAIndexIterator();
			r_iter 	= new STARTSWITHDATAIndexIterator();
		}
		else
		{
			iter 	= new EQIndexIterator();
			r_iter 	= new EQIndexIterator();
		}
	}
	
	public void resume(BDBSecondaryIndex index,Object token) throws DatabaseException
	{
		super.resume(index,token);
		prepare_iterator();
		first_value_key = keys.get(0);
		keys_size = keys.size();
		iter.resume(index,data);
		((PredicateIndexIterator)r_iter).resume(index,new DatabaseEntry());	
		advance_to_next();

	}

	private void advance_to_next() throws DatabaseException
	{
		if(iter.isDone())
		{
			//System.out.println(">>> ITER IS DONE");
			return;//if it cant even find the first one we already failed
		}
		do
		{		
			//System.out.println(">>> ITER IS NOT DONE");
			for(int i = 1;i < keys_size;i++)
			{
				//DatabaseEntry tmp = IteratorUtil.cloneDatabaseEntry(iter.currentData());
				//System.out.println(">>>ABOUT TO MOVE DATA IS "+)
				r_iter.move(keys.get(i),iter.currentData());
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
				this.data 		 = iter.data;
				this.last_opstat = iter.last_opstat;
				break;
			}
		}while(iter.isValid());
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
		((PredicateIndexIterator)r_iter).close();
	}

}
