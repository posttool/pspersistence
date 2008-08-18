package com.pagesociety.bdb.index.query;

import java.util.Map;

import com.pagesociety.bdb.BDBPrimaryIndex;
import com.pagesociety.bdb.BDBSecondaryIndex;

public class QueryManagerConfig 
{
	private Map<String,BDBPrimaryIndex> _primary_index_map;
	private Map<String,Map<String,BDBSecondaryIndex>> _secondary_index_map;
	private int   _entity_cache_initial_size;
	private float _entity_cache_load_factor;
	private int   _entity_cache_max_size;
	
	public void setPrimaryIndexMap(Map<String,BDBPrimaryIndex> primary_index_map)
	{
		_primary_index_map = primary_index_map;
	}
	
	public void setSecondaryIndexMap(Map<String,Map<String,BDBSecondaryIndex>> secondary_index_map)
	{
		_secondary_index_map = secondary_index_map;
	}
	
	public Map<String,BDBPrimaryIndex> getPrimaryIndexMap()
	{
		return _primary_index_map;
	}
	
	public Map<String,Map<String,BDBSecondaryIndex>> getSecondaryIndexMap()
	{
		return _secondary_index_map;
	}
	
	public void setEntityCacheInitialSize(int size)
	{
		_entity_cache_initial_size = size;
	}
	
	public int getEntityCacheInitialSize()
	{
		return _entity_cache_initial_size;
	}
	
	public void setEntityCacheLoadFactor(float f)
	{
		_entity_cache_load_factor = f;
	}
	
	public float getEntityCacheLoadFactor()
	{
		return _entity_cache_load_factor;
	}
	public void setEntityCacheMaxSize(int ms)
	{
		_entity_cache_max_size = ms;
	}
	
	public int getEntityCacheMaxSize()
	{
		return _entity_cache_max_size;
	}
	
}
