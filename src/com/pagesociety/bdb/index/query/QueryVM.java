package com.pagesociety.bdb.index.query;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

import com.pagesociety.bdb.BDBPrimaryIndex;
import com.pagesociety.bdb.BDBQueryResult;
import com.pagesociety.bdb.index.ArrayMembershipIndex;
import com.pagesociety.bdb.index.MultiFieldArrayMembershipIndex;
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
import com.pagesociety.persistence.PersistenceException;
import com.pagesociety.persistence.Query;
import com.pagesociety.persistence.QueryResult;
import com.pagesociety.persistence.util.EntityComparitor;
import com.pagesociety.persistence.util.EntityIdComparator;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;


public class QueryVM
{
	private static final int MAX_STACK_SIZE = 256;
	private Object[] 		_stack;
	private int 			_current_stack_index;
	private int 			_program_counter;
	private List<Object> 	_program;
	private Object[] 	    _params;
	private String[] 	    _strings;
	private int[]			_pagesizes;
	private int[]			_offsets;
	private String			_return_type_register;
	private String			_index_name_register;
	private int				_offset_register;
	private int				_page_size_register;
	private Object			_general_purpose_register;
	
	private QueryExecutionEnvironment _env;
	
	public QueryVM(QueryExecutionEnvironment env)
	{
		_stack = new Object[MAX_STACK_SIZE];
		_env = env;
	}

	public QueryResult execute(QueryCompiler q) throws PersistenceException
	{
		long t1;
		long t2; 
		
		t1 = System.currentTimeMillis();
		_current_stack_index 		= 0;
		_program_counter 	 		= 0;
		
		_program 					= q.getProgram();
		_params  					= q.getParams();
		_strings 					= q.getStrings();
		_pagesizes 					= q.getPageSizes();
		_offsets 					= q.getOffsets();
		_return_type_register 		= null;
		_index_name_register 		= null;
		_offset_register 			= 0;
		_page_size_register 		= 0;
		_general_purpose_register 	= null;

		
		int opcode;
		while((opcode = (Integer)_program.get(_program_counter++)) != Q.RETURN)
		{
			//System.out.println("PC IS "+_program_counter);
			//System.out.println("OPCODE IS "+Integer.toHexString(opcode));
			switch(opcode)
			{
				case Q.PUSH_CONSTANT_INT:
					do_push_constant();
					break;
				case Q.LOAD_STRING_VAR:
					do_push_string_var();
					break;	
				case Q.LOAD_VAR:
					do_push_var();
					break;	
				case Q.LOAD_OFFSET_VAR:
					do_push_offset_var();
					break;						
				case Q.LOAD_PAGESIZE_VAR:
					do_push_pagesize_var();
					break;	
				case Q.DO_ITER:
					do_iter();		
					break;
				case Q.SET_RETURN_TYPE_REGISTER:
					_return_type_register = (String)do_pop();
					break;
				case Q.SET_INDEX_NAME_REGISTER:
					_index_name_register  = (String)do_pop();
					break;
				case Q.SET_OFFSET_REGISTER:
					_offset_register  = (Integer)do_pop();
					break;
				case Q.SET_PAGE_SIZE_REGISTER:
					//System.out.println("SET PAGE SIZE REGISTER");
					_page_size_register  = (Integer)do_pop();
					break;
				case Q.SET_GP_REGISTER:
					//System.out.println("SET GP REGISTER");
					_general_purpose_register  = do_pop();
					break;	
				case Q.LOAD_RETURN_TYPE_REGISTER:
					//System.out.println("SET RETURN TYPE REGISTER");
					do_push(_return_type_register);
					break;
				case Q.LOAD_INDEX_NAME_REGISTER:
					//System.out.println("LOAD INDEX NAME REGISTER");
					do_push(_index_name_register);
					break;
				case Q.LOAD_OFFSET_REGISTER:
					//System.out.println("LOAD OFFSET REGISTER");
					do_push(_offset_register);
					break;
				case Q.LOAD_PAGE_SIZE_REGISTER:
					//System.out.println("LOAD OFFSET REGISTER");
					do_push(_page_size_register);
					break;	
				case Q.LOAD_GP_REGISTER:
					//System.out.println("LOAD GP REGISTER");
					do_push(_general_purpose_register);
					break;
				case Q.PUSH_NULL:
					//System.out.println("PUSH NULL");
					do_push(null);
					break;	
				case Q.DO_INTERSECTION:
					do_intersection();
					break;
				case Q.DO_UNION:
					do_union();
					break;
				case Q.DO_ORDER_BY:
					do_order_by();
					break;
				default:
				{
					try{
						throw new Exception();
					}catch(Exception e)
					{
						e.printStackTrace();
					}
					System.err.println("SHOULD NOT BE HERE OPCODE WAS"+"0x"+Integer.toHexString(opcode));
				}
			}
			//System.out.println("STACK IS");
			//for(int i = 0;i < _current_stack_index;i++)
			//{
			//	System.out.println("\t"+i+"\t"+String.valueOf(_stack[i]));
			//}
			//System.out.println();
		}

		//System.out.println("HIT RETURN");
		t2 = System.currentTimeMillis();
		System.out.println("INTERNAL EXECUTE TOOK "+(t2-t1));	
		BDBQueryResult res = (BDBQueryResult)do_pop();
		System.out.println("QUERY OUTER PAGE SIZE IS "+q.getPageSize());
		System.out.println("PAGE SIZE REG IS "+_page_size_register);
		System.out.println("QUERY OUTER PAGE SIZE IS "+q.getOffset());
		System.out.println("OFFSET REG IS    "+_offset_register);
		return (QueryResult)res;
	}

