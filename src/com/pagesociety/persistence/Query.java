package com.pagesociety.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

//import com.pagesociety.bdb.index.query.Q;

public class Query 
{

	/* The query class builds up a parse tree of an abstract query language
	 * as you interact with it. Run main to see what I am talking about.
	 * It should be possible to write an interpreter for this AST for any
	 * database
	 */
	
	public static void main(String[] args)
	{
		Query q;
	
		/* union */
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.startUnion();
			q.lt("Gigi");
			q.gt("Daya");
		q.endUnion();	
		q.orderBy("Last Name");
		q.ret();

		System.out.println("UNION");
		System.out.println(q);
		
		/* between exclusive descending */
		q = new Query("Author");
		q.pageSize(Query.ALL_RESULTS);
		q.idx("byFirstName");
		q.betweenExclusiveDesc("Gigi", "Daya");
		q.ret();
		
		System.out.println("BETWEEN");
		System.out.println(q);
		
	/* ficticiuos rediculous */
		q = new Query("Author");
		q.pageSize(15);
			q.startIntersection();
			q.idx("byFirstName");
				q.startUnion();
					q.pageSize(100);	
						q.eq("Gigi");
						q.eq("Daya");
				q.endUnion();
				q.startUnion();
					q.pageSize(115);		
						q.eq("Fox");
						q.eq("Hound");
				q.endUnion();
			q.endIntersection();
		q.orderBy("Last Name");
		q.cacheResults(false);
		q.ret();
		
		System.out.println("REDICULOUS A:");
		System.out.println(q);
		
		q.setOffset(25); 
		q.setPageSize(25);
		q.setParam(0,"Gigi_1");
		q.setParam(1,"Daya_1"); 
		q.setParam(2,"hello");
		q.setParam(3,"there"); 
		System.out.println(q.toString());
		System.out.println("REDICULOUS B:");
		System.out.println(q);
	}
	
	//..........................................................................//
	
	
	public static final int ALL_RESULTS = Integer.MAX_VALUE;
	public static final Object VAL_MIN  = new Object();
	public static final Object VAL_MAX  = new Object();
	public static final Object VAL_GLOB = new Object();
	public static final String PRIMARY_IDX = new String("_byId_");
	public static final int ASC			   = 0x00;
	public static final int DESC		   = 0x01;
	public static final int NODE_TYPE_COMPOUND		= 0x20;
	public static final int NODE_TYPE_INTERSECTION 	= 0x21;
	public static final int NODE_TYPE_UNION			= 0x22;
	public static final int NODE_TYPE_SIMPLE		= 0x40;
	public static final int NODE_TYPE_ITER			= 0x41;
	
	public class QueryNode
	{
		public QueryNode(int type)
		{
			this.type = type;
			this.children = new ArrayList<QueryNode>(4);
			this.attributes = new HashMap<String,Object>();
		}
		public int type;
		public Map<String,Object> attributes;
		public List<QueryNode> children;
		
	}
	private QueryNode    _root_node;
	private StringBuffer _cache_key_buf;
	private String		 _cache_key_str;
	public Query(String return_type)
	{
		setup_root_node(return_type);
		push_block(_root_node);
		_cache_key_buf = new StringBuffer(64);
		_cache_key_buf.append(return_type+":");
	}
	

