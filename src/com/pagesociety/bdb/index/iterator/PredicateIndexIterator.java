package com.pagesociety.bdb.index.iterator;

import java.util.StringTokenizer;


import com.sleepycat.db.Cursor;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.OperationStatus;

/* iterator that takes one user param */
public abstract class PredicateIndexIterator extends IndexIterator
{
	protected IterableIndex		index;
	protected DatabaseEntry   	key;
	protected DatabaseEntry 	data;
	//protected DatabaseEntry		original_param;
	protected OperationStatus 	last_opstat;
	protected Cursor			index_cursor;

	/* serialization stuff */
	private final char 	 DELIM   = ':';
	private final String DELIM_S = ":";
	
	public void open(IterableIndex index,Object... user_args) throws DatabaseException
	{
		this.index = index;
		index_cursor = index.getDbh().openCursor(null, null);
		key			=	(DatabaseEntry)user_args[0];
		data 		= 	new DatabaseEntry();
		//original_param = IteratorUtil.cloneDatabaseEntry(key);
	}
	
	public void resume(IterableIndex index,Object token) throws DatabaseException
	{
		decode(token);
		index_cursor = index.getDbh().openCursor(null, null);
	}
	
	protected void resume(IterableIndex index,DatabaseEntry key,DatabaseEntry data) throws DatabaseException
	{
		this.key = key;
		this.data = data;
		index_cursor = index.getDbh().openCursor(null, null);
	}
	
	public void close() throws DatabaseException
	{
		index_cursor.close();
	}
	
	/*encode more or less means getnextquery token in the old system */
	/* it means encode the state. if you have no more records there is no
	 * more state to speak of for this instance so it returns null
	 */
	public String encode()
	{
		if(last_opstat == OperationStatus.SUCCESS)
		{
			StringBuffer buf = new StringBuffer();
			buf.append(IteratorUtil.databaseEntryToHexString(key));
			buf.append(DELIM);
			buf.append(IteratorUtil.databaseEntryToHexString(data));
			buf.append(DELIM);
		//	buf.append(IteratorUtil.databaseEntryToHexString(original_param));
			return buf.toString();
		}
		return null;
	}
	
	protected void decode(Object token)
	{
		StringTokenizer st 	= new StringTokenizer((String)token,DELIM_S,false);
		key  			= IteratorUtil.hexStringToDatabaseEntry(st.nextToken());
		data 			= IteratorUtil.hexStringToDatabaseEntry(st.nextToken());
		//original_param  = IteratorUtil.hexStringToDatabaseEntry(st.nextToken());
	}		
}
