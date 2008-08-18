package com.pagesociety.bdb.index.iterator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.pagesociety.bdb.BDBSecondaryIndex;
import com.sleepycat.db.Cursor;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.OperationStatus;

/* iterator that takes one user param */
public abstract class SetIndexIterator extends IndexIterator
{
	protected List<DatabaseEntry> keys;
	protected DatabaseEntry 	  data;
	protected OperationStatus 	  last_opstat;
	protected int				  current_key;
	protected boolean			  globbing;
	//used_by_union
	protected Set<Long>  seen_results;
	/* serialization stuff */
	private final char 	 DELIM   = ':';
	private final String DELIM_S = ":";
	
	public void open(IterableIndex index,Object... args) throws DatabaseException
	{

		globbing 	 = (Boolean)args[0];
		keys		 =	(List<DatabaseEntry>)args[1];
		data 		 = 	new DatabaseEntry();
		current_key  = 0;
		seen_results = new HashSet<Long>();
		
	}
	
	public void resume(IterableIndex index,Object token) throws DatabaseException
	{
		decode(token);
	}

	
	/*encode more or less means getnextquery token in the old system */
	/* it means encode the state. if you have no more records there is no
	 * more state to speak of for this instance so it returns null
	 */
	public String encode()
	{
		int keyssize = keys.size();
		if(last_opstat == OperationStatus.SUCCESS)
		{
			StringBuffer buf = new StringBuffer();
			buf.append(String.valueOf(keyssize));
			for(int i = 0;i < keyssize;i++)
			{
				buf.append(IteratorUtil.databaseEntryToHexString(keys.get(i)));
				buf.append(DELIM);
			}
			buf.append(IteratorUtil.databaseEntryToHexString(data));
			buf.append(DELIM);
			buf.append(String.valueOf(current_key));
			buf.append(DELIM);
			flatten_seen_results(buf);
			return buf.toString();
		}
		return null;
	}
	
	protected void decode(Object token)
	{
		int keyssize;
		int seenresults_size;
		seen_results = new HashSet<Long>();
		keys = new ArrayList<DatabaseEntry>();
		StringTokenizer st 	= new StringTokenizer((String)token,DELIM_S,false);
		keyssize = Integer.parseInt(st.nextToken());
		for(int i = 0;i < keyssize;i++)
		{
			keys.add(IteratorUtil.hexStringToDatabaseEntry(st.nextToken()));
		}
		data 		= IteratorUtil.hexStringToDatabaseEntry(st.nextToken());
		current_key = Integer.parseInt(st.nextToken());
		seenresults_size = Integer.parseInt(st.nextToken());
		for(int i = 0;i < seenresults_size;i++)
		{
			seen_results.add(Long.parseLong(st.nextToken()));
		}
	}	
	
	private String flatten_seen_results(StringBuffer buf)
	{
		
		int size = seen_results.size();
		Iterator<Long> it = seen_results.iterator();
		buf.append(size);
		buf.append(DELIM);
		Long d;
		while(it.hasNext())
		{
			d = (Long)it.next();
			buf.append(d);
			buf.append(DELIM);
		}
		buf.setLength(buf.length()-1);
		return buf.toString();
	}



}
