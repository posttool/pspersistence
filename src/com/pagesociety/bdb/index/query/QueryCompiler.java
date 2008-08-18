package com.pagesociety.bdb.index.query;

import java.util.ArrayList;
import java.util.List;

import com.pagesociety.persistence.Query;

public class QueryCompiler 
{
 
	public static final int ALL_RESULTS = Integer.MAX_VALUE;
	public static final Object VAL_MIN  = new Object();
	public static final Object VAL_MAX  = new Object();
	public static final Object VAL_NULL = new Object();
	public static final Object VAL_GLOB = new Object();
	
	private List<Object> 			_text_segment;//code
	private Object[] 	 			_data_segment;//params
	private int			 			_data_segment_length  	 = 0;
	private String[] 	 			_string_segment;//strings ??could be collapsed to data segment right now
	private int			 			_string_segment_length   = 0;
	private int[] 	 				_offset_segment;//strings ??could be collapsed to data segment right now
	private int			 			_offset_segment_length   = 0;
	private int[] 	 				_pagesize_segment;//strings ??could be collapsed to data segment right now
	private int			 			_pagesize_segment_length = 0;

	private boolean	is_complex 		= false;					
	private String					_the_return_type;
	private int						_the_offset;
	private int						_the_page_size;

	
	private int 					_instruction_offset 	= 0;
	private static final int  		MAX_LOCAL_VARS			= 32;
	private static final int  		MAX_SEGMENT   			= 16;
		
	
	public QueryCompiler(String return_type)
	{
		_text_segment 				   	= new ArrayList<Object>();
		_data_segment			   		= new Object[MAX_LOCAL_VARS];
		_string_segment					= new String[MAX_SEGMENT];
		_offset_segment			   		= new int[MAX_SEGMENT];
		_pagesize_segment				= new int[MAX_SEGMENT];
		_the_return_type				= return_type;
		_the_offset						= 0;
		_the_page_size					= Query.ALL_RESULTS;
		init_registers(_the_return_type, null, _the_offset, _the_page_size);
	}
	
	
	private void build_predicate_query(int op,Object val)
	{
		int i = define_local_var(val);//set it as a convenience. could require explict setParam() like store proc
		PUSH_LOCAL_VAR(i);		
		PUSH_CONSTANT_INT(op);
		EXEC(Q.DO_ITER);
	}

	
	private void build_range_query(int op,Object bottom_val,Object top_val)	{
		int i = define_local_var(top_val);//set it as a convenience. could require explict setParam() like store proc
		PUSH_LOCAL_VAR(i);
		i = define_local_var(bottom_val);//set it as a convenience. could require explict setParam() like store proc
		PUSH_LOCAL_VAR(i);
		PUSH_CONSTANT_INT(op);
		EXEC(Q.DO_ITER);
	}
	
	
	private void build_set_query(int op,Object val)
	{
		int i = define_local_var(val);//set it as a convenience. could require explict setParam() like store proc
		PUSH_LOCAL_VAR(i);
		PUSH_CONSTANT_INT(op);
		EXEC(Q.DO_ITER);
	}
	

	
	///end iterators///
	
	public QueryCompiler isAnyOf(List<Object> vals){return this;}
	
	public QueryCompiler orderBy(String attribute)
	{
		int i = define_local_string_var(attribute);//set it as a convenience. could require explict setParam() like store proc
		PUSH_STRING_VAR(i);
		EXEC(Q.DO_ORDER_BY);
		is_complex = true;
		return this;
	}
	
	public QueryCompiler startIntersection()
	{
		
		save_registers_to_stack();
		SET_PAGE_SIZE_REGISTER(0);
		PUSH_NULL();//null will terminate the list of result sets to do the union on on the stack
		is_complex = true;
		return this;
	}
	
	public QueryCompiler endIntersection()
	{
		EXEC(Q.DO_INTERSECTION);
		SET_GP_REGISTER();
		restore_registers_from_stack();
		LOAD_GP_REGISTER();
		return this;
	}
	
