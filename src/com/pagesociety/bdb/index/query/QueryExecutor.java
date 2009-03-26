 package com.pagesociety.bdb.index.query;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import com.pagesociety.bdb.BDBPrimaryIndex;
import com.pagesociety.bdb.BDBQueryResult;
import com.pagesociety.bdb.binding.FieldBinding;
import com.pagesociety.bdb.cache.ConcurrentLRUCache;
import com.pagesociety.bdb.index.ArrayMembershipIndex;
import com.pagesociety.bdb.index.SingleFieldFreeTextIndex;
import com.pagesociety.bdb.index.MultiFieldArrayMembershipIndex;
import com.pagesociety.bdb.index.MultiFieldFreeTextIndex;
import com.pagesociety.bdb.index.SimpleMultiFieldIndex;
import com.pagesociety.bdb.index.SimpleSingleFieldIndex;
import com.pagesociety.bdb.index.iterator.BETWEEN_ASC_END_INCLUSIVEIndexIterator;
import com.pagesociety.bdb.index.iterator.BETWEEN_ASC_EXCLUSIVEIndexIterator;
import com.pagesociety.bdb.index.iterator.BETWEEN_ASC_INCLUSIVEIndexIterator;
import com.pagesociety.bdb.index.iterator.BETWEEN_ASC_START_INCLUSIVEIndexIterator;
import com.pagesociety.bdb.index.iterator.BETWEEN_DESC_END_INCLUSIVEIndexIterator;
import com.pagesociety.bdb.index.iterator.BETWEEN_DESC_EXCLUSIVEIndexIterator;
import com.pagesociety.bdb.index.iterator.BETWEEN_DESC_INCLUSIVEIndexIterator;
import com.pagesociety.bdb.index.iterator.BETWEEN_DESC_START_INCLUSIVEIndexIterator;
import com.pagesociety.bdb.index.iterator.EQIndexIterator;
import com.pagesociety.bdb.index.iterator.FREETEXTCONTAINSALLIndexIterator;
import com.pagesociety.bdb.index.iterator.FREETEXTCONTAINSANYIndexIterator;
import com.pagesociety.bdb.index.iterator.FREETEXTCONTAINSPHRASEIndexIterator;
import com.pagesociety.bdb.index.iterator.FREETEXTMULTIWRAPPERIterator;
import com.pagesociety.bdb.index.iterator.GTEIndexIterator;
import com.pagesociety.bdb.index.iterator.GTIndexIterator;
import com.pagesociety.bdb.index.iterator.IndexIterator;
import com.pagesociety.bdb.index.iterator.IterableIndex;
import com.pagesociety.bdb.index.iterator.LTIndexIterator;
import com.pagesociety.bdb.index.iterator.PredicateIndexIterator;
import com.pagesociety.bdb.index.iterator.RangeIndexIterator;
import com.pagesociety.bdb.index.iterator.SETCONTAINSANYIndexIterator;
import com.pagesociety.bdb.index.iterator.SETCONTAINSALLIndexIterator;
import com.pagesociety.bdb.index.iterator.STARTSWITHSTRINGIndexIterator;
import com.pagesociety.bdb.index.iterator.SetIndexIterator;
import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.pagesociety.persistence.Query;
import com.pagesociety.persistence.QueryResult;
import com.pagesociety.persistence.Query.QueryNode;
import com.pagesociety.persistence.util.EntityIdComparator;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;


public class QueryExecutor 
{
	private static final Logger logger = Logger.getLogger(QueryExecutor.class);
	
	private QueryExecutionEnvironment _env;
	/* get params out of here so we can have one instance of the executor */
	private Object[] _query_params;
	public QueryExecutor(QueryExecutionEnvironment env)
	{
		_env = env;
	}

	private QueryResult eval(QueryNode node) throws PersistenceException
	{
		
		switch(node.type)
		{
			case 0: //root node 
// FIXME
//				if (node.children.size()==0)
//					throw new RuntimeException("QueryExecutor: THERE MUST BE AT LEAST ONE CHILD OF "+node.type);
				return eval(node.children.get(0));		
			case Query.NODE_TYPE_INTERSECTION:
				return do_intersection(node);
			case Query.NODE_TYPE_UNION:
				return do_union(node);
			case Query.NODE_TYPE_ITER:
				//TODO: we would probably cache on this level
				//need to review cache keys and how this would
				//work for simple and complex queries
				return do_iter(node);
			default:
					throw new PersistenceException("UNKNOWN NODE TYPE IN QUERY: "+node.type);
		}
	}
	
	private List<Long> eval_count(QueryNode node) throws PersistenceException
	{
		
		switch(node.type)
		{
			case 0: //root node 
				return eval_count(node.children.get(0));		
			case Query.NODE_TYPE_INTERSECTION:
				return count_intersection(node);
			case Query.NODE_TYPE_UNION:
				return count_union(node);
			case Query.NODE_TYPE_ITER:
				return count_iter(node);
			default:
					throw new PersistenceException("UNKNOWN NODE TYPE IN QUERY: "+node.type);
		}
	}
	
	
	public QueryResult execute(Query q) throws PersistenceException
	{
		long t1;
		//System.out.println("EXECUTE "+q);
		
		/* we want to always check and store results based on this 
		 * cache key and the data as it is at the beginning of the query
		 */
		_query_params = q.getParams();//copy_params(q.getParams());

		String real_cache_key 	= get_the_cache_key(q,_query_params);		
		String return_type 		= q.getReturnType();
		boolean cached_query 	= q.getCacheResults();	
		
		QueryResult result = null;
		if(cached_query)
		{
			//ConcurrentLRUCache<String,Object> qc = (ConcurrentLRUCache<String,Object>)_env.getQueryCacheManager().getQueryCache(return_type);
			//synchronized(qc)
			//{
				
			//}
			result = get_cached_results(return_type,real_cache_key);
		}
		if(result == null)
		{
			//System.out.println("!!!!NO CACHE HIT!!!!! FOR "+real_cache_key);
			t1 = System.currentTimeMillis();		
			QueryNode root 	   = q.getRootNode();
			result = eval(root); 	
			//System.out.println("INTERNAL EXECUTE TOOK "+(System.currentTimeMillis() - t1));	
			//if(result.size() != 0)
				//System.out.println("RESULT SIZE IS "+result.size()+" LAST ID IS "+result.getEntities().get(result.size()-1).getId());	
			String order_attribute = (String)root.attributes.get(Query.ATT_ORDER_FIELDNAME);
			if(order_attribute != null)
			{
				int direction = (Integer)root.attributes.get(Query.ATT_ORDER_ORDER);
				do_order_by(result, order_attribute,direction);
				
			}
			if(cached_query)
				put_cached_results(return_type,real_cache_key, result);
		}
		else
		{
			//System.out.println("!!!!CACHE HIT!!!!! FOR "+real_cache_key);
		}
		
		int from_index 	= q.getOffset();
		int s 			= result.size();
		if(from_index > s)
			return BDBQueryResult.EMPTY_RESULT;
		
		int to_index   = from_index + q.getPageSize();//to is exclusive		
		to_index = (to_index > s )?s :to_index;  
		QueryResult ret = new BDBQueryResult(result.getEntities().subList(from_index, to_index));
		return (QueryResult)ret;
	}
	
