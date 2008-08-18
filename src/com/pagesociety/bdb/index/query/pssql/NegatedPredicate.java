package com.pagesociety.bdb.index.query.pssql;

import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;


public class NegatedPredicate extends PredicateExpr
{
	public PredicateExpr p;
	
	public NegatedPredicate(PredicateExpr p)
	{
		this.p  = p;
	}

	public boolean eval(EntityDefinition def,Entity candidate) throws PSSqlException
	{
		return !p.eval(def,candidate);
	}

	
	public void toBuf(int indent,StringBuilder buf)
	{
		indent(indent,buf);
		buf.append("NOT \n");
		p.toBuf(indent, buf);	
	}
}
