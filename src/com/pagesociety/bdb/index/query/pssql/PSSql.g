grammar PSSql;

@header {

package com.pagesociety.bdb.index.query.pssql;
import com.pagesociety.bdb.index.query.*;
import com.pagesociety.persistence.*;
import java.util.ArrayList;
import java.util.List;
}

@lexer::header {package com.pagesociety.bdb.index.query.pssql;}

@members {



/*
public void emitErrorMessage(String error)
{
  System.out.println("EMITTING error "+error);
}

*/

}//end members


@rulecatch{
  catch(RecognitionException re)
  {
    throw re;
  }
}

prog returns[List<PSSqlStatement> values]:   
	{$values = new ArrayList<PSSqlStatement>();}
	(s=stat';'{$values.add($s.value);})+ EOF
 	;
                
stat returns[PSSqlStatement value]
	:s=selectStatement{$value = $s.value;}
    	;

selectStatement returns[SelectStatement value]
	:
	SELECT s=selectList FROM ID (w=whereClause)?{$value = new SelectStatement($s.values,$ID.text,$w.value);}	
	;
				
selectList returns[List<String> values]
@init { $values = new ArrayList<String>();}:  
	STAR 
	|(v=ID{$values.add($v.text);}(',' v=ID{$values.add($v.text);})*)*
	;		
	
whereClause returns[WhereClause value]
	:WHERE p=orPredicate{$value = new WhereClause($p.pred);} 
	;
	
/*this is how we express operator precendece. 
AND will bind more tightly than OR if they are not bracketed*/
orPredicate returns[PredicateExpr pred]
	:p1=andPredicate{$pred=$p1.pred;} (OR p2=andPredicate{$pred = new UnionPredicateExpr($pred,$p2.pred);})*
	;

andPredicate returns[PredicateExpr pred]
	:p1=negatablePredicate{$pred=$p1.pred;}(AND p2=negatablePredicate{$pred = new IntersectionPredicateExpr($pred,$p2.pred);})*	
	;
	
negatablePredicate returns[PredicateExpr pred]
@init { boolean negated = false; } 	
	: (NOT{negated=true;})? p=terminalPredicate{if(negated)$pred= new NegatedPredicate($p.pred);else $pred=$p.pred;}
	;
	
terminalPredicate returns[PredicateExpr pred]	
	:p=simplePredicate{$pred=$p.pred;}
	|setPredicate
	|rangePredicate
	|'(' p=orPredicate{$pred=$p.pred; }')'
	;

simplePredicate returns [PredicateExpr pred]
	:ID op=s_predicate_op v=value{$pred = new SimplePredicateExpr($op.value,$ID.text,$v.value);}
	;
	
rangePredicate returns [PredicateExpr pred]
	:ID BETWEEN sv=value AND rv=value{$pred = new RangePredicateExpr(Query.BETWEEN_INCLUSIVE_ASC,$ID.text,$sv.value,$rv.value);} 
	;

setPredicate
	:ID CONTAINS (ANY|ALL)? set_value 
	;				

s_predicate_op returns[int value]
	:EQ 	 {$value = Query.EQ;}
	|GT 	 {$value = Query.GT;}
	|GTE	 {$value = Query.GTE;}
	|LT 	 {$value = Query.LT;} 
	|LTE	 {$value = Query.LTE;}
	|NOTEQ {$value = Query.EQ;System.err.println("NOT EQUAL AS AN OP IS NOT SUPPORTED YET>NEED TO MODIFY QUERY AND MAKE ANOTHER ITERATOR POTENTIALLY.");}
	|STARTSWITH{$value = Query.STARTSWITH;}
	;

value returns[Object value]
	:INT{$value= new Integer($INT.text);}
	|SINGLE_QUOTED_STRING{$value=$SINGLE_QUOTED_STRING.text;}
	|DOUBLE_QUOTED_STRING{$value=$DOUBLE_QUOTED_STRING.text;}
	|DOUBLE{$value=new Float($DOUBLE.text);}
	|NULL{$value = null;}
	|ID':'INT	{$value=new EntityValue($ID.text,Long.parseLong($INT.text));}
	;

set_value returns[List<Object> values]
@init { $values = new ArrayList<Object>();}:
  '[' (v1=value{$values.add($v1.value);}(',' v2=value{$values.add($v2.value);})*)* ']'
  ;
					

/* lexer tokens */
/* keywords */



SELECT: ('S'|'s')('E'|'e')('L'|'l')('E'|'e')('C'|'c')('T'|'t');
FROM:	('F'|'f')('R'|'r')('O'|'o')('M'|'m');
WHERE:	('W'|'w')('H'|'h')('E'|'e')('R'|'r')('E'|'e');	
CONTAINS:('C'|'c')('O'|'o')('N'|'n')('T'|'t')('A'|'a')('I'|'i')('N'|'n')('S'|'s');
ANY:	('A'|'a')('N'|'n')('Y'|'y');
ALL:	('A'|'a')('L'|'l')('L'|'l');
AND:	('A'|'a')('N'|'n')('D'|'d');
OR:	('O'|'o')('R'|'r'); 	
BETWEEN:('B'|'b')('E'|'e')('T'|'t')('W'|'w')('E'|'e')('E'|'e')('N'|'n');		
STARTSWITH: ('S'|'s')('T'|'t')('A'|'a')('R'|'r')('T'|'t')('S'|'s')('W'|'w')('I'|'i')('T'|'t')('H'|'h');
NOT:	('N'|'n')('O'|'o')('T'|'t');	
NULL: ('N'|'n')('U'|'u')('L'|'l')('L'|'l'); 
ID  :   ('a'..'z'|'A'..'Z'|'_')('a'..'z'|'A'..'Z'|'0'..'9'|'_')*;
 

INT :   ('0'..'9')+ ;
DOUBLE: ('0'..'9')* '\.' ('0'..'9')*;	
SINGLE_QUOTED_STRING: '\'' .* '\'' {setText(getText().substring(1, getText().length()-1));} ;
DOUBLE_QUOTED_STRING:  '"' .* '"'  {setText(getText().substring(1, getText().length()-1));} ;
		
WS  :   (   ' '
        |   '\t'
        |   '\r'
        |   '\n'
        )+
        { $channel=HIDDEN; }
    ;  

/*special*/
STAR:	'*';

	
/*operators*/
EQ:	'=';
GT:	'>';
GTE:	'>=';
LT:	'<';	
LTE:	'<=';
NOTEQ:	'!=';
	
			
