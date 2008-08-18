package com.pagesociety.bdb.locker;

import java.util.HashMap;



public class JavaConcurrencyReentrantReadWriteLocker implements Locker 
{
	
	private java.util.concurrent.locks.ReadWriteLock _lock = new java.util.concurrent.locks.ReentrantReadWriteLock();
	private java.util.concurrent.locks.Lock   _read_lock;
	private java.util.concurrent.locks.Lock   _write_lock;
	
	public JavaConcurrencyReentrantReadWriteLocker() 
	{
		init(null);
	}
	
	public void enterAppThread() 
	{
		_read_lock.lock();	
		//System.out.println(Thread.currentThread()+" GOT READ LOCK");
	}

	public void enterLockerThread() 
	{
		_write_lock.lock();
		//System.out.println(Thread.currentThread()+" GOT WRITE LOCK");
	}

	public void exitAppThread() 
	{
		_read_lock.unlock();
		//System.out.println(Thread.currentThread()+" RELEASED READ LOCK");
	}

	public void exitLockerThread() 
	{
		_write_lock.unlock();
		//System.out.println(Thread.currentThread()+" RELEASED WRITE LOCK");
	}

	public void init(HashMap<Object, Object> config) {

		_lock = new java.util.concurrent.locks.ReentrantReadWriteLock(false);	
		_read_lock = _lock.readLock();
		_write_lock = _lock.writeLock();
	}

	
	////test stuff//
	public class T extends Thread
	{
		JavaConcurrencyReentrantReadWriteLocker locker;
		public T(String name,JavaConcurrencyReentrantReadWriteLocker locker)
		{
			super(name);
			this.locker = locker;
		}
		
		public void run()
		{
			float f;
			if(getName().startsWith("R"))
				locker.enterAppThread();
			else
				locker.enterLockerThread();
			for(int i = 0;i < 500000000;i++)
				f = (float)i;
			if(getName().startsWith("R"))
				locker.exitAppThread();
			else
				locker.exitLockerThread();
			
			
		}
	}
	
	public static void main(String[] args)
	{
		JavaConcurrencyReentrantReadWriteLocker locker = new JavaConcurrencyReentrantReadWriteLocker();
		locker.init(null);
		T[] readers = new T[24];
		T[] writers = new T[5];

		for(int i = 0;i < 12;i++)
		{
			readers[i] = locker.new T("Reader "+i,locker);
			readers[i].start();
		}

		
		for(int i = 0;i < writers.length;i++)
		{
			writers[i] = locker.new T("Writer "+i,locker);
			writers[i].start();
		}

		for(int i = 12;i < 24;i++)
		{
			readers[i] = locker.new T("Reader "+i,locker);
			readers[i].start();
		}
		
	}
}
