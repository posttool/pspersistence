package com.pagesociety.bdb;

import java.util.HashMap;

public class DefaultCheckpointPolicy implements CheckpointPolicy
{
	private static final int MAX_COUNT = 1000; // records
	private static final int MAX_TIMEOUT = 5000; // ms
	private int checkpoint_count;
	private long _last_checkpoint;
	private boolean _use_timeout;

	public void init(HashMap<Object, Object> config)
	{
		_last_checkpoint = System.currentTimeMillis();
		_use_timeout = true; // TODO setup in config...
	}

	public boolean isCheckpointNecessary()
	{
		checkpoint_count++;
		if (checkpoint_count == MAX_COUNT)
		{
			checkpoint_count = 0;
			return true;
		}
		
		long l = System.currentTimeMillis();
		if (_use_timeout && ((l - MAX_TIMEOUT) > _last_checkpoint))
		{
			_last_checkpoint = l;
			return true;
		}
		
		return false;
	}
}