	/*predicate helpers */
	public Query eq(Object val){build_predicate_query(EQ,val);return this;}
	public Query eq(List<?> vals){build_predicate_query(EQ,vals);return this;}	
	public Query gt(Object val){build_predicate_query(GT,val);return this;}
	public Query gt(List<?> vals){build_predicate_query(GT,vals);return this;}
	public Query gte(Object val){build_predicate_query(GTE,val);return this;}
	public Query gte(List<?> vals){build_predicate_query(GTE,vals);return this;}
	public Query lt(Object val){build_predicate_query(LT,val);return this;}
	public Query lt(List<?> vals){build_predicate_query(LT,vals);return this;}
	public Query lte(Object val){build_predicate_query(LTE,val);return this;}
	public Query lte(List<?> vals){build_predicate_query(LTE,vals);return this;}	
	public Query startsWith(Object val){build_predicate_query(STARTSWITH,val);return this;}
	public Query startsWith(List<?> vals){build_predicate_query(STARTSWITH,vals);return this;}
	/*range helpers */
	public Query between(Object bottom_val,Object top_val){build_range_query(BETWEEN_INCLUSIVE_ASC, bottom_val, top_val);return this;}
	public Query between(List<?> bottom_vals,List<?> top_vals){build_range_query(BETWEEN_INCLUSIVE_ASC, bottom_vals, top_vals);return this;}
	public Query betweenExclusive(Object bottom_val,Object top_val){build_range_query(BETWEEN_EXCLUSIVE_ASC, bottom_val, top_val);return this;}
	public Query betweenExclusive(List<?> bottom_vals,List<?> top_vals){build_range_query(BETWEEN_EXCLUSIVE_ASC, bottom_vals, top_vals);return this;}
	public Query betweenStartInclusive(Object bottom_val,Object top_val){build_range_query(BETWEEN_START_INCLUSIVE_ASC, bottom_val, top_val);return this;}
	public Query betweenStartInclusive(List<?> bottom_vals,List<?> top_vals){build_range_query(BETWEEN_START_INCLUSIVE_ASC, bottom_vals, top_vals);return this;	}	
	public Query betweenEndInclusive(Object bottom_val,Object top_val){build_range_query(BETWEEN_END_INCLUSIVE_ASC, bottom_val, top_val);return this;}
	public Query betweenEndInclusive(List<?> bottom_vals,List<?> top_vals){build_range_query(BETWEEN_END_INCLUSIVE_ASC, bottom_vals, top_vals);return this;}
	public Query betweenDesc(Object top_val,Object bottom_val){build_range_query(BETWEEN_INCLUSIVE_DESC, top_val, bottom_val);return this;}
	public Query betweenDesc(List<?> top_vals,List<?> bottom_vals){build_range_query(BETWEEN_INCLUSIVE_DESC, top_vals, bottom_vals);return this;}
	public Query betweenExclusiveDesc(Object top_val,Object bottom_val){build_range_query(BETWEEN_EXCLUSIVE_DESC, top_val, bottom_val);return this;}
	public Query betweenExclusiveDesc(List<?> top_vals,List<?> bottom_vals){build_range_query(BETWEEN_EXCLUSIVE_DESC,top_vals, bottom_vals);return this;}
	public Query betweenStartInclusiveDesc(Object top_val,Object bottom_val){build_range_query(BETWEEN_START_INCLUSIVE_DESC, top_val, bottom_val);return this;}
	public Query betweenStartInclusiveDesc(List<?> top_vals,List<?> bottom_vals){build_range_query(BETWEEN_START_INCLUSIVE_DESC, top_vals, bottom_vals);return this;}
	public Query betweenEndInclusiveDesc(Object top_val,Object bottom_val){build_range_query(BETWEEN_END_INCLUSIVE_DESC, top_val, bottom_val);return this;}
	public Query betweenEndInclusiveDesc(List<?> top_vals,List<?> bottom_vals){build_range_query(BETWEEN_END_INCLUSIVE_DESC, top_vals, bottom_vals);return this;}	
	/* set helpers */
	public Query setContainsAll(List<?> vals){build_set_query(SET_CONTAINS_ALL, vals);return this;}
	public Query setContainsAny(List<?> vals){build_set_query(SET_CONTAINS_ANY, vals);return this;}
	/* freetext helpers */
	public Query textContainsAll(List<?> vals){build_freetext_query(FREETEXT_CONTAINS_ALL, vals);return this;}
	public Query textContainsAny(List<?> vals){build_freetext_query(FREETEXT_CONTAINS_ANY, vals);return this;}
	public Query textContainsPhrase(List<?> vals){build_freetext_query(FREETEXT_CONTAINS_PHRASE, vals);return this;}
	
	

	/* currently unimplemented ops */
	public Query isAnyOf(List<?> vals){return this;}

	
	private void build_predicate_query(int op,Object val)
	{
		QueryNode iter = new QueryNode(NODE_TYPE_ITER);
		copy_block_context(current_block(), iter);
		iter.attributes.put(ATT_ITER_OP, op);
		int i = define_param(val);
		iter.attributes.put(ATT_PREDICATE_ITER_USER_PARAM,i);
		current_block().children.add(iter);
		_cache_key_buf.append("_OP:"+queryOpToString(op));
	}

	private void build_range_query(int op,Object bottom_val,Object top_val)	{
		QueryNode iter = new QueryNode(NODE_TYPE_ITER);
		copy_block_context(current_block(), iter);
		iter.attributes.put(ATT_ITER_OP, op);
		int i = define_param(bottom_val);
		iter.attributes.put(ATT_RANGE_ITER_USER_BOTTOM_PARAM,i);
		i = define_param(top_val);
		iter.attributes.put(ATT_RANGE_ITER_USER_TOP_PARAM,i);
		current_block().children.add(iter);
		_cache_key_buf.append("_OP:"+queryOpToString(op));
	}
	