	public int executeCount(Query q) throws PersistenceException
	{
		_query_params = copy_params(q.getParams());
		String real_cache_key 	= get_the_cache_key(q,_query_params);		
		String return_type 		= q.getReturnType();
		Integer c = get_cached_count(return_type,real_cache_key);
		if(c == null)
		{		
			QueryNode root 	   = q.getRootNode();
			int s = eval_count(root).size(); 
			put_cached_count(return_type, real_cache_key, s);
			return s;
		}
		else
		{
			return c;
		}
	}
	

	//TODO optimize
	private Object[] copy_params(Object[] params)
	{
		int qplen = params.length;
		Object[] copy = new Object[qplen];
		System.arraycopy(params, 0, copy, 0, qplen);
		for (int i=0; i<qplen; i++)
		{
			if (copy[i] != null && copy[i].getClass() == ArrayList.class)
			{
				ArrayList<?> al_copy = (ArrayList<?>)copy[i];
				copy[i] = al_copy.clone();
			}
		}
		return copy;
	}
	
	private static String get_the_cache_key(Query q,Object[] query_params)
	{
		//System.out.println("CACHEKEY IS "+q.getCacheKey());
		//System.out.println("PARAMS ARE :");
		StringBuffer param_key = new StringBuffer();
		for(int i = 0;i < q.getNumParams();i++)
		{
			//System.out.println("\t"+query_params[i]);
			param_key.append(query_params[i]);
		}
		return q.getCacheKey()+param_key.toString();
	}
	
	private QueryResult get_cached_results(String entity_type,String key)
	{
		return (QueryResult)_env.getQueryCacheManager().getQueryCache(entity_type).get(key);
	}
	
	private void put_cached_results(String entity_type,String key,QueryResult result)
	{
		_env.getQueryCacheManager().getQueryCache(entity_type).put(key,result);
	}
	
	private Integer get_cached_count(String entity_type,String key)
	{
		key = "CNT:"+key;
		return (Integer)_env.getQueryCacheManager().getQueryCache(entity_type).get(key);
	}
	
	private void put_cached_count(String entity_type,String key,Integer count)
	{
		key = "CNT:"+key;
		_env.getQueryCacheManager().getQueryCache(entity_type).put(key,count);
	}

	private void do_order_by(QueryResult result,String order_attribute,int direction) throws PersistenceException
	{
		int i = 0;
		int s = result.size();
		Entity valid_instance = null;
		List<Entity> result_entities = result.getEntities();
		while(i < s && (valid_instance = result_entities.get(i)) == null)
				i++;
		if(valid_instance == null)
			return;
		else
		{
			
			EntityDefinition def = _env.getPrimaryIndex(valid_instance.getType()).getEntityDefinition();
			Comparator<Entity> comp = EntityComparatorFactory.getComparator(def, order_attribute,direction);
			Collections.sort(result.getEntities(),comp);
		}
	}

	private QueryResult do_intersection(QueryNode intersection_node) throws PersistenceException
	{
		List<QueryResult> intersectees = new ArrayList<QueryResult>();
		QueryResult qr;
		int s = intersection_node.children.size();
		for(int i = 0;i < s;i++)
		{
			qr = eval(intersection_node.children.get(i));
			if(qr.size() == 0)
				return BDBQueryResult.EMPTY_RESULT;
			intersectees.add(qr);
		}
		
		ArrayList<Entity> merge = new ArrayList<Entity>(512);
		/* merge */
		s = intersectees.size();
		for(int i = 0; i< s;i++)
			merge.addAll(intersectees.get(i).getEntities());

		BDBQueryResult inter 		= new BDBQueryResult();
		EntityIdComparator comp = new EntityIdComparator();
		long t1 = System.currentTimeMillis();
		/* sort */
		Collections.sort(merge, comp);
		/* intersect */
		long last_id = -1;
		s = merge.size();
		int c = 1;
		int is = intersectees.size();
		for(int i = 0;i < s;i++)
		{
			Entity e = merge.get(i);
			long id = e.getId();
			
			if(id == last_id)
				c++;
			else
				c = 1;
			
			if(c == is)
				inter.add(e);
			last_id = id;
		}
		long t2 = System.currentTimeMillis() - t1;
		//System.out.println("do_fast_intersection(Query) -  FAST INTERSECT " + t2);
		return inter;
	}
	
	private static final List<Long> EMPTY_LONG_LIST = new ArrayList<Long>(0);
	private List<Long> count_intersection(QueryNode intersection_node) throws PersistenceException
	{
		List<List<Long>> intersectees = new ArrayList<List<Long>>();
		List<Long> qr;
		int s = intersection_node.children.size();
		for(int i = 0;i < s;i++)
		{
			qr = eval_count(intersection_node.children.get(i));
			if(qr.size() == 0)
				return EMPTY_LONG_LIST;
			intersectees.add(qr);
		}
		
		List<Long> merge = new ArrayList<Long>(512);
		/* merge */
		s = intersectees.size();
		for(int i = 0; i< s;i++)
			merge.addAll(intersectees.get(i));

		List<Long> inter 		= new ArrayList<Long>();
		long t1 = System.currentTimeMillis();
		/* sort */
		Collections.sort(merge);
		/* intersect */
		long last_id = -1;
		s = merge.size();
		int c = 1;
		int is = intersectees.size();
		for(int i = 0;i < s;i++)
		{
			long id = merge.get(i);
			if(id == last_id)
				c++;
			else
				c = 1;
			if(c == is)
				inter.add(id);
			last_id = id;
		}
		
		long t2 = System.currentTimeMillis() - t1;
		//System.out.println("count_fast_intersection(Query) -  FAST INTERSECT " + t2);
		return inter;
	}
	
	private QueryResult do_union(QueryNode union_node) throws PersistenceException
	{
		List<QueryResult> unionees = new ArrayList<QueryResult>();
		QueryResult qr;
		int s = union_node.children.size();
		for(int i = 0;i < s;i++)
		{
			qr = eval(union_node.children.get(i));
			if(qr.size() == 0)
				continue;
			unionees.add(qr);
		}
		
		TreeMap<Long,Entity> tmap = new TreeMap<Long,Entity>();
		s = unionees.size();
		for(int i = 0; i < s;i++)
		{
			qr = unionees.get(i);
			List<Entity> ee = qr.getEntities();
			int ss = qr.size();
			for(int ii = 0; ii < ss;ii++)
			{
				Entity e = ee.get(ii);
				tmap.put(e.getId(),e);
			}
		}
		 return new BDBQueryResult(tmap.values());	
	}
	
