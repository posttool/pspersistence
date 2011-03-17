package com.pagesociety.bdb;


import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sleepycat.db.DatabaseException;

public class SystemLoadCheckpointProcessor implements CheckpointPolicy
{
	private static final Logger logger = Logger.getLogger(DefaultCheckpointPolicyWithInterval.class);
	
	
	protected 	int 		checkpoint_interval_in_minutes;
	protected 	BDBStore	context;
	
	Thread checkpoint_thread;
	private boolean checkpoint_thread_running;
	private int num_checkpoint_requests;
	
	public void init(BDBStore context,Map<String, Object> config)
	{
		this.context = context;
		reset_num_checkpoint_requests();
		start_checkpoint_thread();
	}
	//good reference http://epcc.sjtu.edu.cn/~jzhou/research/publications/2010_dsn.pdf
	//the adaptive checkpointing part

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
							Thread.sleep((1000*5));//one minute
						}catch(InterruptedException ie)
						{
							continue loop;
						}
						
						synchronized (this)
						{
							update_load_ewma();
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
	
	private int last_observed_rpm 	= -1;
	private float last_load 		= -1;
	private float alpha 			= 0.7f; //bigger values discard the history faster
	private float beta				= 1.0f;
	private float current_load 		= -1;
	private float threshold			= -1;
	private float low_watermark  = 2f; //in requests every 5 seconds..for now
	private float  high_watermark = 30f;//in requests every 5 seconds..for now
	private float lower_bound		= 5;
	private float upper_bound		= 20;
	private void update_load_ewma()
	{

		synchronized(this)
		{
		int observed_rpm 		= num_checkpoint_requests;
		System.out.println("NUM CHECKPOINT REQUESTS IN 5 SECONDS IS "+observed_rpm);
		if(last_load == -1)
			current_load = alpha * observed_rpm;
		else
			current_load = (alpha * observed_rpm + (1 - alpha) * last_load);	
		

		last_load = current_load;
		calculate_threshold();
		reset_num_checkpoint_requests();
		System.out.println("CURRENT THRESHOLD IS "+threshold);
		System.out.println("CURRENT LOAD "+current_load);
		if(current_load > threshold)
			System.out.println("!!!!!!!!!!!!!!!!! CHECKPOINT");
			try{
			if(context != null)
				context.do_checkpoint();				
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	private float get_current_load()
	{
		return current_load;
	}
	
	private float load_f(float load)
	{
		if(load <= low_watermark)
			return 0;
		if(load >= high_watermark)
			return 1;
		else
		{
			return ((load - low_watermark)/(high_watermark - low_watermark))*beta; 
		}
	}
	
	private void calculate_threshold()
	{
		threshold = lower_bound + load_f(current_load) * (upper_bound - lower_bound);
	}
	
	
	
	private void reset_num_checkpoint_requests()
	{
		num_checkpoint_requests = 0;
	}
	
	public void handleCheckpoint() throws DatabaseException
	{
		synchronized (this) {
			num_checkpoint_requests++; //keep accumulating sample data//
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
	
	
	
	public static void main(String[] args)
	{
		final SystemLoadCheckpointProcessor test = new SystemLoadCheckpointProcessor();
		test.reset_num_checkpoint_requests();
		test.start_checkpoint_thread();
		
		Thread t = new Thread()
		{
			
			public void run()
			{
				while(true)
				{
					
					long interval = (long)(Math.random() * 250 + 100); 
					try{
						test.handleCheckpoint();
						Thread.sleep(interval);
						if(Math.random() > 0.8)
							Thread.sleep(3000);
					}catch(Exception e)
					{
						e.printStackTrace();
					}
				}
				
				
			}
			
		};
		t.start();

	}
}