	private void build_set_query(int op,Object val)
	{
		QueryNode iter = new QueryNode(NODE_TYPE_ITER);
		copy_block_context(current_block(), iter);
		iter.attributes.put(ATT_ITER_OP, op);
		int i = define_param(val);
		iter.attributes.put(ATT_SET_ITER_USER_PARAM, i);
		current_block().children.add(iter);
		_cache_key_buf.append("_OP:"+queryOpToString(op));
	}
	
	private void build_freetext_query(int op,Object val)
	{
		QueryNode iter = new QueryNode(NODE_TYPE_ITER);
		copy_block_context(current_block(), iter);
		iter.attributes.put(ATT_ITER_OP, op);
		int i = define_param(val);
		iter.attributes.put(ATT_SET_ITER_USER_PARAM, i);
		current_block().children.add(iter);
		_cache_key_buf.append("_OP:"+queryOpToString(op));
	}
	/* helper for passing in freetext queries as a single string instead of List<String> */
	private static List<String> string_to_list(String s)
	{
		List<String> ret = new ArrayList<String>();
		StringTokenizer st = new StringTokenizer(s);
		while(st.hasMoreElements())
			ret.add((String)st.nextElement());
		return ret;
	}
	private void setup_root_node(String return_type)
	{
		_root_node = new QueryNode(0);
		_root_node.attributes.put(ATT_RETURN_TYPE, return_type);
		//int i = define_offset_var(0);
		_root_node.attributes.put(ATT_OFFSET,0);
		//i = define_pagesize_var(Query.ALL_RESULTS);
		_root_node.attributes.put(ATT_PAGE_SIZE, Query.ALL_RESULTS);
		_root_node.attributes.put(ATT_INDEX_NAME, null);
		_root_node.attributes.put(ATT_ORDER_FIELDNAME, null);		
		_root_node.attributes.put(ATT_CACHE_RESULTS, true);				
	}

//start per block commands//	
	public Query orderBy(String attribute)
	{
		current_block().attributes.put(ATT_ORDER_FIELDNAME, attribute);
		current_block().attributes.put(ATT_ORDER_ORDER, Query.ASC);
		_cache_key_buf.append("_OB:"+attribute+(Query.ASC));
		return this;
	}
	
	public Query orderBy(String attribute,int direction)
	{
		current_block().attributes.put(ATT_ORDER_FIELDNAME, attribute);
		current_block().attributes.put(ATT_ORDER_ORDER, direction);
		_cache_key_buf.append("_OB:"+attribute+direction);
		return this;
	}
	
	public Query startIntersection()
	{
		QueryNode intersection = new QueryNode(NODE_TYPE_INTERSECTION);
		copy_block_context(current_block(), intersection);
		intersection.attributes.put(ATT_OFFSET, 0);
		intersection.attributes.put(ATT_PAGE_SIZE, Query.ALL_RESULTS);
		intersection.attributes.put(ATT_CACHE_RESULTS, false);
		current_block().children.add(intersection);
		push_block(intersection);
		_cache_key_buf.append("_I");
		return this;
	}
	
	private void copy_block_context(QueryNode from, QueryNode to)
	{
		to.attributes.put(ATT_RETURN_TYPE, from.attributes.get(ATT_RETURN_TYPE));
		to.attributes.put(ATT_OFFSET, from.attributes.get(ATT_OFFSET));
		to.attributes.put(ATT_INDEX_NAME, from.attributes.get(ATT_INDEX_NAME));
		to.attributes.put(ATT_PAGE_SIZE, from.attributes.get(ATT_PAGE_SIZE));
	}
	
	public Query endIntersection()
	{
		pop_block();
		return this;
	}
		
	public Query startUnion()
	{
		QueryNode union = new QueryNode(NODE_TYPE_UNION);
		copy_block_context(current_block(), union);
		union.attributes.put(ATT_OFFSET, 0);
		union.attributes.put(ATT_PAGE_SIZE, Query.ALL_RESULTS);
		union.attributes.put(ATT_CACHE_RESULTS, false);
		current_block().children.add(union);
		push_block(union);
		_cache_key_buf.append("_U");
		return this;
	}
	