	private List<Long> count_union(QueryNode union_node) throws PersistenceException
	{
		List<List<Long>> unionees = new ArrayList<List<Long>>();
		List<Long> qr;
		int s = union_node.children.size();
		for(int i = 0;i < s;i++)
		{
			qr = eval_count(union_node.children.get(i));
			if(qr.size() == 0)
				continue;
			unionees.add(qr);
		}
		
		//tree map preserves order//
		TreeMap<Long,Long> tmap = new TreeMap<Long,Long>();
		s = unionees.size();
		for(int i = 0; i < s;i++)
		{
			qr = unionees.get(i);
			int ss = qr.size();
			for(int ii = 0; ii < ss;ii++)
			{
				long id = qr.get(ii);
				tmap.put(id,id);
			}
		}
		 return new ArrayList<Long>(tmap.values());	
	}
	
	private QueryResult do_iter(QueryNode iter_node) throws PersistenceException
	{
		//want to take offset and pagesize into account//
		String return_type 		= (String)iter_node.attributes.get(Query.ATT_RETURN_TYPE);
		String index_name  		= (String)iter_node.attributes.get(Query.ATT_INDEX_NAME);
		int page_size      		= Query.ALL_RESULTS;  //(Integer)iter_node.attributes.get(Query.ATT_PAGE_SIZE);;
		int offset		   		= 0;//(Integer)iter_node.attributes.get(Query.ATT_OFFSET);
		int iter_op	   	   		= (Integer)iter_node.attributes.get(Query.ATT_ITER_OP);
		BDBPrimaryIndex p_idx 	= _env.getPrimaryIndex(return_type);
			
		if(index_name.equals(Query.PRIMARY_IDX))
			return do_primary_index_iter(p_idx,iter_node,iter_op,offset,page_size);
		else
			return do_secondary_index_iter(p_idx,index_name,iter_node,iter_op,offset,page_size);

	}
	
	private QueryResult do_primary_index_iter(BDBPrimaryIndex pidx,QueryNode iter_node,int iter_op,int offset,int page_size) throws PersistenceException
	{
		IndexIterator iter;
		
		if((iter_op & Query.SET_ITER_TYPE) == Query.SET_ITER_TYPE)
			throw new PersistenceException("SET QUERY OPS ARE INVALID ON PRIMIARY INDEXES. ONLY PREDICATE AND BETWEEN OPS ARE SUPPORTED.");
		
		if((iter_op & Query.PREDICATE_ITER_TYPE) == Query.PREDICATE_ITER_TYPE)
		{
			DatabaseEntry param = new DatabaseEntry();
			Object user_param = _query_params[(Integer)iter_node.attributes.get(Query.ATT_PREDICATE_ITER_USER_PARAM)];
			
			if(user_param == Query.VAL_MIN)
				LongBinding.longToEntry(Long.MIN_VALUE, param);
			else if(user_param == Query.VAL_MAX)
				LongBinding.longToEntry(Long.MAX_VALUE, param);
			else if(user_param == Query.VAL_GLOB)
			{
				LongBinding.longToEntry(Long.MIN_VALUE, param);
				iter_op = Query.GTE;
			}
			else
			{
				long id_param;
				if(user_param.getClass() == Integer.class)
					id_param = (long)(Integer)user_param;
				else if(user_param.getClass() == Long.class)
					id_param = (Long)user_param;
				else
					throw new PersistenceException("PARAM FOR PRIMARY INDEX QUERY MUST BE A LONG.");
				
				LongBinding.longToEntry(id_param, param);
			}
			
			switch(iter_op)
			{
			case Query.EQ:		
				iter 	= new EQIndexIterator();
				break;
			case Query.GT:
				iter 	= new GTIndexIterator();
				break;			
			case Query.GTE:
				iter 	= new GTEIndexIterator();
				break;			
			case Query.LT:
				iter 	= new LTIndexIterator();
				break;			
			case Query.LTE:
				iter 	= new LTIndexIterator();
				break;	
			case Query.STARTSWITH:
				throw new PersistenceException("STARTSWITH IS INVALID PREDICATE ON PRIMARY INDEX");
			default:
				throw new PersistenceException("UNKNOWN PREDICATE ITER TYPE");
			}
		
			//open the iterator and return it//
			try{
				iter.open(pidx, param);
			}catch(DatabaseException dbe)
			{
				try{
					iter.close();
				}catch(DatabaseException dbe2)
				{
					dbe2.printStackTrace();
				}
				dbe.printStackTrace();
				throw new PersistenceException("FAILED TO OPEN ITERATOR!!!");

			}
		}
		else if((iter_op & Query.BETWEEN_ITER_TYPE) == Query.BETWEEN_ITER_TYPE)
		{
			DatabaseEntry param 	  = new DatabaseEntry();
			DatabaseEntry range_param = new DatabaseEntry();
			Object user_param  			= _query_params[(Integer)iter_node.attributes.get(Query.ATT_RANGE_ITER_USER_BOTTOM_PARAM)];
			Object user_range_param		= _query_params[(Integer)iter_node.attributes.get(Query.ATT_RANGE_ITER_USER_TOP_PARAM)];

			if(user_param == Query.VAL_GLOB || user_range_param == Query.VAL_GLOB)
				throw new PersistenceException("GLOB PARAMS ARE UNSUPPORTED FOR RANGE QUERIES. USE VAL_MIN AND/OR VAL_MAX.");
			
			if(user_param == Query.VAL_MIN)
				LongBinding.longToEntry(Long.MIN_VALUE, param);
			else if(user_param == Query.VAL_MAX)
				LongBinding.longToEntry(Long.MAX_VALUE, param);
			else
			{
				long id_param;
				if(user_param.getClass() == Integer.class)
					id_param = (long)(Integer)user_param;
				else if(user_param.getClass() == Long.class)
					id_param = (Long)user_param;
				else
					throw new PersistenceException("PARAM FOR PRIMARY INDEX QUERY MUST BE A LONG.");

				LongBinding.longToEntry(id_param, param);
			}
			
			if(user_range_param == Query.VAL_MIN)
				LongBinding.longToEntry(Long.MIN_VALUE, range_param);
			else if(user_range_param == Query.VAL_MAX)
				LongBinding.longToEntry(Long.MAX_VALUE, range_param);
			else
			{
				long id_param;
				if(user_range_param.getClass() == Integer.class)
					id_param = (long)(Integer)user_range_param;
				else if(user_param.getClass() == Long.class)
					id_param = (Long)user_range_param;
				else
					throw new PersistenceException("PARAM FOR PRIMARY INDEX QUERY MUST BE A LONG.");
				LongBinding.longToEntry(id_param, range_param);
			}

			switch(iter_op)
			{
				case Query.BETWEEN_INCLUSIVE_ASC:		
					iter 	= new BETWEEN_ASC_INCLUSIVEIndexIterator();
					break;
				case Query.BETWEEN_INCLUSIVE_DESC:
					iter 	= new BETWEEN_DESC_INCLUSIVEIndexIterator();
					break;			
				case Query.BETWEEN_EXCLUSIVE_ASC:
					iter 	= new BETWEEN_ASC_EXCLUSIVEIndexIterator();
					break;			
				case Query.BETWEEN_EXCLUSIVE_DESC:
					iter 	= new BETWEEN_DESC_EXCLUSIVEIndexIterator();
					break;			
				case Query.BETWEEN_START_INCLUSIVE_ASC:
					iter 	= new BETWEEN_ASC_START_INCLUSIVEIndexIterator();
					break;	
				case Query.BETWEEN_START_INCLUSIVE_DESC:
					iter 	= new BETWEEN_DESC_START_INCLUSIVEIndexIterator();
					break;
				case Query.BETWEEN_END_INCLUSIVE_ASC:
					iter 	= new BETWEEN_ASC_END_INCLUSIVEIndexIterator();
					break;	
				case Query.BETWEEN_END_INCLUSIVE_DESC:
					iter 	= new BETWEEN_DESC_END_INCLUSIVEIndexIterator();
					break;	
				default:
					throw new PersistenceException("UNKNOWN RANGE ITER TYPE");
			}
			//open the iterator and return it//
			try{
				iter.open(pidx, param,range_param);
			}catch(DatabaseException dbe)
			{
				try{
					iter.close();
				}catch(DatabaseException dbe2)
				{
					dbe2.printStackTrace();
				}
				dbe.printStackTrace();
				throw new PersistenceException("FAILED TO OPEN ITERATOR!!!");
			}
		}
		else
		{
			throw new PersistenceException("DEFINTELY SHOULD NOT BE HERE. NOT A PREDICATE OR BETWEEN OR SET ITER TYPE.");
		}
		//take offset and pagesize into account here
		BDBQueryResult results = new BDBQueryResult();
		int added = 0;
		try{
			while(iter.isValid())
			{
				Entity e = pidx.getByRow(iter.currentKey(),iter.currentData());
				results.add(e);
				if(++added == page_size)
				{
					break;
				}
				iter.next();
			}
			iter.close();
			return results;
		}catch(DatabaseException dbe)
		{
			dbe.printStackTrace();
			throw new PersistenceException("DATABASE EXCEPTION OCCURRED WHEN EXECUTING ITERATOR. SEE LOGS");

		}finally
		{
			try{
				iter.close();
			}catch(DatabaseException de)
			{
				de.printStackTrace();
				throw new PersistenceException("UNABLE TO CLOSE ITERATOR!!!");
			}
		}
	}
	
