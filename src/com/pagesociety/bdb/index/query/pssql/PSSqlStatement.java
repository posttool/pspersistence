package com.pagesociety.bdb.index.query.pssql;

public abstract class PSSqlStatement {

	protected static final int PSSQL_STATEMENT_TYPE_SELECT	= 0x01;
	protected static final int PSSQL_STATEMENT_TYPE_UPDATE	= 0x02;
	protected static final int PSSQL_STATEMENT_TYPE_INSERT	= 0x03;
	
	public abstract int getType();

}