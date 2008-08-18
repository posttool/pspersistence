package com.pagesociety.bdb.index.query.pssql;

import java.util.List;

import com.pagesociety.bdb.index.query.pssql.WhereClause;

class SelectStatement extends PSSqlStatement
{
	public String return_type;
	public List<String> select_list;
	public WhereClause where_clause;

	public SelectStatement(List<String> select_list,String return_type,WhereClause where_clause)
	{
		this.return_type 	= return_type;
		this.select_list 	= select_list;
		this.where_clause	= where_clause;
	}
	
	public int getType(){return PSSQL_STATEMENT_TYPE_SELECT;}

}
