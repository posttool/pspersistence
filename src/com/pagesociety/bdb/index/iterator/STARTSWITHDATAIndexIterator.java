package com.pagesociety.bdb.index.iterator;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.Cursor;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.Transaction;

public class STARTSWITHDATAIndexIterator extends PredicateIndexIterator implements RespositionableIndexIterator
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
		//SEE SAME COMMENT IN STARTSWITHSTRINGIndexIterator
		original_param_length = original_param.getSize();	
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
		validate_position();
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

	private void validate_position()
	{
		//System.out.println("!!!!!!!!!!!VALIDATE POSITION: ok "+new String(original_param.getData())+" found "+ new String(key.getData()));
		if(last_opstat == OperationStatus.SUCCESS)
		{
			int c = 0;
			if((c = IteratorUtil.compareDatabaseEntries(original_param, 0, original_param_length, key, 0, original_param_length)) != 0)
			{
				last_opstat = OperationStatus.NOTFOUND;
				//System.out.println("FAILED VALIDATING PROSITION");
			}
		}		
	}

	public void move(DatabaseEntry newkey) throws DatabaseException
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
	
	//this is only used by globbed SETCONTAINSALL
	//TODO: NOTE: this shit is sort of crazy....
	//...the reason we do this is because you can't move
	//to a partial key with the data specified.searchkeyrange()
	//does not allow us to specify the data//
	
	/*essentially move with partial key and known data*/
	public void move(DatabaseEntry newkey,DatabaseEntry newdata) throws DatabaseException
	{
		Cursor reverse_cursor = index.getReverseIndexDbh().openCursor(null, null);
		original_param  	  = IteratorUtil.cloneDatabaseEntry((DatabaseEntry)newkey);
		original_param_length = original_param.getSize();

		key = IteratorUtil.cloneDatabaseEntry(newkey);
		   	
		last_opstat	=	reverse_cursor.getSearchBothRange(newdata,key, LockMode.DEFAULT);
		if(IteratorUtil.compareDatabaseEntries(key, 0, original_param_length, original_param, 0, original_param_length)==0)
		{
			last_opstat = index_cursor.getSearchBoth(key, newdata, LockMode.DEFAULT);				
			reverse_cursor.close();
			return;
		}

		last_opstat = OperationStatus.NOTFOUND;
		reverse_cursor.close();
	}
	
	/* only called by globbing freetextcontainsall iterator...this is unique because
	 * both key and data are are partial instead of just one being partial */
	
	/* essentially move with partial key AND partial data */
	public void moveWithPartialData(DatabaseEntry newkey,DatabaseEntry newdata) throws DatabaseException
	{
		Cursor reverse_cursor = index.getReverseIndexDbh().openCursor(null, null);
		DatabaseEntry original_data  	  = IteratorUtil.cloneDatabaseEntry(newdata);
		int original_data_length = original_data.getSize();
		DatabaseEntry original_key   		= IteratorUtil.cloneDatabaseEntry(newkey);
		int original_key_length   = original_key.getSize();

		/* we do reverse cursor again so we just search that record instead of all records
		 * containing that word.we are forced to do a mini table scan in this case since
		 * pos data is part of freetext index pkey. 
		 * 
		 */
		
		last_opstat	=	reverse_cursor.getSearchKeyRange(newdata,key, LockMode.DEFAULT);
		if(IteratorUtil.compareDatabaseEntries(newdata, 0, original_data_length, original_data, 0, original_data_length)==0)
		{
			do{
				if((IteratorUtil.compareDatabaseEntries(newdata, 0, original_data_length, original_data, 0, original_data_length)==0)
				&&(IteratorUtil.compareDatabaseEntries(key, 0, original_key_length, original_key, 0, original_key_length)==0))
				{
					index_cursor.getSearchBoth(key, newdata, LockMode.DEFAULT);
					reverse_cursor.close();
					return;
				}
			}while((last_opstat = reverse_cursor.getNext(newdata, key, LockMode.DEFAULT)) == OperationStatus.SUCCESS);
		}

		last_opstat = OperationStatus.NOTFOUND;
		reverse_cursor.close();
	}
	

}