	public QueryCompiler startUnion()
	{

		save_registers_to_stack();
		SET_PAGE_SIZE_REGISTER(0);
		PUSH_NULL();//null will terminate the list of result sets to do the union on on the stack
		is_complex = true;
		return this;
	}
	
	public QueryCompiler endUnion()
	{
		EXEC(Q.DO_UNION);
		SET_GP_REGISTER();
		restore_registers_from_stack();
		LOAD_GP_REGISTER();
		return this;
	}
	
	public QueryCompiler cacheResults(boolean b)//set some header bytes SET_GLOBAL
	{
		//PUSH_CONSTANT_INT(b);
		//EXEC(Q.SET_CACHE_RESULTS);
		return this;
	}
	
	
	public void ret()
	{
		//SET_GP_REGISTER();
		//restore_registers_from_stack();
		//LOAD_GP_REGISTER();
		EXEC(Q.RETURN);
	}
	
	//frame options//
	public QueryCompiler returnType(String entity_type)
	{
		int i = define_local_string_var(entity_type);
		SET_RETURN_TYPE_REGISTER(i);
		return this;
	}
	
	public QueryCompiler idx(String index_name)
	{
		int i = define_local_string_var(index_name);
		SET_INDEX_NAME_REGISTER(i);
		return this;
	}

	public QueryCompiler pageSize(int page_size)
	{
//		if(!is_complex)
//			_the_page_size = page_size;
		
		int i = define_local_pagesize_var(page_size);
		SET_PAGE_SIZE_REGISTER(i);
		return this;
	}
	
	public QueryCompiler offset(int offset)
	{
		//if(!is_complex)
		//	_the_offset = offset;
		
		int i = define_local_offset_var(offset);
		SET_OFFSET_REGISTER(i);
		return this;
	}

	public void setParam(int idx,Object value)
	{
		_data_segment[idx] = value;
	}

	
	public List<Object> getProgram()
	{
		return _text_segment;
	}
	
	public Object[] getParams()
	{
		return _data_segment;
	}
	
	public int getNumParams()
	{
		return _data_segment_length;
	}
	
	public String[] getStrings()
	{
		return _string_segment;
	}
	
	public int getNumStrings()
	{
		return _string_segment_length;
	}
	
	public int[] getOffsets()
	{
		return _offset_segment;
	}
	
	public int getNumOffsets()
	{
		return _offset_segment_length;
	}
	
	public int[] getPageSizes()
	{
		return _pagesize_segment;
	}
	
	public int getNumPageSizes()
	{
		return _pagesize_segment_length;
	}
	
	public int getPageSize()
	{
		return _the_page_size;
	}
	
	public int getOffset()
	{
		return _the_offset;
	}
	
	private int define_local_var(Object val)
	{
		int ret = _data_segment_length;
		_data_segment[_data_segment_length++] = val;
		return ret;//return index of set variable
	}
	
	private int define_local_string_var(String val)
	{
		int ret = _string_segment_length;
		_string_segment[_string_segment_length++] = val;	
		return ret;//return index of set string
	}
	
	private int define_local_offset_var(int val)
	{
		int ret = _offset_segment_length;
		_offset_segment[_offset_segment_length++] = val;	
		return ret;//return index of set string
	}
	
	private int define_local_pagesize_var(int val)
	{
		int ret = _pagesize_segment_length;
		_pagesize_segment[_pagesize_segment_length++] = val;	
		return ret;//return index of set string
	}

