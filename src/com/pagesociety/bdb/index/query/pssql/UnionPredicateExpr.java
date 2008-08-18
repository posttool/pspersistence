package com.pagesociety.bdb.index.query.pssql;

import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;


public class UnionPredicateExpr extends PredicateExpr
{
	private PredicateExpr term1;
	private PredicateExpr term2;
	
	public UnionPredicateExpr(PredicateExpr term1,PredicateExpr term2)
	{
		this.term1 = term1;
		this.term2 = term2;
	}
	
	public boolean eval(EntityDefinition def,Entity entity) throws PSSqlException
	{
		return (term1.eval(def,entity) || term2.eval(def,entity));
	}
	
	
	public void toBuf(int indent,StringBuilder buf)
	{
		indent(indent,buf);
		buf.append("OR\n");
		term1.toBuf(indent+1,buf);
		term2.toBuf(indent+1,buf);
		
	}

}
