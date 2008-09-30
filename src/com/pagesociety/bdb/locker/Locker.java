package com.pagesociety.bdb.locker;


import java.util.Map;

public interface Locker 
{
	public void init(Map<String, Object> config);
	public void enterAppThread();
	public void exitAppThread();
	public void enterLockerThread();
	public void exitLockerThread();
		
}