	public Query endUnion()
	{
		pop_block();
		return this;
	}
	
	public Query returnType(String entity_type)
	{
		current_block().attributes.put(ATT_RETURN_TYPE, entity_type);
		_cache_key_buf.append("_RT:"+entity_type);
		return this;
	}
	
	public Query idx(String index_name)
	{
		current_block().attributes.put(ATT_INDEX_NAME,index_name);
		_cache_key_buf.append("_IDX:"+index_name);
		return this;
	}

	public Query offset(int offset)
	{
		int i = define_offset_var(offset);
		current_block().attributes.put(ATT_OFFSET,_offset_vars[i]);
		if(_current_block_idx != 0)
			_cache_key_buf.append("_OFST:"+offset);
		return this;
	}
	
	public Query pageSize(int page_size)
	{
		int i = define_pagesize_var(page_size);
		current_block().attributes.put(ATT_PAGE_SIZE,_pagesize_vars[i]);
		if(_current_block_idx != 0)
			_cache_key_buf.append("_PGSZ:"+page_size);	
		return this;
	}
	
	public Query cacheResults(boolean b)
	{
		current_block().attributes.put(ATT_CACHE_RESULTS, b);
		return this;
	}
	
	public void ret()
	{
		//do nothing...we could add a node of type return. not a big deal if we don't have 
		//control flow//
	}
	//end per block commands
		
	//settable attributes on a 'compiled' query//
	public void setParam(int idx,Object value)
	{
		_params[idx] = value;
	}
	
	public Object[] getParams()
	{
		return _params;
	}
	
	public int getNumParams()
	{
		return _current_param_idx;
	}
	
	
	public Query setPageSize(int page_size)
	{
		_root_node.attributes.put(ATT_PAGE_SIZE,page_size);
		return this;
	}

	public Query setOffset(int offset)
	{
		_root_node.attributes.put(ATT_OFFSET,offset);
		return this;
	}
	
	//canonical values for these parameters. used for cacheing strategy
	public int getPageSize()
	{
		return (Integer)_root_node.attributes.get(ATT_PAGE_SIZE);
	}
	
	public int getOffset()
	{
		return (Integer)_root_node.attributes.get(ATT_OFFSET);
	}
	
	public String getReturnType()
	{
		return (String)_root_node.attributes.get(ATT_RETURN_TYPE);
	}
	
	public boolean getCacheResults()
	{
		return (Boolean)_root_node.attributes.get(ATT_CACHE_RESULTS);
	}
	
	public QueryNode getRootNode()
	{
		return _root_node;
	}
	
	/* returns a unique identifier for the query independent of outer most page size and offset*/
	/* the rest of query equality would be matching on this in addition to params. this enables us to 
	/* have prepared statement types of behavior in a sql impl. when this query tree is parsed one
	 * could make a prepared statement instead of a statement and cache the prepared statement
	 * by this query key*/
	public String getCacheKey()
	{
		if(_cache_key_str == null)
			_cache_key_str = _cache_key_buf.toString();
		return _cache_key_str;
	}
	
	public String toString()
	{
		StringBuffer buf = new StringBuffer();
		sprint_node(getRootNode(),buf,1);
		return buf.toString();
	}
	
	private void sprint_node(QueryNode node,StringBuffer buf,int indent)
	{
		sprint_indent(buf, indent);
		switch(node.type)
		{
			case 0: //root node
				buf.append("[ROOT]\n");
				sprint_attributes(buf,node,indent);
				for(int i = 0;i < node.children.size();i++)
					sprint_node(node.children.get(i), buf,indent+1);
				break;
			case NODE_TYPE_INTERSECTION:
				buf.append("[INTERSECTION]\n");
				sprint_attributes(buf,node,indent+1);
				for(int i = 0;i < node.children.size();i++)
					sprint_node(node.children.get(i), buf,indent+1);
				break;
			case NODE_TYPE_UNION:
				buf.append("[UNION]\n");
				sprint_attributes(buf,node,indent+1);
				for(int i = 0;i < node.children.size();i++)
					sprint_node(node.children.get(i), buf,indent+1);
				break;
			case NODE_TYPE_ITER:
				buf.append("[ITER]\n");
				sprint_attributes(buf,node,indent+1);
				for(int i = 0;i < node.children.size();i++)
					sprint_node(node.children.get(i), buf,indent+1);
				break;
		}
	}
		
