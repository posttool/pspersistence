package com.pagesociety.bdb.locker;

import java.util.HashMap;

public interface Locker 
{
	public void init(HashMap<Object, Object> config);
	public void enterAppThread();
	public void exitAppThread();
	public void enterLockerThread();
	public void exitLockerThread();
		
}
