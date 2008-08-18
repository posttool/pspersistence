package com.pagesociety.bdb.index.iterator;

import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;

public class BETWEEN_ASC_START_INCLUSIVEIndexIterator extends BETWEEN_ASC_INCLUSIVEIndexIterator
{
	public void next() throws DatabaseException
	{
		last_opstat =  index_cursor.getNextDup(key, data, LockMode.DEFAULT);	
		if(last_opstat == OperationStatus.NOTFOUND)
		{
			last_opstat = index_cursor.getNextNoDup(key, data, LockMode.DEFAULT);
			if(last_opstat == OperationStatus.SUCCESS)
			{
				int l1 = key.getSize();
				int l2 = terminal_key_length;
				//int l = (l1 < l2)?l1:l2;
				if(IteratorUtil.compareDatabaseEntries(key, 0, l1, terminal_key, 0, l2) >= 0)
					last_opstat =  OperationStatus.NOTFOUND;
			}
		}
	}

}