	private void sprint_attributes(StringBuffer buf,QueryNode node,int indent)
	{
		sprint_indent(buf, indent);
		buf.append("NODE: type="+node.type+"\n");
		for(int i = 0; i < sprintf_attribute_list.length;i++)
		{
			sprint_indent(buf, indent);
			buf.append(sprintf_attribute_list[i]+" = "+node.attributes.get(sprintf_attribute_list[i])+"\n");
		}
		if(node.type == NODE_TYPE_ITER)
		{
			sprint_indent(buf,indent);
			int op = (Integer)node.attributes.get(ATT_ITER_OP);
			buf.append("iter op = "+queryOpToString(op)+"\n");
			sprint_indent(buf,indent);
			if((op & PREDICATE_ITER_TYPE) == PREDICATE_ITER_TYPE)
			{
				buf.append("p_user_param = "+_params[(Integer)node.attributes.get(ATT_PREDICATE_ITER_USER_PARAM)]+"\n");
			}
			else if((op & BETWEEN_ITER_TYPE) == BETWEEN_ITER_TYPE)
			{
				buf.append("p_user_bottom_param = "+_params[(Integer)node.attributes.get(ATT_RANGE_ITER_USER_BOTTOM_PARAM)]+"\n");
				sprint_indent(buf,indent);
				buf.append("p_user_top_param = "+_params[(Integer)node.attributes.get(ATT_RANGE_ITER_USER_TOP_PARAM)]+"\n");
			}
			else if((op & SET_ITER_TYPE) == SET_ITER_TYPE)
			{
				buf.append("s_user_param = "+_params[(Integer)node.attributes.get(ATT_SET_ITER_USER_PARAM)]+"\n");
			}
			else
			{
				buf.append("PARSE ERROR: UNKNOWN ITER TYPE "+Integer.toHexString(node.type));
			}
		}
	}
	
	private void sprint_indent(StringBuffer buf,int indent)
	{
		for(int i = 0; i < indent;i++)
			buf.append("\t");
	}
	

	

	//access to variable tables for parse tree
	public int[] getOffsetVars()
	{
		return _offset_vars;
	}
	
	public int getNumOffsetVars()
	{
		return _offset_vars.length;
	}
	
	public int[] getPageSizeVars()
	{
		return _pagesize_vars;
	}
	
	public int getNumPageSizeVars()
	{
		return _pagesize_vars.length;
	}
	
	
	private int define_param(Object val)
	{
		int ret = _current_param_idx;
		_params[_current_param_idx++] = val;
		return ret;//return index of set variable
	}
	
	
	private static final int  		MAX_PARAMS	= 32;
	private Object[]_params 		= new Object[MAX_PARAMS];
	private int _current_param_idx = 0;	
	private int[] _offset_vars = new int[MAX_PARAMS];
	private int _current_offset_idx = 0;
	private int[] _pagesize_vars = new int[MAX_PARAMS];
	private int _current_pagesize_idx = 0;
	private int define_offset_var(int val)
	{
		int ret = _current_offset_idx;
		_offset_vars[_current_offset_idx++] = val;	
		return ret;//return index of set string
	}
	
	private int define_pagesize_var(int val)
	{
		int ret = _current_pagesize_idx;
		_pagesize_vars[_current_pagesize_idx++] = val;	
		return ret;//return index of set string
	}


	//stuff for dealing with keeping track of "blocks"
	private static final int MAX_BLOCKS = 16;
	private QueryNode[] _blocks = new QueryNode[MAX_BLOCKS];
	private int _current_block_idx = -1;
	private void push_block(QueryNode block)
	{
		_blocks[++_current_block_idx] = block;
	}
	
	private QueryNode pop_block()
	{
		return _blocks[_current_block_idx--];
	}
	
	private QueryNode current_block()
	{
		return _blocks[_current_block_idx];
	}

	public static final String ATT_RETURN_TYPE   			= "return_type";
	public static final String ATT_INDEX_NAME    			= "index_name";
	public static final String ATT_OFFSET		  			= "offset";
	public static final String ATT_PAGE_SIZE				= "page_size";
	public static final String ATT_ORDER_FIELDNAME		  	= "order_field";
	public static final String ATT_ORDER_ORDER			  	= "order_order";
	public static final String ATT_CACHE_RESULTS 			= "cache_results";
	public static final String ATT_ITER_OP 	  				= "iter_op";
	public static final String ATT_PREDICATE_ITER_USER_PARAM 	 = "p_user_param";
	public static final String ATT_RANGE_ITER_USER_BOTTOM_PARAM = "r_user_bottom_param";
	public static final String ATT_RANGE_ITER_USER_TOP_PARAM 	 = "r_user_top_param";
	public static final String ATT_SET_ITER_USER_PARAM 	 	 = "s_user_param";
	
