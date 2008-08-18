package com.pagesociety.bdb.locker;

import java.util.HashMap;


public class AdminLocker implements Locker
{
	private Object  _mutex 				    = new Object();
	private boolean _admin_thread_is_active = false;
	private int _num_waiting_admin_threads  = 0;
	private int _num_app_threads 			= 0;
	
	public void init(HashMap<Object, Object> config)
	{
		
	}
	public void enterAppThread() 
	{
		long t1 = System.currentTimeMillis();
		
		synchronized (_mutex) 
		{
			while(_admin_thread_is_active || _num_waiting_admin_threads > 0)
			{
				try {
					_mutex.wait();
				} catch (InterruptedException e) {
				}
			}
			_num_app_threads++;
		}
		//System.out.println("ENTER APP THREAD LOCK ACQUIRE TOOK "+(System.currentTimeMillis()-t1));
		 
	}
	
	public void exitAppThread()
	{
		long t1 = System.currentTimeMillis();
		thread_exit();
		//System.out.println("EXIT APP THREAD LOCK ACQUIRE TOOK "+(System.currentTimeMillis()-t1));
	}

	public void enterLockerThread()
	{
	
		synchronized(_mutex)
		{
			//System.out.println("ENTER ADMIN NUM APP THREADS IS..."+_num_app_threads);
			_num_waiting_admin_threads++;			
			while(_num_app_threads != 0 || _admin_thread_is_active)
			{
				try {
					_mutex.wait();
				} catch (InterruptedException e) {
				}
			}
			_num_waiting_admin_threads--;
			_admin_thread_is_active = true;
		}	
		 
	}
	
	public void exitLockerThread()
	{
	//	System.out.println("EXIT ADMIN.......................");
		thread_exit();
	}

	private void thread_exit()
	{
		synchronized (_mutex) 
		{
			 //check for errors
		     // if ((num_app_threads == 0) && (!admin_thread_is_active)) {
		      //  System.err.println(" !!!!!!!!!!!!!!!!!!!!!!!!!!!!Error: Invalid call to release the lock");
		       // return;
		      //}
		      if (_admin_thread_is_active)
		      {
		        _admin_thread_is_active = false;
		      }
		      else
		        _num_app_threads--;

		      _mutex.notifyAll();		
		}
	}
	
}
