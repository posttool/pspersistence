package com.pagesociety.bdb;


import java.util.Map;

import org.apache.log4j.Logger;

import com.sleepycat.db.DatabaseException;
import com.sun.management.OperatingSystemMXBean;

public class DefaultCheckpointPolicyWithInterval implements CheckpointPolicy
{
	private static final Logger logger = Logger.getLogger(DefaultCheckpointPolicyWithInterval.class);
	
	// TODO instead of this, check load and checkpoint when it seems ok
	// http://download.oracle.com/javase/6/docs/api/java/lang/management/package-summary.html#examples
	private static final int MAX_COUNT 									= 3000; 
	private static final int DEFAULT_CHECKPOINT_INTERVAL_IN_MINUTES 	= 1;
	private static String PARAM_CHECKPOINT_THREAD_INTERVAL_IN_MINUTES 	= "checkpoint-interval-in-minutes";
	
	protected 	int 		checkpoint_interval_in_minutes;
	private 	int 		checkpoint_count;
	protected 	BDBStore	context;
	
	Thread checkpoint_thread;
	private boolean checkpoint_thread_running;
	
	public void init(BDBStore context,Map<String, Object> config)
	{
		this.context = context;
		String s_ciim = (String)config.get(PARAM_CHECKPOINT_THREAD_INTERVAL_IN_MINUTES);
		if(s_ciim == null)
			checkpoint_interval_in_minutes = DEFAULT_CHECKPOINT_INTERVAL_IN_MINUTES;
		else
			checkpoint_interval_in_minutes = Integer.parseInt(s_ciim);
		start_checkpoint_thread();
	}

	private void start_checkpoint_thread() 
	{
		checkpoint_thread_running = true;
		checkpoint_thread		  = new Thread()
		{
			public void run()
			{
				loop:while(checkpoint_thread_running)
				{
					try{
						try{
							Thread.sleep(checkpoint_interval_in_minutes*(1000*60));
						}catch(InterruptedException ie)
						{
							continue loop;
						}
						
						synchronized (this)
						{
							context.do_checkpoint();	
						}
						
					}catch(Exception e)
					{
						logger.error(e);
						continue;
					}
				}
			}
		};
		checkpoint_thread.start();
		
	}

	public void handleCheckpoint() throws DatabaseException
	{
		checkpoint_count++;
		if (checkpoint_count == MAX_COUNT)
		{
			checkpoint_count = 0;
			synchronized (this) 
			{
				context.do_checkpoint();				
			}
			// start the checkp[oint thread over //
			checkpoint_thread.interrupt();
		}
	}

	@Override
	public void destroy() 
	{
		checkpoint_thread_running = false;
		synchronized (this) 
		{
			checkpoint_thread.interrupt();
		}

	}
}