	private void init_registers(String return_type,String index_name,int offset,int page_size)
	{
		int i;
		i = define_local_string_var(return_type);
		
		PUSH_STRING_VAR(i);
		_text_segment.add(Q.SET_RETURN_TYPE_REGISTER);		
		
		i = define_local_string_var(index_name);
		PUSH_STRING_VAR(i);
		_text_segment.add(Q.SET_INDEX_NAME_REGISTER);
		
		i = define_local_offset_var(offset);
		PUSH_OFFSET_VAR(i);
		_text_segment.add(Q.SET_OFFSET_REGISTER);		
		
		i = define_local_pagesize_var(page_size);
		PUSH_PAGESIZE_VAR(i);		
		_text_segment.add(Q.SET_PAGE_SIZE_REGISTER);
	}
	
	
	private void save_registers_to_stack()
	{
		_text_segment.add(Q.LOAD_PAGE_SIZE_REGISTER);
		_text_segment.add(Q.LOAD_OFFSET_REGISTER);
		_text_segment.add(Q.LOAD_INDEX_NAME_REGISTER);
		_text_segment.add(Q.LOAD_RETURN_TYPE_REGISTER);
	}
	
	private void restore_registers_from_stack()
	{
		_text_segment.add(Q.SET_RETURN_TYPE_REGISTER);
		_text_segment.add(Q.SET_INDEX_NAME_REGISTER);
		_text_segment.add(Q.SET_OFFSET_REGISTER);
		_text_segment.add(Q.SET_PAGE_SIZE_REGISTER);		
	}
	
	private void PUSH_NULL()//maybe not needed anymore
	{
		_text_segment.add(Q.PUSH_NULL);
		_instruction_offset+=1;
	}
	
	private void PUSH_CONSTANT_INT(Integer val)//maybe not needed anymore
	{
		_text_segment.add(Q.PUSH_CONSTANT_INT);
		_text_segment.add(val);
		_instruction_offset+=2;
	}

	private void PUSH_LOCAL_VAR(int idx)
	{

		_text_segment.add(Q.LOAD_VAR);
		_text_segment.add(idx);		
		_instruction_offset+=2;
	}
	
	private void PUSH_STRING_VAR(int idx)
	{
		_text_segment.add(Q.LOAD_STRING_VAR);
		_text_segment.add(idx);
		_instruction_offset+=2;
	}
	
	private void PUSH_OFFSET_VAR(int idx)
	{
		_text_segment.add(Q.LOAD_OFFSET_VAR);
		_text_segment.add(idx);
		_instruction_offset+=2;
	}
	
	private void PUSH_PAGESIZE_VAR(int idx)
	{
		_text_segment.add(Q.LOAD_PAGESIZE_VAR);
		_text_segment.add(idx);
		_instruction_offset+=2;
	}
	
	private void SET_RETURN_TYPE_REGISTER(int offset_into_strings)
	{
		PUSH_STRING_VAR(offset_into_strings);
		_text_segment.add(Q.SET_RETURN_TYPE_REGISTER);
		_instruction_offset++;
	}
	
	private void SET_INDEX_NAME_REGISTER(int offset_into_strings)
	{
		PUSH_STRING_VAR(offset_into_strings);
		_text_segment.add(Q.SET_INDEX_NAME_REGISTER);
		_instruction_offset++;
	}
	
	private void SET_OFFSET_REGISTER(int offset_var_idx)
	{
		PUSH_OFFSET_VAR(offset_var_idx);
		_text_segment.add(Q.SET_OFFSET_REGISTER);
		_instruction_offset++;
	}
	
	private void SET_PAGE_SIZE_REGISTER(int page_size_var_idx)
	{
		PUSH_PAGESIZE_VAR(page_size_var_idx);
		_text_segment.add(Q.SET_PAGE_SIZE_REGISTER);
		_instruction_offset++;
	}
	
	private void SET_GP_REGISTER()
	{
		_text_segment.add(Q.SET_GP_REGISTER);	
		_instruction_offset++;
	}
	
	private void LOAD_GP_REGISTER()
	{
		_text_segment.add(Q.LOAD_GP_REGISTER);	
		_instruction_offset+=1;
	}
	
	private void EXEC(int opcode)
	{
		_text_segment.add(opcode);
		_instruction_offset++;
	}

}
