package com.pagesociety.bdb.index.query;

public class Q 
{
	public static final int GLOBAL_CONFIG_OP			= 0x10;	
	public static final int SET_CACHE_RESULTS			= 0x11;
	public static final int SET_IS_COMPLEX				= 0x12;
	
	public static final int OP							= 0x20;
	public static final int PUSH_CONSTANT_INT			= 0x21;
	public static final int PUSH_NULL					= 0x22;
	public static final int LOAD_VAR					= 0x23;
	public static final int LOAD_STRING_VAR				= 0x24;
	public static final int LOAD_PAGESIZE_VAR			= 0x25;
	public static final int LOAD_OFFSET_VAR				= 0x26;
	public static final int DO_ITER						= 0x27;
	public static final int RETURN						= 0x28;
	public static final int SET_RETURN_TYPE_REGISTER	= 0x29;
	public static final int SET_INDEX_NAME_REGISTER		= 0x30;
	public static final int SET_OFFSET_REGISTER			= 0x31;
	public static final int SET_PAGE_SIZE_REGISTER		= 0x32;
	public static final int SET_GP_REGISTER				= 0x33;
	public static final int LOAD_RETURN_TYPE_REGISTER	= 0x34;
	public static final int LOAD_INDEX_NAME_REGISTER	= 0x35;
	public static final int LOAD_OFFSET_REGISTER		= 0x36;
	public static final int LOAD_PAGE_SIZE_REGISTER		= 0x37;
	public static final int LOAD_GP_REGISTER			= 0x38;
	
	public static final int DO_INTERSECTION				= 0x401;
	public static final int DO_UNION					= 0x402;
	
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
	
	public static final int SET_ITER_TYPE				= 0x200;
	public static final int SET_CONTAINS_ALL				= 0x201;
	public static final int SET_CONTAINS_ANY				= 0x202;
	
	public static final int RESULT_SET_PROCESSING_OP	= 0x800;
	public static final int DO_ORDER_BY					= 0x801;
	public static final int DO_SUBSET					= 0x802;
	public static final int DO_REVERSE					= 0x803;
	
	public static final int FLOW_OP						= 0x1000;
	public static final int ENTER						= 0x1001;
	public static final int EXIT						= 0x1002;
		
	public static String toString(int code)
	{
		switch (code)
		{
			case GLOBAL_CONFIG_OP				: return "GLOBAL_CONFIG_OP";
			case SET_CACHE_RESULTS				: return "SET_CACHE_RESULTS";
			case SET_IS_COMPLEX					: return "SET_IS_COMPLEX";
			case OP								: return "OP";
			case PUSH_CONSTANT_INT				: return "PUSH_CONSTANT_INT";
			case PUSH_NULL						: return "PUSH_NULL";
			case LOAD_VAR						: return "LOAD_VAR";
			case LOAD_STRING_VAR				: return "LOAD_STRING_VAR";
			case LOAD_PAGESIZE_VAR				: return "LOAD_PAGESIZE_VAR";
			case LOAD_OFFSET_VAR				: return "LOAD_OFFSET_VAR";
			case DO_ITER						: return "DO_ITER";
			case RETURN							: return "RETURN";
			case SET_RETURN_TYPE_REGISTER		: return "SET_RETURN_TYPE_REGISTER";
			case SET_INDEX_NAME_REGISTER		: return "SET_INDEX_NAME_REGISTER";
			case SET_OFFSET_REGISTER			: return "SET_OFFSET_REGISTER";
			case SET_PAGE_SIZE_REGISTER			: return "SET_PAGE_SIZE_REGISTER";
			case SET_GP_REGISTER				: return "SET_GP_REGISTER";
			case LOAD_RETURN_TYPE_REGISTER		: return "LOAD_RETURN_TYPE_REGISTER";
			case LOAD_INDEX_NAME_REGISTER		: return "LOAD_INDEX_NAME_REGISTER";
			case LOAD_OFFSET_REGISTER			: return "LOAD_OFFSET_REGISTER";
			case LOAD_PAGE_SIZE_REGISTER		: return "LOAD_PAGE_SIZE_REGISTER";
			case LOAD_GP_REGISTER				: return "LOAD_GP_REGISTER";
			case DO_INTERSECTION				: return "DO_INTERSECTION";
			case DO_UNION						: return "DO_UNION";
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
			case RESULT_SET_PROCESSING_OP		: return "RESULT_SET_PROCESSING_OP";
			case DO_ORDER_BY					: return "DO_ORDER_BY";
			case DO_SUBSET						: return "DO_SUBSET";
			case DO_REVERSE						: return "DO_REVERSE";
			case FLOW_OP						: return "FLOW_OP";
			case ENTER							: return "ENTER";
			case EXIT							: return "EXIT";			
			default								: return "UNKNOWN";
		}
	}
			
}
