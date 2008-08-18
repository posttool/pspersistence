package com.pagesociety.bdb.index.query.pssql;

import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;



class WhereClause
{
	PredicateExpr root;
	
	WhereClause(PredicateExpr root)
	{
		this.root = root;
	}
	
	public boolean eval(EntityDefinition def,Entity candidate) throws PSSqlException
	{
		return root.eval(def,candidate);
	}
	
	/* this stuff is just for generating string view of structure of where predicate tree */
	public String toString()
	{
		StringBuilder buf = new StringBuilder();		
		buf.append("WHERE\n");
		root.toBuf(0, buf);
		return buf.toString();
	}
	
	
}