	private void do_order_by()
	{
		String attribute = (String)do_pop();
		QueryResult qr   = (QueryResult)do_pop();
		EntityComparitor comp = new EntityComparitor(attribute);
		Collections.sort(qr.getEntities(),comp);
		do_push(qr);
	}

	private void do_intersection()
	{
		List<QueryResult> intersectees = new ArrayList<QueryResult>();
		QueryResult qr;
		while((qr = (QueryResult)do_pop()) != null)
		{
			if(qr.size() == 0)
			{
				do_push(BDBQueryResult.EMPTY_RESULT);
				return;
			}
			intersectees.add(qr);
		}
		
		ArrayList<Entity> merge = new ArrayList<Entity>(512);
		/* merge */
		int s = intersectees.size();
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
		System.out.println("do_fast_intersection(Query) -  FAST INTERSECT " + t2);
		do_push(inter);
	}
	
	private void do_union()
	{
		List<QueryResult> unionees = new ArrayList<QueryResult>();
		QueryResult qr;
		while((qr = (QueryResult)do_pop()) != null)
		{
			if(qr.size() == 0)
				continue;
			unionees.add(qr);
		}
		
		TreeMap<Long,Entity> tmap = new TreeMap<Long,Entity>();
		int ss = unionees.size();
		for(int i = 0; i < ss;i++)
		{
			qr = unionees.get(i);
			List<Entity> ee = qr.getEntities();
			int s = qr.size();
			for(int ii = 0; ii < s;ii++)
			{
				Entity e = ee.get(ii);
				tmap.put(e.getId(),e);
			}
		}
		 do_push(new BDBQueryResult(tmap.values()));	
	}
	
	private void do_push_constant()
	{
		Integer val = (Integer)_program.get(_program_counter++);
	//	System.out.println("PUSHING "+String.valueOf(val)+" ONTO STACK");
		_stack[_current_stack_index++] = val;
	}
	
	
	private void do_push_var()
	{
		Integer idx = (Integer)_program.get(_program_counter++);
	//	System.out.println("PUSHING "+String.valueOf(val)+" ONTO STACK");
		_stack[_current_stack_index++] = _params[idx];
	}
	
