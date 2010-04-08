package com.pagesociety.bdb.index.iterator;

/* probably need to serialize original search key too!!!*/
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.Transaction;

public class STARTSWITHSTRINGIndexIterator extends PredicateIndexIterator /*implements RespositionableIndexIterator*/
{

	private int 		  original_param_length;
	private DatabaseEntry original_param;
	public void open(Transaction txn,IterableIndex index,Object... user_arg) throws DatabaseException
	{
		super.open(txn,index,user_arg);
		original_param        = IteratorUtil.cloneDatabaseEntry((DatabaseEntry)user_arg[0]);
		//TODO: NOTE THINGS COULD GET WEIRD HERE IF WE ARE NOT USING STRINGS
		//HERE WE ARE IGNORING THE NULL BYTE SO THAT IT DOESNT SCREW UP OUR COMPARE
		//WE ONLY WANT TO COMPARE THE ACTUAL STRING. NOT ITS TERMINATOR
		//IN ESSENCE I THINK WE MAY BE COMPARING UP UNTIL THE LAST BYTE-1
		//....HENCE WE HAVE STARTS WITH DATA ITERATOR WHICH DOES NOT DO THE DECREMENT
		original_param_length = original_param.getSize()-1;	
		last_opstat	=	index_cursor.getSearchKeyRange(key, data, LockMode.DEFAULT);
		validate_position();

	}
	
	public void resume(IterableIndex index,Object token) throws DatabaseException
	{
		super.resume(index,token);
		original_param_length = original_param.getSize();
		last_opstat	=	index_cursor.getSearchBothRange(key, data, LockMode.DEFAULT);
	}
	
	public void next() throws DatabaseException
	{
		last_opstat =  index_cursor.getNext(key, data, LockMode.DEFAULT);	
		if(last_opstat == OperationStatus.SUCCESS)
		{
			if(IteratorUtil.compareDatabaseEntries(original_param, 0,original_param_length, key, 0, original_param_length) != 0)
			{
				last_opstat = OperationStatus.NOTFOUND;
			}
		}
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
		return last_opstat == OperationStatus.SUCCESS;
	}
	
	public boolean isDone()
	{
		return last_opstat == OperationStatus.NOTFOUND;
	}

	public void move(DatabaseEntry newkey) throws DatabaseException
	{
		//System.out.println("NEWKEY IS "+new String(newkey.getData()));

		original_param  = IteratorUtil.cloneDatabaseEntry((DatabaseEntry)newkey);;
		original_param_length = original_param.getSize()-1;
		
		key = newkey;
		last_opstat	=	index_cursor.getSearchKeyRange(key, data, LockMode.DEFAULT);	
		
		//TODO: NOTE THINGS COULD GET WEIRD HERE IF WE ARE NOT USING STRINGS
		//HERE WE ARE IGNORING THE NULL BYTE SO THAT IT DOESNT SCREW UP OUR COMPARE
		//WE ONLY WANT TO COMPARE THE ACTUAL STRING. NOT ITS TERMINATOR
		//IN ESSENCE I THINK WE MAY BE COMPARING UP UNTIL THE LAST BYTE-1
		//....HENCE WE HAVE STARTS WITH DATA ITERATOR WHICH DOES NOT DO THE DECREMENT
		validate_position();
	}

	public void move(DatabaseEntry newkey,DatabaseEntry newdata) throws DatabaseException
	{
		//System.out.println("NEWKEY IS "+new String(newkey.getData()));


		original_param  = IteratorUtil.cloneDatabaseEntry((DatabaseEntry)newkey);;
		//TODO: NOTE THINGS COULD GET WEIRD HERE IF WE ARE NOT USING STRINGS
		//HERE WE ARE IGNORING THE NULL BYTE SO THAT IT DOESNT SCREW UP OUR COMPARE
		//WE ONLY WANT TO COMPARE THE ACTUAL STRING. NOT ITS TERMINATOR
		//IN ESSENCE I THINK WE MAY BE COMPARING UP UNTIL THE LAST BYTE-1
		//....HENCE WE HAVE STARTS WITH DATA ITERATOR WHICH DOES NOT DO THE DECREMENT
		//SEE SAME COMMENT IN STARTSWITHSTRINGIndexIterator
		original_param_length = original_param.getSize();
		
		key = newkey;
		last_opstat	=	index_cursor.getSearchKeyRange(key, data, LockMode.DEFAULT);	
		
		validate_position();
	}
	
	private void validate_position()
	{
		//System.out.println("!!!!!!!!!!!VALIDATE POSITION: ok "+new String(original_param.getData())+" found "+ new String(key.getData()));
		if(last_opstat == OperationStatus.SUCCESS)
		{
			int c = 0;
			if((c = IteratorUtil.compareDatabaseEntries(original_param, 0, original_param_length, key, 0, original_param_length)) != 0)
			{
				last_opstat = OperationStatus.NOTFOUND;
				System.out.println("FAILED VALIDATING POSITION");
			}
		}		
	}

}
