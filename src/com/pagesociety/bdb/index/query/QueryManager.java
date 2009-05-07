package com.pagesociety.bdb.index.query;

import com.pagesociety.bdb.index.query.pssql.PSSqlExecutor;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.pagesociety.persistence.Query;
import com.pagesociety.persistence.QueryResult;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.Environment;
import com.sleepycat.db.Transaction;
import com.sun.xml.internal.bind.CycleRecoverable.Context;

public class QueryManager
{

	private QueryManagerConfig _config;
	private QueryExecutionEnvironment _query_exec_env;
	private QueryCacheManager _cache_manager;
	private Environment _environment;
	public QueryManager(QueryManagerConfig config)
	{
		_config = config;
		_environment = _config.getContext().getEnvironment();
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
	
	public QueryResult executeQuery(Transaction parent_txn,Query q) throws PersistenceException
	{
		
		QueryExecutor ex = new QueryExecutor(_query_exec_env);
		//try{
		//	Transaction txn  = _environment.beginTransaction(parent_txn, null);
			QueryResult r =  ex.execute(parent_txn,q); 
		//	txn.commitNoSync();
			return r;
		//}catch(DatabaseException dbe)
		//{
		//	throw new PersistenceException("QUERY FAILED:",dbe);
		//}
	}
	
	public QueryResult executePSSqlQuery(Transaction parent_txn,String pssql) throws PersistenceException
	{
		PSSqlExecutor ex = new PSSqlExecutor(_query_exec_env);
		try{
			Transaction txn  = _environment.beginTransaction(parent_txn, null);
			QueryResult r = ex.execute(txn,pssql); 
			txn.commitNoSync();
			return r;
		}catch(DatabaseException dbe)
		{
			throw new PersistenceException("PSQL QUERY FAILED:",dbe);
		}
	}
	
	public int executeCount(Transaction parent_txn,Query q) throws PersistenceException
	{
		QueryExecutor ex = new QueryExecutor(_query_exec_env);
		return  ex.executeCount(parent_txn,q);
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
