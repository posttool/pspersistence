package com.pagesociety.bdb;


import java.util.Map;

import org.apache.log4j.Logger;

import com.sleepycat.db.DatabaseException;
import com.sun.xml.internal.bind.CycleRecoverable.Context;

public class DefaultCheckpointPolicy implements CheckpointPolicy
{
	private static final Logger logger = Logger.getLogger(DefaultCheckpointPolicy.class);
	private static final int MAX_COUNT = 1000; // records
	private static final int MAX_TIMEOUT = 5000; // ms
	private int checkpoint_count;
	private long _last_checkpoint;
	private boolean _use_timeout;
	protected BDBStore context;
	public void init(BDBStore context,Map<String, Object> config)
	{
		this.context = context;
		_last_checkpoint = System.currentTimeMillis();
		_use_timeout = true; // TODO setup in config...
	
	}

	public void handleCheckpoint() throws DatabaseException
	{
		checkpoint_count++;
		if (checkpoint_count == MAX_COUNT)
		{
			checkpoint_count = 0;
			context.do_checkpoint();
		}
		
		long l = System.currentTimeMillis();
		if (_use_timeout && ((l - MAX_TIMEOUT) > _last_checkpoint))
		{
			_last_checkpoint = l;
			context.do_checkpoint();
		}
		
		
	}

	@Override//we dont use a thread in this one
	public void destroy() 
	{
		try {
			context.do_checkpoint();
		} catch (DatabaseException e) {
			
			logger.error(e);
		}
	}
}