	private QueryResult do_secondary_index_iter(BDBPrimaryIndex p_idx,String index_name,QueryNode iter_node,int iter_op,int offset,int page_size) throws PersistenceException
	{
		
		IterableIndex idx;
		IndexIterator iter;
		idx = _env.getSecondaryIndex(p_idx.getName(), index_name);
		
		boolean ismulti = idx.isMultiFieldIndex();
		if((iter_op & Query.PREDICATE_ITER_TYPE) == Query.PREDICATE_ITER_TYPE)
		{
			iter = setup_predicate_iterator(idx,ismulti,iter_op,iter_node);
		}
		else if((iter_op & Query.BETWEEN_ITER_TYPE) == Query.BETWEEN_ITER_TYPE)
		{
			iter = setup_range_iterator(idx,ismulti,iter_op,iter_node);
		}
		else if((iter_op & Query.SET_ITER_TYPE) == Query.SET_ITER_TYPE)
		{
			iter = setup_set_iterator(idx,ismulti,iter_op,iter_node);
		}
		else if((iter_op & Query.FREETEXT_ITER_TYPE) == Query.FREETEXT_ITER_TYPE)
		{
			iter = setup_freetext_iterator(idx,ismulti,iter_op,iter_node);
		}
		else
		{
			throw new PersistenceException("UNKNOWN ITERATOR TYPE 0x"+Integer.toHexString(iter_op));
		}
		
		//take offset and pagesize into account here
		BDBQueryResult results = new BDBQueryResult();
		int added = 0;
		try{
			while(iter.isValid())
			{
				//System.out.println("ABOUT TO LOOKUP...CURRENT DATA IS "+new String(iter.currentData().getData()));
				Entity e = p_idx.getByPrimaryKey(iter.currentData());
				
		//		if(e == null)
		//		{
		//			System.out.println("E WAS NULL FOR PKEY "+new String(iter.currentData().getData()));
		//			byte[] tmp = new byte[8];
		//			System.arraycopy(iter.currentData().getData(), 0, tmp, 0, 8);
		//			System.out.println("ID IS "+LongBinding.entryToLong(new DatabaseEntry(tmp)));
		//		}
				results.add(e);
				if(++added == page_size)
				{
					break;
				}
				iter.next();
			}

			iter.close();
			return results;
		}catch(DatabaseException dbe)
		{
			dbe.printStackTrace();
			throw new PersistenceException("DATABASE EXCEPTION OCCURRED WHEN EXECUTING ITERATOR. SEE LOGS");

		}finally
		{
			try{
				iter.close();
			}catch(DatabaseException de)
			{
				de.printStackTrace();
				throw new PersistenceException("UNABLE TO CLOSE ITERATOR!!!");
			}
		}		
	}
	
	private List<Long> count_iter(QueryNode iter_node) throws PersistenceException
	{
		//want to take offset and pagesize into account//
		String return_type 		= (String)iter_node.attributes.get(Query.ATT_RETURN_TYPE);
		String index_name  		= (String)iter_node.attributes.get(Query.ATT_INDEX_NAME);
		int page_size      		= Query.ALL_RESULTS;  //(Integer)iter_node.attributes.get(Query.ATT_PAGE_SIZE);;
		int offset		   		= 0;//(Integer)iter_node.attributes.get(Query.ATT_OFFSET);
		int iter_op	   	   		= (Integer)iter_node.attributes.get(Query.ATT_ITER_OP);
		BDBPrimaryIndex p_idx 	= _env.getPrimaryIndex(return_type);
		if(index_name.equals(Query.PRIMARY_IDX))
			return do_primary_index_count(p_idx,iter_node,iter_op,offset,page_size);
		else
			return do_secondary_index_count(p_idx,index_name,iter_node,iter_op,offset,page_size);

	}
	
