package com.pagesociety.bdb.index.query.pssql;

import java.util.List;

import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.Query;


public class SetPredicateExpr extends PredicateExpr
{
	private int op;
	private String field;
	private List<Object> values;
	
	SetPredicateExpr(int op,String field,List<Object> values)
	{
		this.op = op;
		this.field = field;
		this.values = values;
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
			case Query.SET_CONTAINS_ANY:
				s_op = "CONTAINS ANY";
				break;
			case Query.SET_CONTAINS_ALL:
				s_op = "CONTAINS ALL";
			default:
				s_op = "Unknown";
		}
	
		buf.append(field+" "+s_op+" "+values.toString());
	}

}
