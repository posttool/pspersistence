package com.pagesociety.bdb.index.query.pssql;

import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;


public class IntersectionPredicateExpr extends PredicateExpr
{
	private PredicateExpr term1;
	private PredicateExpr term2;
	
	IntersectionPredicateExpr(PredicateExpr term1,PredicateExpr term2)
	{
		this.term1 = term1;
		this.term2 = term2;
	}
	
	public boolean eval(EntityDefinition def,Entity candidate) throws PSSqlException
	{
		return (term1.eval(def,candidate) && term2.eval(def,candidate));
	}

	public void toBuf(int indent,StringBuilder buf)
	{
		indent(indent,buf);
		buf.append("AND\n");
		term1.toBuf(indent+1,buf);
		term2.toBuf(indent+1,buf);
	}

}