	private List<Long> do_primary_index_count(BDBPrimaryIndex pidx,QueryNode iter_node,int iter_op,int offset,int page_size) throws PersistenceException
	{
		IndexIterator iter;
		
		if((iter_op & Query.SET_ITER_TYPE) == Query.SET_ITER_TYPE)
			throw new PersistenceException("SET QUERY OPS ARE INVALID ON PRIMIARY INDEXES. ONLY PREDICATE AND BETWEEN OPS ARE SUPPORTED.");
		
		if((iter_op & Query.PREDICATE_ITER_TYPE) == Query.PREDICATE_ITER_TYPE)
		{
			DatabaseEntry param = new DatabaseEntry();
			Object user_param = _query_params[(Integer)iter_node.attributes.get(Query.ATT_PREDICATE_ITER_USER_PARAM)];
			
			if(user_param == Query.VAL_MIN)
				LongBinding.longToEntry(Long.MIN_VALUE, param);
			else if(user_param == Query.VAL_MAX)
				LongBinding.longToEntry(Long.MAX_VALUE, param);
			else if(user_param == Query.VAL_GLOB)
			{
				LongBinding.longToEntry(Long.MIN_VALUE, param);
				iter_op = Query.GTE;
			}
			else
			{
				long id_param;
				if(user_param.getClass() == Integer.class)
					id_param = (long)(Integer)user_param;
				else if(user_param.getClass() == Long.class)
					id_param = (Long)user_param;
				else
					throw new PersistenceException("PARAM FOR PRIMARY INDEX QUERY MUST BE A LONG.");
				
				LongBinding.longToEntry(id_param, param);
			}
			
			switch(iter_op)
			{
			case Query.EQ:		
				iter 	= new EQIndexIterator();
				break;
			case Query.GT:
				iter 	= new GTIndexIterator();
				break;			
			case Query.GTE:
				iter 	= new GTEIndexIterator();
				break;			
			case Query.LT:
				iter 	= new LTIndexIterator();
				break;			
			case Query.LTE:
				iter 	= new LTIndexIterator();
				break;	
			case Query.STARTSWITH:
				throw new PersistenceException("STARTSWITH IS INVALID PREDICATE ON PRIMARY INDEX");
			default:
				throw new PersistenceException("UNKNOWN PREDICATE ITER TYPE");
			}
		
			//open the iterator and return it//
			try{
				iter.open(pidx, param);
			}catch(DatabaseException dbe)
			{
				try{
					iter.close();
				}catch(DatabaseException dbe2)
				{
					dbe2.printStackTrace();
				}
				dbe.printStackTrace();
				throw new PersistenceException("FAILED TO OPEN ITERATOR!!!");

			}
		}
		else if((iter_op & Query.BETWEEN_ITER_TYPE) == Query.BETWEEN_ITER_TYPE)
		{
			DatabaseEntry param 	  = new DatabaseEntry();
			DatabaseEntry range_param = new DatabaseEntry();
			Object user_param  			= _query_params[(Integer)iter_node.attributes.get(Query.ATT_RANGE_ITER_USER_BOTTOM_PARAM)];
			Object user_range_param		= _query_params[(Integer)iter_node.attributes.get(Query.ATT_RANGE_ITER_USER_TOP_PARAM)];

			if(user_param == Query.VAL_GLOB || user_range_param == Query.VAL_GLOB)
				throw new PersistenceException("GLOB PARAMS ARE UNSUPPORTED FOR RANGE QUERIES. USE VAL_MIN AND/OR VAL_MAX.");
			
			if(user_param == Query.VAL_MIN)
				LongBinding.longToEntry(Long.MIN_VALUE, param);
			else if(user_param == Query.VAL_MAX)
				LongBinding.longToEntry(Long.MAX_VALUE, param);
			else
			{
				long id_param;
				if(user_param.getClass() == Integer.class)
					id_param = (long)(Integer)user_param;
				else if(user_param.getClass() == Long.class)
					id_param = (Long)user_param;
				else
					throw new PersistenceException("PARAM FOR PRIMARY INDEX QUERY MUST BE A LONG.");

				LongBinding.longToEntry(id_param, param);
			}
			
			if(user_range_param == Query.VAL_MIN)
				LongBinding.longToEntry(Long.MIN_VALUE, range_param);
			else if(user_range_param == Query.VAL_MAX)
				LongBinding.longToEntry(Long.MAX_VALUE, range_param);
			else
			{
				long id_param;
				if(user_param.getClass() == Integer.class)
					id_param = (long)(Integer)user_param;
				else if(user_param.getClass() == Long.class)
					id_param = (Long)user_param;
				else
					throw new PersistenceException("PARAM FOR PRIMARY INDEX QUERY MUST BE A LONG.");
				LongBinding.longToEntry(id_param, range_param);
			}

			switch(iter_op)
			{
				case Query.BETWEEN_INCLUSIVE_ASC:		
					iter 	= new BETWEEN_ASC_INCLUSIVEIndexIterator();
					break;
				case Query.BETWEEN_INCLUSIVE_DESC:
					iter 	= new BETWEEN_DESC_INCLUSIVEIndexIterator();
					break;			
				case Query.BETWEEN_EXCLUSIVE_ASC:
					iter 	= new BETWEEN_ASC_EXCLUSIVEIndexIterator();
					break;			
				case Query.BETWEEN_EXCLUSIVE_DESC:
					iter 	= new BETWEEN_DESC_EXCLUSIVEIndexIterator();
					break;			
				case Query.BETWEEN_START_INCLUSIVE_ASC:
					iter 	= new BETWEEN_ASC_START_INCLUSIVEIndexIterator();
					break;	
				case Query.BETWEEN_START_INCLUSIVE_DESC:
					iter 	= new BETWEEN_DESC_START_INCLUSIVEIndexIterator();
					break;
				case Query.BETWEEN_END_INCLUSIVE_ASC:
					iter 	= new BETWEEN_ASC_END_INCLUSIVEIndexIterator();
					break;	
				case Query.BETWEEN_END_INCLUSIVE_DESC:
					iter 	= new BETWEEN_DESC_END_INCLUSIVEIndexIterator();
					break;	
				default:
					throw new PersistenceException("UNKNOWN RANGE ITER TYPE");
			}
			//open the iterator and return it//
			try{
				iter.open(pidx, param,range_param);
			}catch(DatabaseException dbe)
			{
				try{
					iter.close();
				}catch(DatabaseException dbe2)
				{
					dbe2.printStackTrace();
				}
				dbe.printStackTrace();
				throw new PersistenceException("FAILED TO OPEN ITERATOR!!!");
			}
		}
		else
		{
			throw new PersistenceException("DEFINTELY SHOULD NOT BE HERE. NOT A PREDICATE OR BETWEEN OR SET ITER TYPE.");
		}
		//take offset and pagesize into account here
		//take offset and pagesize into account here
		List<Long> ids = new ArrayList<Long>(512);
		int added = 0;
		try{
			while(iter.isValid())
			{
				//Entity e = p_idx.getByPrimaryKey(data);
				ids.add(LongBinding.entryToLong(iter.currentKey()));
				if(++added == page_size)
					break;
				iter.next();
			}
			iter.close();//i dont think we need this because of the finally//
			return ids;
		}catch(DatabaseException dbe)
		{
			dbe.printStackTrace();
			throw new PersistenceException("DATABASE EXCEPTION OCCURRED WHEN EXECUTING ITERATOR. SEE LOGS");

		}finally
		{
			try{
				iter.close();
			}catch(DatabaseException de)
			{
				de.printStackTrace();
				throw new PersistenceException("UNABLE TO CLOSE ITERATOR!!!");
			}
		}	
	}
	
