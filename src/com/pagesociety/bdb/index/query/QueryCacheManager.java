package com.pagesociety.bdb.index.query;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.Types;

import com.pagesociety.bdb.cache.ConcurrentLRUCache;
import com.pagesociety.bdb.locker.JavaConcurrencyReentrantReadWriteLocker;

public class QueryCacheManager 
{
	private static final boolean IGNORE_DEPENDENCIES = true;
	
	private ConcurrentMap<String,ConcurrentLRUCache<String,Object>> _entity_query_caches;
	private int 	_initial_lru_cache_size;
	private float 	_lru_load_factor;
	private int 	_max_lru_cache_size;
	private ConcurrentLRUCache<String, Object> _next_entity_cache;
	
	public QueryCacheManager(int initial_lru_cache_size,float lru_load_factor,int max_lru_cache_size)
	{
		_initial_lru_cache_size = initial_lru_cache_size;
		_lru_load_factor 		= lru_load_factor;
		_max_lru_cache_size		= max_lru_cache_size;
		_entity_query_caches 	= new ConcurrentHashMap<String, ConcurrentLRUCache<String,Object>>(32);
		_next_entity_cache		= new ConcurrentLRUCache<String,Object>(_initial_lru_cache_size,
																			_lru_load_factor,
																			_max_lru_cache_size,
																			new JavaConcurrencyReentrantReadWriteLocker());
	}

	public ConcurrentLRUCache<String, Object> getQueryCache(String entity_name)
	{
		try{
		if(_entity_query_caches.putIfAbsent(entity_name,_next_entity_cache)== null)
		{
			_next_entity_cache = new ConcurrentLRUCache<String, Object>(_initial_lru_cache_size,
																			_lru_load_factor,
																			_max_lru_cache_size,
																			new JavaConcurrencyReentrantReadWriteLocker());
		}
		return _entity_query_caches.get(entity_name);
		}catch(NullPointerException npe)//this try/catch is just for debugging to track down
		{								//password cleaner bug.
			System.out.println("ENTITY_NAME WAS "+entity_name);
			throw npe;
		}
	}

	public void clearQueryCache(String entity_name)
	{
		ConcurrentLRUCache<String, Object> e_cache 	 = null;
		if((e_cache = _entity_query_caches.get(entity_name)) == null)
			return;
		else
			e_cache.clear();
	
		List<String> dependencies = _dependency_map.get(entity_name);
		//System.out.println("CLEARING QUERY CACHE FOR "+entity_name);
		if(dependencies == null || IGNORE_DEPENDENCIES)
			return;
		int s = dependencies.size();
		for(int i = 0;i < s;i++)
		{
			String dep_name = dependencies.get(i);
			if((e_cache = _entity_query_caches.get(dep_name)) == null)
				return;
			else
			{
				e_cache.clear();
				System.out.println("CLEARING QUERY CACHE FOR DEPENDENT"+dep_name);
			}
		}
	}

	
	private Map<String,List<String>> _dependency_map = new HashMap<String,List<String>>();
	public void calculateDependencies(EntityDefinition def)
	{
		String entity_name = def.getName();
		List<FieldDefinition> fields = def.getFields();
		int s = fields.size();
		for(int i = 0;i < s;i++)
		{
			FieldDefinition f = fields.get(i);
			if((f.getBaseType() & Types.TYPE_REFERENCE) == Types.TYPE_REFERENCE)
			{
				String ref_type = f.getReferenceType();
				List<String> dep_list;
				if((dep_list = _dependency_map.get(ref_type)) == null)
				{
					dep_list = new ArrayList<String>();
					_dependency_map.put(ref_type,dep_list);
				}
				if(dep_list.contains(entity_name) || ref_type.equals(entity_name))
					continue;
				else
					dep_list.add(entity_name);
			}
		}
		//dump_all_dependencies();
	}
	
	public void removeDependencies(EntityDefinition def)
	{
		String entity_name = def.getName();
		for(String s:_dependency_map.keySet())
		{
			List<String> dd = _dependency_map.get(s);
			dd.remove(entity_name);
		}
		//dump_all_dependencies();
	}
	
	
	private void dump_all_dependencies()
	{
		System.out.println("CURRENT_DEPENDENCIES");
		for(String s:_dependency_map.keySet())
		{
			List<String> dd = _dependency_map.get(s);
			System.out.println("\t"+s+": "+dd);
		}
	}

}
