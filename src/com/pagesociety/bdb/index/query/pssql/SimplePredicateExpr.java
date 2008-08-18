package com.pagesociety.bdb.index.query.pssql;

import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.Query;
import com.pagesociety.persistence.Types;


public class SimplePredicateExpr extends PredicateExpr
{
	private int op;
	private String field;
	private Object value;
	
	
	SimplePredicateExpr(int op,String field,Object value)
	{
		this.op = op;
		this.field = field;
		this.value = value;
	}
	
	private static Object coerce_value(int type,Object value) throws PSSqlException
	{
		if(value == null)
			return null;
		switch(type)
		{
			case Types.TYPE_DOUBLE:
				if(value.getClass() == Integer.class)
					return new Double((Integer)value);
				else if(value.getClass() == Float.class)
					return new Double((Float)value);			
				break;
			case Types.TYPE_FLOAT:
				if(value.getClass() == Integer.class)
					return new Float((Integer)value);
				break;
			case Types.TYPE_LONG:
				if(value.getClass() == Integer.class)
					return new Long((Integer)value);
				else if(value.getClass() == Float.class)
					return ((Float)value).longValue();
				break;
			case Types.TYPE_INT:	
				if(value.getClass() == Float.class)
					return ((Float)value).intValue();
				break;
		}
		return value;
	}
	
	public boolean eval(EntityDefinition def,Entity candidate) throws PSSqlException
	{
		if(def.getField(field)==null)
			throw PSSqlException.EXEC_EXCEPTION("Field "+field+" does not exist in entity "+def.getName(), field+" "+opToString(op)+" "+value);
		
		int field_type					 = def.getField(field).getBaseType();
		value = coerce_value(field_type,value);
		
		Object candidate_raw_value 		 = candidate.getAttribute(field);
		Comparable<Object> val 			 = (Comparable<Object>)value;
		Comparable<Object> candidate_val = (Comparable<Object>)candidate_raw_value;
		
		if(candidate_val == null)
			return(value == null);
		if(val == null)
			return false;
		
		int res;
		switch(op)
		{
			case Query.EQ:
				res = (candidate_val).compareTo(val);
				return(res == 0);
			case Query.GT:
				res = (candidate_val).compareTo(value);
				return(res > 0);
			case Query.GTE:
				res = (candidate_val).compareTo(value);
				return(res >= 0);
			case Query.LT:
				res = (candidate_val).compareTo(value);
				return(res < 0);
			case Query.LTE:
				res = (candidate_val).compareTo(value);
				return(res <= 0);
			case Query.STARTSWITH:
				return ((String)candidate_raw_value).startsWith((String)value);
			default:
				PSSqlException.EXEC_EXCEPTION("Unknown Op: "+op, "");
		
		}
		
		return true;
	}
	
	
	public void toBuf(int indent,StringBuilder buf)
	{
		indent(indent,buf);
		String s_op = opToString(op);
		buf.append(field+" "+s_op+" "+((value==null)?"null":value.toString())+"\n");
	}

	public static String opToString(int op)
	{
		switch(op)
		{
		case Query.EQ:
			return "=";
		case Query.GT:
			return ">";
		case Query.GTE:
			return ">=";
		case Query.LT:
			return "<";
		case Query.LTE:
			return "<=";
		case Query.STARTSWITH:
			return "STARTSWITH";
		default:
				return "Unknown Op";
		}
	
	
	}
	 
}