	private void do_push_string_var()
	{
		Integer idx = (Integer)_program.get(_program_counter++);
		//System.out.println("PUSHING "+String.valueOf(_strings[idx])+" "+idx+" ONTO STACK");
		_stack[_current_stack_index++] = _strings[idx];
	}
	
	private void do_push_offset_var()
	{
		Integer idx = (Integer)_program.get(_program_counter++);
		//System.out.println("PUSHING "+String.valueOf(_strings[idx])+" "+idx+" ONTO STACK");
		_stack[_current_stack_index++] = _offsets[idx];
	}
	
	private void do_push_pagesize_var()
	{
		Integer idx = (Integer)_program.get(_program_counter++);
		//System.out.println("PUSHING "+String.valueOf(_strings[idx])+" "+idx+" ONTO STACK");
		_stack[_current_stack_index++] = _pagesizes[idx];
	}
	
	private void do_push(Object val)
	{
		//System.out.println("PUSHING RESULT"+String.valueOf(result)+" ONTO STACK");
		_stack[_current_stack_index++] =  val;
	}
	
	private Object do_pop()
	{
		Object val =  _stack[--_current_stack_index];
		//System.out.println("POPPING "+String.valueOf(val)+" OFF OF STACK");
		return val;
	}
	
	private void do_iter() throws PersistenceException
	{
		String return_type 		= _return_type_register;
		String index_name  		= _index_name_register;
		int page_size      		= _page_size_register;
		int offset		   		= _offset_register;
		int iter_op	   	   		= (Integer)do_pop();//maybe this is a register at some point as well	
		BDBPrimaryIndex p_idx 	= _env.getPrimaryIndex(return_type);
		IterableIndex idx;
		IndexIterator iter;
		
		
		if(index_name.equals(return_type))
			idx = p_idx;
		else
			idx = _env.getSecondaryIndex(return_type, index_name);
		boolean ismulti = idx.isMultiFieldIndex();
		
		if((iter_op & Q.PREDICATE_ITER_TYPE) == Q.PREDICATE_ITER_TYPE)
		{
			iter = setup_predicate_iterator(idx,ismulti,iter_op);
		}
		else if((iter_op & Q.BETWEEN_ITER_TYPE) == Q.BETWEEN_ITER_TYPE)
		{
			iter = setup_range_iterator(idx,ismulti,iter_op);
		}
		else if((iter_op & Q.SET_ITER_TYPE) == Q.SET_ITER_TYPE)
		{
			iter = setup_set_iterator(idx,ismulti,iter_op);
		}
		else
		{
			throw new PersistenceException("UNKNOWN ITERATOR TYPE...BAD OPCODE "+iter_op);
		}
		
		DatabaseEntry data;
		BDBQueryResult results = new BDBQueryResult();
		int added = 0;
		try{
			while(iter.isValid())
			{
				data 	 = iter.currentData();
				Entity e = p_idx.getByPrimaryKey(data);
				results.add(e);
				if(++added == page_size)
				{
					//result.setNextResultsToken(iter.getNextResultsToken());	
					break;
				}
				iter.next();
			}
			iter.close();
			do_push(results);
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
		
	//	System.out.println("EXEC ITER OF TYPE: "+iter_type);
	//	System.out.println("\t RETURN TYPE: "+return_type);
	//	System.out.println("\t INDEX NAME: "+index_name);
	//	System.out.println("\t PAGE SIZE: "+page_size);

	}
	
	private IndexIterator setup_predicate_iterator(IterableIndex idx,boolean is_multi,int iter_type) throws PersistenceException
	{		
		if(idx.isSetIndex())
			throw new PersistenceException("UNSUPPORTED OPERATION FOR INDEX."+idx.getName()+"IS NOT A SET INDEX");
	
		DatabaseEntry param			= null;
		Object user_param  			= (Object)do_pop();

		if(is_multi)
		{
			List<Object> l_user_param;
			l_user_param = (List<Object>)user_param;
			int last 	 = l_user_param.size()-1;
			
			if (l_user_param.get(last) == Query.VAL_GLOB)
			  return get_globbed_multi_index_predicate_iterator(idx,l_user_param,iter_type,last);
			
			if(last != idx.getNumIndexedFields()-1 && iter_type != Q.STARTSWITH)
				throw new PersistenceException("WRONG NUMBER OF ARGUMENTS FOR INDEX. INDEX HAS "+(last+1)+" AND "+l_user_param.size()+" WERE PROVIDED");
 
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
				  return get_globbed_single_index_predicate_iterator(idx,iter_type);
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
			case Q.EQ:		
				iter 	= new EQIndexIterator();
				break;
			case Q.GT:
				iter 	= new GTIndexIterator();
				break;			
			case Q.GTE:
				iter 	= new GTEIndexIterator();
				break;			
			case Q.LT:
				iter 	= new LTIndexIterator();
				break;			
			case Q.LTE:
				iter 	= new LTIndexIterator();
				break;	
			case Q.STARTSWITH:
				iter 	= new STARTSWITHSTRINGIndexIterator();
				break;
			default:
				throw new PersistenceException("UNKNOWN PREDICATE ITER TYPE");
		}
		//open the iterator and return it//
		try{
			//TODO: this shoujld be a transaction no null as the first arg
			iter.open(null,idx, param);
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
	
	private IndexIterator get_globbed_multi_index_predicate_iterator(IterableIndex idx,List<Object> params,int iter_type,int glob_idx) throws PersistenceException
	{
		int i = glob_idx;
		switch(iter_type)
		{
			case Q.EQ:/* a globbed multi field equals is actually a between */
					List<Object> range_param = new ArrayList<Object>(params.size());
					range_param.addAll(params);
					do	
					{
						params.set(i,Query.VAL_MIN);
						range_param.set(i,Query.VAL_MAX);
					}while(i >= 0 && params.get(--i) == Query.VAL_GLOB);

					iter_type = Q.BETWEEN_INCLUSIVE_ASC;
					do_push(range_param);
					do_push(params);
					return setup_range_iterator(idx,true,iter_type);
			case Q.GT:
					do	
					{
						params.set(i,Query.VAL_MAX);
					}while(i >= 0 && params.get(--i) == Query.VAL_GLOB);
					
					iter_type = Q.GT;
					do_push(params);
					return setup_predicate_iterator(idx,true,iter_type);
			case Q.GTE:
					do	
					{
						params.set(i,Query.VAL_MIN);
					}while(i >= 0 && params.get(--i) == Query.VAL_GLOB);
	
					do_push(params);
					return setup_predicate_iterator(idx,true,iter_type);
			case Q.LT:
				do	
				{
					params.set(i,Query.VAL_MIN);
				}while(i >= 0 && params.get(--i) == Query.VAL_GLOB);
				do_push(params);
				return setup_predicate_iterator(idx,true,iter_type);
			case Q.LTE:
				do	
				{
					params.set(i,Query.VAL_MAX);
				}while(i >= 0 && params.get(--i) == Query.VAL_GLOB);
	
				do_push(params);
				return setup_predicate_iterator(idx,true,iter_type);
			case Q.STARTSWITH:
				do	
				{
					params.remove(i);
				}while(i >= 0 && params.get(--i) == Query.VAL_GLOB);
				
				do_push(params);
				return setup_predicate_iterator(idx,true,iter_type);
			default:
					throw new PersistenceException("UNKNOWN ITER TYPE IN SET UP GLOBBED PREDICATE MULTI ITER");
		}
	}
	
	private IndexIterator get_globbed_single_index_predicate_iterator(IterableIndex idx,int iter_type) throws PersistenceException
	{
		switch(iter_type)
		{
			case Q.EQ:			
			case Q.GT:	
			case Q.GTE:
			case Q.STARTSWITH:
				do_push(Query.VAL_MIN);
				iter_type = Q.GTE;
				return setup_predicate_iterator(idx, false, iter_type);
			case Q.LT:
			case Q.LTE:
				do_push(Query.VAL_MAX);
				iter_type = Q.LTE;
				return setup_predicate_iterator(idx, false, iter_type);
			default:
					throw new PersistenceException("UNKNOWN ITER TYPE IN SET UP GLOBBED PREDICATE SINGLE ITER");
		}
	}
	
	private IndexIterator setup_range_iterator(IterableIndex idx,boolean is_multi,int iter_type) throws PersistenceException
	{
		if(idx.isSetIndex())
			throw new PersistenceException("UNSUPPORTED OPERATION FOR INDEX."+idx.getName()+"IS NOT A SET INDEX");

		Object user_param  			= (Object)do_pop();
		Object user_range_param		= (Object)do_pop();
		DatabaseEntry param = null;
		DatabaseEntry range_param = null;
		
		if(is_multi)
		{	
			List<Object> l_user_param  = (List<Object>)user_param;
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
			case Q.BETWEEN_INCLUSIVE_ASC:		
				iter 	= new BETWEEN_ASC_INCLUSIVEIndexIterator();
				break;
			case Q.BETWEEN_INCLUSIVE_DESC:
				iter 	= new BETWEEN_DESC_INCLUSIVEIndexIterator();
				break;			
			case Q.BETWEEN_EXCLUSIVE_ASC:
				iter 	= new BETWEEN_ASC_EXCLUSIVEIndexIterator();
				break;			
			case Q.BETWEEN_EXCLUSIVE_DESC:
				iter 	= new BETWEEN_DESC_EXCLUSIVEIndexIterator();
				break;			
			case Q.BETWEEN_START_INCLUSIVE_ASC:
				iter 	= new BETWEEN_ASC_START_INCLUSIVEIndexIterator();
				break;	
			case Q.BETWEEN_START_INCLUSIVE_DESC:
				iter 	= new BETWEEN_DESC_START_INCLUSIVEIndexIterator();
				break;
			case Q.BETWEEN_END_INCLUSIVE_ASC:
				iter 	= new BETWEEN_ASC_END_INCLUSIVEIndexIterator();
				break;	
			case Q.BETWEEN_END_INCLUSIVE_DESC:
				iter 	= new BETWEEN_DESC_END_INCLUSIVEIndexIterator();
				break;	
			default:
				throw new PersistenceException("UNKNOWN RANGE ITER TYPE");
		}
		//open the iterator and return it//
		try{
			//TODO:// this should be a transaction as the first arg//
			iter.open(null,idx, param,range_param);
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
	
	private IndexIterator setup_set_iterator(IterableIndex idx,boolean is_multi,int iter_type) throws PersistenceException
	{
		if(idx.isNormalIndex())
			throw new PersistenceException("UNSUPPORTED OPERATION FOR INDEX."+idx.getName()+"IS A SET INDEX AND ONLY SUPPORTS SET QUERY OPERATIONS");

		List<Object> user_list_param = (List<Object>)do_pop();
		List<DatabaseEntry> list_param = null;
		boolean globbing = false;
		if(is_multi)
		{
			try{
				int last = user_list_param.size() - 1;
				if(user_list_param.get(last) == Query.VAL_GLOB)
				{
					do
					{
						user_list_param.remove(last--);						
					}
					while(user_list_param.get(last) == Query.VAL_GLOB);
					globbing = true;
				}
				list_param = ((MultiFieldArrayMembershipIndex)idx).getQueryKeys((List<Object>)user_list_param);
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
			case Q.SET_CONTAINS_ANY:
				iter 	= new SETCONTAINSANYIndexIterator();
				break;			
			case Q.SET_CONTAINS_ALL:		
				iter 	= new SETCONTAINSALLIndexIterator();
				break;
			default:
				throw new PersistenceException("UNKNOWN SET ITER TYPE");
		}
		//open the iterator and return it//
		try{
			//TODO:  this should be a transaction not nullas w the first arg
			iter.open(null,idx,globbing,list_param);
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