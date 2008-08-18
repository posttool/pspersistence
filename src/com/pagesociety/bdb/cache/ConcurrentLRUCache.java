package com.pagesociety.bdb.cache;

import java.util.LinkedHashMap;
import java.util.Map;

import com.pagesociety.bdb.locker.Locker;

public class ConcurrentLRUCache<K,V> 
{

	private lru_map<K, V>  _map;
	private Locker _locker;

	public ConcurrentLRUCache(int initialCapacity,float loadFactor,int maxSize,Locker locker)
	{
		_map = new lru_map<K,V>(initialCapacity,loadFactor,maxSize);
		_locker = locker;
	}

	public V get(K key) 
	{
		_locker.enterAppThread(); try { return _map.get(key); } finally { _locker.exitAppThread(); }
	}
	
	@SuppressWarnings("unchecked")
	public K[] allKeys() 
	{
		_locker.enterAppThread(); try { return (K[])_map.keySet().toArray(); } finally { _locker.exitAppThread(); }
	}

	public Object put(K key, V  value) 
	{
		_locker.enterLockerThread(); try { return _map.put(key, value); } finally { _locker.exitLockerThread(); }
	}
	
	public void clear() 
	{
		_locker.enterLockerThread(); try { _map.clear(); } finally { _locker.exitLockerThread(); }
	}

	public int size()
	{
		_locker.enterAppThread(); try { return _map.size();} finally { _locker.exitLockerThread(); }
	}
	
	public void setMaxCapacity(int new_max)
	{
		_locker.enterLockerThread(); try { _map.max_capacity = new_max; } finally { _locker.exitLockerThread(); }
	}
	
	class lru_map<K0,V0> extends LinkedHashMap<K0,V0> 
	{  	
		
		private static final long serialVersionUID = 1L;
		public int max_capacity;  
		          
		public lru_map(int initialCapacity, float loadFactor, int maxCapacity) 
		{  
		   super(initialCapacity, loadFactor, true);  
		   this.max_capacity = maxCapacity;  
		}  

		protected boolean removeEldestEntry(Map.Entry<K0,V0> eldest) 
		{  
			return size() >= this.max_capacity;  
		}  
	}  

}