	private List<Long> do_secondary_index_count(BDBPrimaryIndex p_idx,String index_name,QueryNode iter_node,int iter_op,int offset,int page_size) throws PersistenceException
	{
		
		IterableIndex idx;
		IndexIterator iter;
		idx = _env.getSecondaryIndex(p_idx.getName(), index_name);
		
		boolean ismulti = idx.isMultiFieldIndex();
		if((iter_op & Query.PREDICATE_ITER_TYPE) == Query.PREDICATE_ITER_TYPE)
		{
			iter = setup_predicate_iterator(idx,ismulti,iter_op,iter_node);
		}
		else if((iter_op & Query.BETWEEN_ITER_TYPE) == Query.BETWEEN_ITER_TYPE)
		{
			iter = setup_range_iterator(idx,ismulti,iter_op,iter_node);
		}
		else if((iter_op & Query.SET_ITER_TYPE) == Query.SET_ITER_TYPE)
		{
			iter = setup_set_iterator(idx,ismulti,iter_op,iter_node);
		}
		else if((iter_op & Query.FREETEXT_ITER_TYPE) == Query.FREETEXT_ITER_TYPE)
		{
			iter = setup_freetext_iterator(idx,ismulti,iter_op,iter_node);
		}
		else
		{
			throw new PersistenceException("UNKNOWN ITERATOR TYPE 0x"+Integer.toHexString(iter_op));
		}
		
		//take offset and pagesize into account here
		List<Long> ids = new ArrayList<Long>(512);
		int added = 0;
		try{
			while(iter.isValid())
			{
				//Entity e = p_idx.getByPrimaryKey(data);
				ids.add(LongBinding.entryToLong(iter.currentData()));
				if(++added == page_size)
					break;
				iter.next();
			}
			iter.close();//i dont think we need this because of the finally//
			return ids;
		}catch(DatabaseException dbe)
		{
			dbe.printStackTrace();
			throw new PersistenceException("DATABASE EXCEPTION OCCURRED WHEN EXECUTING ITERATOR. SEE LOGS");

		}finally
		{
			try{
				iter.close();
			}catch(DatabaseException de)
			{
				de.printStackTrace();
				throw new PersistenceException("UNABLE TO CLOSE ITERATOR!!!");
			}
		}	
	}
	
	private IndexIterator setup_predicate_iterator(IterableIndex idx,boolean is_multi,int iter_type,QueryNode iter_node) throws PersistenceException
	{		
		if(idx.isSetIndex())
			throw new PersistenceException("UNSUPPORTED OPERATION FOR INDEX."+idx.getName()+"IS NOT A PREDICATE INDEX");
		
		DatabaseEntry param			= null;
		Object user_param = _query_params[(Integer)iter_node.attributes.get(Query.ATT_PREDICATE_ITER_USER_PARAM)];
		
		if(is_multi)
		{
			List<Object> l_user_param;
			try{
				l_user_param = (List<Object>)user_param;
			}catch (ClassCastException e)
			{
				throw new PersistenceException("QUERY ARGUMENT MUST BE A LIST: "+user_param);
			}
			int last 	 = l_user_param.size()-1;
			
			if (l_user_param.get(last) == Query.VAL_GLOB)
			  return get_globbed_multi_index_predicate_iterator(idx,l_user_param,iter_type,last,iter_node);
			
			if(last != idx.getNumIndexedFields()-1 && iter_type != Query.STARTSWITH)
				throw new PersistenceException("WRONG NUMBER OF ARGUMENTS FOR INDEX. INDEX HAS "+idx.getNumIndexedFields()+" AND "+l_user_param.size()+" WERE PROVIDED");
 
			try{
				param = ((SimpleMultiFieldIndex)idx).getQueryKey((List<Object>)user_param);			
			}catch(DatabaseException dbe)
			{
				throw new PersistenceException("UNABLE TO GENERATE QUERY KEY FOR QUERY VAL");
			}
		}
		else
		{
			if (user_param == Query.VAL_GLOB)
				  return get_globbed_single_index_predicate_iterator(idx,iter_type,iter_node);
			try{
				param = ((SimpleSingleFieldIndex)idx).getQueryKey(user_param);
			}catch(DatabaseException dbe)
			{
				throw new PersistenceException("UNABLE TO GENERATE QUERY KEY FOR QUERY VAL");
			}
		}
		
		PredicateIndexIterator iter = null;

		switch(iter_type)
		{
			case Query.EQ:		
				iter 	= new EQIndexIterator();
				break;
			case Query.GT:
				iter 	= new GTIndexIterator();
				break;			
			case Query.GTE:
				iter 	= new GTEIndexIterator();
				break;			
			case Query.LT:
				iter 	= new LTIndexIterator();
				break;			
			case Query.LTE:
				iter 	= new LTIndexIterator();
				break;	
			case Query.STARTSWITH:
				iter 	= new STARTSWITHSTRINGIndexIterator();
				break;
			default:
				throw new PersistenceException("UNKNOWN PREDICATE ITER TYPE");
		}
		//open the iterator and return it//
		try{
			iter.open(idx, param);
		}catch(DatabaseException dbe)
		{
			try{
				iter.close();
			}catch(DatabaseException dbe2)
			{
				dbe2.printStackTrace();
			}
			dbe.printStackTrace();
			throw new PersistenceException("FAILED TO OPEN ITERATOR!!!");

		}
		return iter;
	}	
	
	private IndexIterator get_globbed_multi_index_predicate_iterator(IterableIndex idx,List<Object> params,int iter_type,int glob_idx,QueryNode iter_node) throws PersistenceException
	{
		int i = glob_idx;
		switch(iter_type)
		{
			case Query.EQ:/* a globbed multi field equals is actually a between */
					List<Object> top_params = new ArrayList<Object>(params.size());
					top_params.addAll(params);
					do	
					{
						params.set(i,Query.VAL_MIN);
						top_params.set(i,Query.VAL_MAX);
					}while(--i >= 0 && params.get(i) == Query.VAL_GLOB);
					
					iter_type = Query.BETWEEN_INCLUSIVE_ASC; 
					iter_node.attributes.put(Query.ATT_ITER_OP,iter_type);
					//stuff them at the end of paramter space. not the best idea but easy to solve perhaps 
					//params is always 4 longer than max params or something
					int new_idx = _query_params.length-1;
					_query_params[new_idx] = params;					
					iter_node.attributes.put(Query.ATT_RANGE_ITER_USER_BOTTOM_PARAM, new_idx);
					_query_params[new_idx-1] = top_params;
					iter_node.attributes.put(Query.ATT_RANGE_ITER_USER_TOP_PARAM, new_idx-1);
					return setup_range_iterator(idx,true,iter_type,iter_node);
			case Query.GT:
					do	
					{
						params.set(i,Query.VAL_MAX);
					}while(i >= 0 && params.get(--i) == Query.VAL_GLOB);
					//iter_node.attributes.put(Query.ATT_PREDICATE_ITER_USER_PARAM, params);
					return setup_predicate_iterator(idx,true,iter_type,iter_node);
			case Query.GTE:
					do	
					{
						params.set(i,Query.VAL_MIN);
					}while(i >= 0 && params.get(--i) == Query.VAL_GLOB);
					return setup_predicate_iterator(idx,true,iter_type,iter_node);
			case Query.LT:
				do	
				{
					params.set(i,Query.VAL_MIN);
				}while(i >= 0 && params.get(--i) == Query.VAL_GLOB);
				return setup_predicate_iterator(idx,true,iter_type,iter_node);
			case Query.LTE:
				do	
				{
					params.set(i,Query.VAL_MAX);
				}while(i >= 0 && params.get(--i) == Query.VAL_GLOB);
				return setup_predicate_iterator(idx,true,iter_type,iter_node);
			case Query.STARTSWITH:
				do	
				{
					params.remove(i);
				}while(i >= 0 && params.get(--i) == Query.VAL_GLOB);
				return setup_predicate_iterator(idx,true,iter_type,iter_node);
			default:
					throw new PersistenceException("UNKNOWN ITER TYPE IN SET UP GLOBBED PREDICATE MULTI ITER");
		}
	}
	