	public static final int PREDICATE_ITER_TYPE			= 0x80;
	public static final int EQ 							= 0x81;
	public static final int GT 							= 0x82;
	public static final int GTE 						= 0x83;
	public static final int LT  						= 0x84;
	public static final int LTE 						= 0x85;
	public static final int STARTSWITH 					= 0x86;
	public static final int IS_ANY_OF					= 0x87;
	
	public static final int BETWEEN_ITER_TYPE			= 0x100;
	public static final int BETWEEN_INCLUSIVE_ASC 		= 0x101;
	public static final int BETWEEN_EXCLUSIVE_ASC 		= 0x102;
	public static final int BETWEEN_START_INCLUSIVE_ASC	= 0x103;
	public static final int BETWEEN_END_INCLUSIVE_ASC	= 0x104;
	public static final int BETWEEN_INCLUSIVE_DESC 		= 0x105;
	public static final int BETWEEN_EXCLUSIVE_DESC 		= 0x106;
	public static final int BETWEEN_START_INCLUSIVE_DESC	= 0x107;
	public static final int BETWEEN_END_INCLUSIVE_DESC	= 0x108;	
	
	public static final int SET_ITER_TYPE					= 0x200;
	public static final int SET_CONTAINS_ALL				= 0x201;
	public static final int SET_CONTAINS_ANY				= 0x202;
	
	public static final int FREETEXT_ITER_TYPE				= 0x400;
	public static final int FREETEXT_CONTAINS_ALL			= 0x401;
	public static final int FREETEXT_CONTAINS_ANY			= 0x402;
	public static final int FREETEXT_CONTAINS_PHRASE		= 0x403;
	
	private static final String[] sprintf_attribute_list = new String[]
	         	                                                     {
	         															ATT_RETURN_TYPE,
	         															ATT_INDEX_NAME,
	         															ATT_OFFSET,
	         															ATT_PAGE_SIZE,
	         															ATT_ORDER_FIELDNAME,
	         															ATT_ORDER_ORDER,
	         															ATT_CACHE_RESULTS,
	         	                                                     };

	public static String queryOpToString(int code)
	{
		switch (code)
		{
			case PREDICATE_ITER_TYPE			: return "PREDICATE_ITER_TYPE";
			case EQ 							: return "EQ";
			case GT 							: return "GT";
			case GTE 							: return "GTE";
			case LT  							: return "LT";
			case LTE 							: return "LTE";
			case STARTSWITH 					: return "STARTSWITH";
			case IS_ANY_OF						: return "IS_ANY_OF";
			case BETWEEN_ITER_TYPE				: return "BETWEEN_ITER_TYPE";
			case BETWEEN_INCLUSIVE_ASC 			: return "BETWEEN_INCLUSIVE_ASC";
			case BETWEEN_EXCLUSIVE_ASC 			: return "BETWEEN_EXCLUSIVE_ASC";
			case BETWEEN_START_INCLUSIVE_ASC	: return "BETWEEN_START_INCLUSIVE_ASC";
			case BETWEEN_END_INCLUSIVE_ASC		: return "BETWEEN_END_INCLUSIVE_ASC";
			case BETWEEN_INCLUSIVE_DESC 		: return "BETWEEN_INCLUSIVE_DESC";
			case BETWEEN_EXCLUSIVE_DESC 		: return "BETWEEN_EXCLUSIVE_DESC";
			case BETWEEN_START_INCLUSIVE_DESC	: return "BETWEEN_START_INCLUSIVE_DESC";
			case BETWEEN_END_INCLUSIVE_DESC		: return "BETWEEN_END_INCLUSIVE_DESC";	
			case SET_ITER_TYPE					: return "SET_ITER_TYPE";
			case SET_CONTAINS_ALL				: return "SET_CONTAINS_ALL";
			case SET_CONTAINS_ANY				: return "SET_CONTAINS_ANY";
			default								: return "Unknown Query Op";
		}
	}
	
	/*utility method to make query look tight when using multi indexes*/
	public List<?> list(Object ...vals)
	{
		List<Object> ret = new ArrayList<Object>();
		for(int i = 0;i < vals.length;i++)
			ret.add(vals[i]);
		return ret;
	}
}
