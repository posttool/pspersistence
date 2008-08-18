package com.pagesociety.bdb.index.query.pssql;

import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

import com.pagesociety.bdb.BDBQueryResult;
import com.pagesociety.bdb.index.query.QueryExecutionEnvironment;
import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.pagesociety.persistence.Query;
import com.pagesociety.persistence.QueryResult;

public class PSSqlExecutor 
{
	private QueryExecutionEnvironment _env;
	
	public PSSqlExecutor(QueryExecutionEnvironment _query_exec_env) 
	{
		_env = _query_exec_env;
	
	}

	public QueryResult execute(String pssql) throws PersistenceException
	{
	 	PSSqlParser parser=null;
    	PSSqlLexer lex=null;
    	try{
    		lex 						= new PSSqlLexer(new ANTLRStringStream(pssql));
    		CommonTokenStream tokens 	= new CommonTokenStream(lex);
    		parser 						= new PSSqlParser(tokens);
    		List<PSSqlStatement> ss 	= parser.prog(); // launch parsing
    		System.out.println("SS IS "+ss);
    		for(int i = 0;i < ss.size();i++)
    		{
    			PSSqlStatement s = ss.get(i);
    			switch(s.getType())
    			{
    				case PSSqlStatement.PSSQL_STATEMENT_TYPE_SELECT:
    					return do_select((SelectStatement)s);
    				default:
    						throw PSSqlException.EXEC_EXCEPTION("UNKNOWN TYPE OF PSSQL STATEMENT type:"+s.getType(),pssql);
    			}
    		}
    	}catch(RecognitionException e)
    	{
    		throw PSSqlException.SYNTAX_EXCEPTION("SYNTAX ERROR:",e.line,e.charPositionInLine,pssql);
    	}
	
    	return BDBQueryResult.EMPTY_RESULT;
	}


	private QueryResult do_select(SelectStatement s) throws PSSqlException
	{
		System.out.println("SELECT STATEMENT");
		System.out.println("SELECT LIST IS "+s.select_list);
		System.out.println("RETURN TYPE "+s.return_type);
		if(s.where_clause != null)
			System.out.println(s.where_clause);

		EntityDefinition return_type_def = null;
		try{
			return_type_def = _env.getPrimaryIndex(s.return_type).getEntityDefinition();
		}catch(PersistenceException pe)
		{
			PSSqlException.EXEC_EXCEPTION("UNKNOWN ENTITY "+s.return_type,"");
		}
		validate_fields(return_type_def, s.select_list);
		
		Query q = new Query(s.return_type);
		q.idx(Query.PRIMARY_IDX);
		q.eq(Query.VAL_GLOB);
		q.cacheResults(false);
		try{
			BDBQueryResult result 			= new BDBQueryResult(); 
			QueryResult  candidate_result 	= _env.getQueryManager().executeQuery(q);
			List<Entity> candidates 		= candidate_result.getEntities();
			
			int cs = candidates.size();
			for(int i = 0;i < cs;i++)
			{
				Entity candidate = candidates.get(i);
				if(s.where_clause != null)
				{
					if(s.where_clause.eval(return_type_def, candidate))
						result.add(candidate);
				}
				else
					result.add(candidate);
			}
			
			return result;
		}catch(PersistenceException pe)
		{
			pe.printStackTrace();
			throw PSSqlException.EXEC_EXCEPTION(pe.getMessage(), "");
		}
    	
	}

	private void validate_fields(EntityDefinition def,List<String> fields) throws PSSqlException
	{
		int s = fields.size();
		for(int i = 0;i < s;i++)
		{
			if(def.getField(fields.get(i)) == null)
				throw PSSqlException.EXEC_EXCEPTION("UNKNOWN FIELD "+fields.get(i)+" FOR ENTITY "+def.getName(),"");
		}
		
	}
	
}