	private IndexIterator get_globbed_single_index_predicate_iterator(IterableIndex idx,int iter_type,QueryNode iter_node) throws PersistenceException
	{
		switch(iter_type)
		{
			case Query.EQ:			
			case Query.GT:	
			case Query.GTE:
			case Query.STARTSWITH:
				iter_type = Query.GTE;
				iter_node.attributes.put(Query.ATT_ITER_OP,iter_type);
				int user_param_idx = (Integer)iter_node.attributes.get(Query.ATT_PREDICATE_ITER_USER_PARAM);
				_query_params[user_param_idx] = Query.VAL_MIN;
				iter_node.attributes.put(Query.ATT_PREDICATE_ITER_USER_PARAM, user_param_idx);
				return setup_predicate_iterator(idx,false,iter_type,iter_node);
			case Query.LT:
			case Query.LTE:
				user_param_idx = (Integer)iter_node.attributes.get(Query.ATT_PREDICATE_ITER_USER_PARAM);
				_query_params[user_param_idx] = Query.VAL_MAX;
				iter_node.attributes.put(Query.ATT_PREDICATE_ITER_USER_PARAM, user_param_idx);
				return setup_predicate_iterator(idx,false,iter_type,iter_node);
			default:
					throw new PersistenceException("UNKNOWN ITER TYPE IN SET UP GLOBBED PREDICATE SINGLE ITER");
		}
	}
	
	@SuppressWarnings("unchecked")
	private IndexIterator setup_range_iterator(IterableIndex idx,boolean is_multi,int iter_type,QueryNode iter_node) throws PersistenceException
	{
		if(idx.isSetIndex())
			throw new PersistenceException("UNSUPPORTED OPERATION FOR INDEX."+idx.getName()+"IS NOT A SET INDEX");

		Object user_param  			= _query_params[(Integer)iter_node.attributes.get(Query.ATT_RANGE_ITER_USER_BOTTOM_PARAM)];
		Object user_range_param		= _query_params[(Integer)iter_node.attributes.get(Query.ATT_RANGE_ITER_USER_TOP_PARAM)];
		DatabaseEntry param = null;
		DatabaseEntry range_param = null;
		
		if(is_multi)
		{	
			List<Object> l_user_param  = (List<Object>)user_param;
		//	System.out.println("BOTTOM PARAM "+user_param+" "+Query.VAL_MIN);
		//	System.out.println("TOP PARAM "+user_range_param+" "+Query.VAL_MAX);
			List<Object> l_range_param = (List<Object>)user_range_param;
			if(l_user_param.contains(Query.VAL_GLOB) || l_range_param.contains(Query.VAL_GLOB))
				throw new PersistenceException("GLOB PARAMETERS ARE INVALID FOR RANGE QUERIES.Query.VAL_MIN and Query.VAL_MAX ARE VALID BUT NOT Query.VAL_GLOB");
			
			try{
				param = ((SimpleMultiFieldIndex)idx).getQueryKey(l_user_param);
				range_param = ((SimpleMultiFieldIndex)idx).getQueryKey((List<Object>)user_range_param);
			}catch(DatabaseException dbe)
			{
				throw new PersistenceException("UNABLE TO GENERATE QUERY KEY FOR QUERY VAL");
			}
		}
		else
		{
			if(user_param == Query.VAL_GLOB || range_param == Query.VAL_GLOB)
				throw new PersistenceException("GLOB PARAMETERS ARE INVALID FOR RANGE QUERIES.Query.VAL_MIN and Query.VAL_MAX ARE VALID BUT NOT Query.VAL_GLOB");
			try{
				param 		= ((SimpleSingleFieldIndex)idx).getQueryKey(user_param);
				range_param = ((SimpleSingleFieldIndex)idx).getQueryKey(user_range_param);
			}catch(DatabaseException dbe)
			{
				throw new PersistenceException("UNABLE TO GENERATE QUERY KEY FOR QUERY VAL");
			}
		}
		
		RangeIndexIterator iter = null;

		switch(iter_type)
		{
			case Query.BETWEEN_INCLUSIVE_ASC:		
				iter 	= new BETWEEN_ASC_INCLUSIVEIndexIterator();
				break;
			case Query.BETWEEN_INCLUSIVE_DESC:
				iter 	= new BETWEEN_DESC_INCLUSIVEIndexIterator();
				break;			
			case Query.BETWEEN_EXCLUSIVE_ASC:
				iter 	= new BETWEEN_ASC_EXCLUSIVEIndexIterator();
				break;			
			case Query.BETWEEN_EXCLUSIVE_DESC:
				iter 	= new BETWEEN_DESC_EXCLUSIVEIndexIterator();
				break;			
			case Query.BETWEEN_START_INCLUSIVE_ASC:
				iter 	= new BETWEEN_ASC_START_INCLUSIVEIndexIterator();
				break;	
			case Query.BETWEEN_START_INCLUSIVE_DESC:
				iter 	= new BETWEEN_DESC_START_INCLUSIVEIndexIterator();
				break;
			case Query.BETWEEN_END_INCLUSIVE_ASC:
				iter 	= new BETWEEN_ASC_END_INCLUSIVEIndexIterator();
				break;	
			case Query.BETWEEN_END_INCLUSIVE_DESC:
				iter 	= new BETWEEN_DESC_END_INCLUSIVEIndexIterator();
				break;	
			default:
				throw new PersistenceException("UNKNOWN RANGE ITER TYPE");
		}
		//open the iterator and return it//
		try{
			iter.open(idx, param,range_param);
		}catch(DatabaseException dbe)
		{
			try{
				iter.close();
			}catch(DatabaseException dbe2)
			{
				dbe2.printStackTrace();
			}
			dbe.printStackTrace();
			throw new PersistenceException("FAILED TO OPEN ITERATOR!!!");

		}
		return iter;
	}
	
