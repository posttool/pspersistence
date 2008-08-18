package com.pagesociety.bdb.index.query.pssql;

import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.Query;


public class RangePredicateExpr extends PredicateExpr
{
	private int op;
	private String field;
	private Object start_value;
	private Object range_value;
	
	RangePredicateExpr(int op,String field,Object start_value,Object range_value)
	{
		this.op = op;
		this.field = field;
		this.start_value = start_value;
		this.range_value = range_value;
	}
	
	public boolean eval(EntityDefinition def,Entity candidate) throws PSSqlException
	{
		return true;
	}

	public void toBuf(int indent,StringBuilder buf)
	{
		indent(indent,buf);
		String s_op = null;
		switch(op)
		{
			case Query.BETWEEN_INCLUSIVE_ASC:
				s_op = "BETWEEN";
				break;
		}
	
		buf.append(field+" "+s_op+" "+start_value.toString()+","+range_value.toString());
	}

}
