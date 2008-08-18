package com.pagesociety.bdb.index.query.pssql;

import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;


public abstract class PredicateExpr
{
	public abstract boolean eval(EntityDefinition def, Entity candidate) throws PSSqlException;


	public abstract void toBuf(int indent,StringBuilder buf);
	public static void indent(int level,StringBuilder buf)
	{
		for(int i = 0;i < level;i++)
			buf.append(" ");
	}
	
	
}
