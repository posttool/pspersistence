package com.pagesociety.bdb.index.query;

import com.pagesociety.bdb.index.query.pssql.PSSqlExecutor;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.pagesociety.persistence.Query;
import com.pagesociety.persistence.QueryResult;

public class QueryManager
{

	private QueryManagerConfig _config;
	private QueryExecutionEnvironment _query_exec_env;
	private QueryCacheManager _cache_manager;
	
	public QueryManager(QueryManagerConfig config)
	{
		_config = config;
		_cache_manager = new QueryCacheManager(_config.getEntityCacheInitialSize(),
													  _config.getEntityCacheLoadFactor(),
													  _config.getEntityCacheMaxSize());
		System.out.println("CACHE MANGER IS "+_cache_manager);
		_query_exec_env = new QueryExecutionEnvironment(this,
														_config.getPrimaryIndexMap(),
														_config.getSecondaryIndexMap(),
														_cache_manager);
	}
	
	public void init()
	{
		//probalby set up caches and executor pool here
	}
	
	public QueryResult executeQuery(Query q) throws PersistenceException
	{

		QueryExecutor ex = new QueryExecutor(_query_exec_env);
		return ex.execute(q); 
	}
	
	public QueryResult executePSSqlQuery(String pssql) throws PersistenceException
	{

		PSSqlExecutor ex = new PSSqlExecutor(_query_exec_env);
		return ex.execute(pssql); 
	}
	
	public int executeCount(Query q) throws PersistenceException
	{

		QueryExecutor ex = new QueryExecutor(_query_exec_env);
		return ex.executeCount(q);
	}

	
	public void cleanCache(String entity_name)
	{
		_cache_manager.clearQueryCache(entity_name);
	}
	
	
	public void calculateCacheDependencies(EntityDefinition def)
	{
		_cache_manager.calculateDependencies(def);
	}
	
	public void removeCacheDependencies(EntityDefinition def)
	{
		_cache_manager.removeDependencies(def);
	}
	
}
