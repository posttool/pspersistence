package com.pagesociety.bdb.index.query.pssql;

import com.pagesociety.persistence.PersistenceException;

@SuppressWarnings("serial")
public class PSSqlException extends PersistenceException
{
	
	private static final int TYPE_SYNTAX_ERROR = 0x01;
	private static final int TYPE_EXEC_ERROR   = 0x02;
	
	
	public int type;
	public String original_input;
	public int line_no;
	public int offset;
	
	public static PSSqlException SYNTAX_EXCEPTION(String msg,int line_no,int offset,String original_input)
	{
		return new PSSqlException(TYPE_SYNTAX_ERROR,msg,line_no,offset,original_input);
	}
	
	public static PSSqlException EXEC_EXCEPTION(String msg,String original_input)
	{
		return new PSSqlException(TYPE_EXEC_ERROR,msg,0,0,original_input);
	}

	private PSSqlException(int type,String msg,int line_no,int offset,String original_input)
	{
		super(msg);
		this.type = type;
		this.original_input = original_input;
		this.line_no 	= line_no;
		this.offset 	= offset;
	}
}
