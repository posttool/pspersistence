package com.pagesociety.bdb.index.query;

import java.util.Map;

import com.pagesociety.bdb.BDBPrimaryIndex;
import com.pagesociety.bdb.BDBSecondaryIndex;
import com.pagesociety.bdb.index.iterator.IterableIndex;
import com.pagesociety.persistence.PersistenceException;

public class QueryExecutionEnvironment 
{

	private Map<String,BDBPrimaryIndex> 			  _primary_index_map;
	private Map<String,Map<String,BDBSecondaryIndex>> _secondary_index_map;
	private QueryCacheManager				_query_cache_manager;
	private QueryManager					_query_manager;
	public QueryExecutionEnvironment(QueryManager query_manager,Map<String,BDBPrimaryIndex> primary_index_map,
									Map<String,Map<String,BDBSecondaryIndex>> secondary_index_map,
									QueryCacheManager query_cache_manager)
	{
		_primary_index_map   = primary_index_map;
		_secondary_index_map = secondary_index_map;
		_query_cache_manager = query_cache_manager;
		_query_manager		 = query_manager;
	}
	
	public QueryManager getQueryManager()
	{
		return _query_manager;
	}
	
	protected IterableIndex getSecondaryIndex(String entity_name,String index_name) throws PersistenceException
	{
		Map<String,BDBSecondaryIndex> entity_indexes =  _secondary_index_map.get(entity_name);
		if(entity_indexes == null)
			throw new PersistenceException("ENTITY "+entity_name+" DOES NOT EXIST.");
		BDBSecondaryIndex idx = _secondary_index_map.get(entity_name).get(index_name);
		if(idx == null)
			throw new PersistenceException("INDEX "+index_name+" DOES NOT EXIST ON ENTITY "+entity_name);	
		return idx;
	}
	
	// this should use the provider...
	public BDBPrimaryIndex getPrimaryIndex(String entity_name) throws PersistenceException
	{
		BDBPrimaryIndex idx = _primary_index_map.get(entity_name);
		if(idx == null)
			throw new PersistenceException("ENTITY "+entity_name+" DOES NOT EXIST");
		return idx;	
	}
	
	public QueryCacheManager getQueryCacheManager()
	{
		return _query_cache_manager;
	}
	
}