	private IndexIterator setup_set_iterator(IterableIndex idx,boolean is_multi,int iter_type,QueryNode iter_node) throws PersistenceException
	{
		if(idx.isNormalIndex())
			throw new PersistenceException("UNSUPPORTED OPERATION FOR INDEX."+idx.getName()+"IS A SET INDEX AND ONLY SUPPORTS SET QUERY OPERATIONS");
		List<Object> user_list_param = (List<Object>)_query_params[(Integer)iter_node.attributes.get(Query.ATT_SET_ITER_USER_PARAM)];
		//right here is where we would catch classcastexception if we wanted to accept single values to set ops
		List<DatabaseEntry> list_param  = null;
		boolean globbing = false;
		if(is_multi)
		{
			try{
				int last = user_list_param.size() - 1;
				if(user_list_param.get(last) == Query.VAL_GLOB)
				{
					//clone
					ArrayList<Object> copy = (ArrayList<Object>)((ArrayList<Object>)user_list_param).clone();
					
					do
					{
						copy.remove(last--);						
					}
					while(last >= 0 && copy.get(last) == Query.VAL_GLOB);
				
					globbing = true;
					
					if(last == 0 && copy.get(0) == Query.VAL_GLOB )
					{
						throw new PersistenceException("UNSUPPORTED SET QUERY TYPE. THERE IS NO ALL GLOBBING QUERY FOR SET QUERY RIGHT NOW.");
						//copy.add(null);
					}
					list_param = ((MultiFieldArrayMembershipIndex)idx).getQueryKeys((List<Object>)copy);
					//System.out.println("LIST PARAM IS "+list_param);
				}
				else
				{
					list_param = ((MultiFieldArrayMembershipIndex)idx).getQueryKeys((List<Object>)user_list_param);
				}
			}catch(DatabaseException dbe)
			{
				throw new PersistenceException("UNABLE TO GENERATE QUERY KEY FOR QUERY VAL");
			}
		}
		else
		{
			try{
				list_param = ((ArrayMembershipIndex)idx).getQueryKeys((List<Object>)user_list_param);
			}catch(DatabaseException dbe)
			{
				throw new PersistenceException("UNABLE TO GENERATE QUERY KEY FOR QUERY VAL");
			}
		}
		SetIndexIterator iter = null;
		switch(iter_type)
		{
			case Query.SET_CONTAINS_ANY:
				iter 	= new SETCONTAINSANYIndexIterator();
				break;			
			case Query.SET_CONTAINS_ALL:		
				iter 	= new SETCONTAINSALLIndexIterator();
				break;
			default:
				throw new PersistenceException("UNKNOWN SET ITER TYPE");
		}
		//open the iterator and return it//
		try{
			iter.open(idx,globbing,list_param);
		}catch(DatabaseException dbe)
		{
			try{
				iter.close();
			}catch(DatabaseException dbe2)
			{
				dbe2.printStackTrace();
			}
			dbe.printStackTrace();
			throw new PersistenceException("FAILED TO OPEN ITERATOR!!!");

		}
		return iter;
	}
	
	private IndexIterator setup_freetext_iterator(IterableIndex idx,boolean is_multi,int iter_type,QueryNode iter_node) throws PersistenceException
	{
		if(!idx.isFreeTextIndex())
			throw new PersistenceException("UNSUPPORTED OPERATION FOR INDEX."+idx.getName()+"IS A FREETEXT INDEX AND ONLY SUPPORTS FREETEXT OPERATIONS.");
		List<Object> user_list_param = (List<Object>)_query_params[(Integer)iter_node.attributes.get(Query.ATT_SET_ITER_USER_PARAM)];
		//right here is where we would catch classcastexception if we wanted to accept single values to set ops
		
		boolean globbing = false;
		if(is_multi)
		{
			List<List<DatabaseEntry>> multi_list_param = null;
			try{
				/* this is a sort of special case because querying the free text
				 * index is more syntactically complex. so MultiFieldFreeTextIndex
				 * has its own iterator that it uses FREETEXTMULTIWRAPPER. It makes
				 * a key for each field for each term in the search string
				 * textContainsPhrase(q.list(q.list("title","summary"),q.list("huckleberry","finn"),PUBLISHED));
				 * --GET ALL RECORDS WHERE TITLE OR SUMMARY CONTAINS THE PHRASE HUCKLEBERRY FINN
				 */
				int last = user_list_param.size() - 1;
				if(user_list_param.get(last) == Query.VAL_GLOB)
				{
					//clone
					ArrayList<Object> copy = (ArrayList<Object>)((ArrayList<Object>)user_list_param).clone();					
					do
					{
						copy.remove(last--);						
					}
					while(last >= 2 && copy.get(last) == Query.VAL_GLOB);
					globbing = true;
					multi_list_param = ((MultiFieldFreeTextIndex)idx).getQueryKeys((List<Object>)copy);

				}
				else if(user_list_param.get(0) == Query.VAL_GLOB 
						&& user_list_param.get(1) == Query.VAL_GLOB)
				{
					//This is for the special case where you want to ignore the freetext
					//part of the index on a multi freetext index and just don an equality
					//query on the rest of the index
					//q.setContainsAny(Query.VAL_GLOB,Query.VAL_GLOB,PUBLISHED)
					globbing = true;
					multi_list_param = ((MultiFieldFreeTextIndex)idx).getQueryKeys((List<Object>)user_list_param);
				}
				else
				{
					multi_list_param = ((MultiFieldFreeTextIndex)idx).getQueryKeys((List<Object>)user_list_param);
				}
	
			}catch(DatabaseException dbe)
			{
				throw new PersistenceException("UNABLE TO GENERATE QUERY KEY FOR QUERY VAL");
			}
	
			return open_multi_field_freetext_iterator(iter_type, idx, globbing, multi_list_param);			
		}
		else
		{
			List<DatabaseEntry> list_param  = null;
			try{
				list_param = ((SingleFieldFreeTextIndex)idx).getQueryKeys((List<Object>)user_list_param);
			}catch(DatabaseException dbe)
			{
				throw new PersistenceException("UNABLE TO GENERATE QUERY KEY FOR QUERY VAL");
			}
			return open_single_field_freetext_iterator(iter_type,idx, globbing, list_param);
		}
	
	}


	private IndexIterator open_single_field_freetext_iterator(int iter_type,IterableIndex idx,boolean globbing,Object list_param) throws PersistenceException
	{
		SetIndexIterator iter = null;
		switch(iter_type)
		{
			case Query.FREETEXT_CONTAINS_ANY:
				iter 	= new FREETEXTCONTAINSANYIndexIterator();
				break;			
			case Query.FREETEXT_CONTAINS_ALL:		
				iter 	= new FREETEXTCONTAINSALLIndexIterator();
				break;
			case Query.FREETEXT_CONTAINS_PHRASE:		
				iter 	= new FREETEXTCONTAINSPHRASEIndexIterator();
				break;
			default:
				throw new PersistenceException("UNKNOWN FREETEXT ITER TYPE");
		}
		//open the iterator and return it//
		try{
			iter.open(idx,globbing,list_param);
		}catch(DatabaseException dbe)
		{
			try{
				iter.close();
			}catch(DatabaseException dbe2)
			{
				dbe2.printStackTrace();
			}
			dbe.printStackTrace();
			throw new PersistenceException("FAILED TO OPEN ITERATOR!!!");

		}
		return iter;
		
	}
	
	private IndexIterator open_multi_field_freetext_iterator(int iter_type,IterableIndex idx,boolean globbing,Object list_param) throws PersistenceException
	{
		IndexIterator iter;
		iter = new FREETEXTMULTIWRAPPERIterator();
		//open the iterator and return it//
		try{
			iter.open(idx,globbing,list_param,iter_type);
		}catch(DatabaseException dbe)
		{
			try{
				iter.close();
			}catch(DatabaseException dbe2)
			{
				dbe2.printStackTrace();
			}
			dbe.printStackTrace();
			throw new PersistenceException("FAILED TO OPEN ITERATOR!!!");

		}
		return iter;		
		
	}


}