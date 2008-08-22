// $ANTLR 3.1b1 C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g 2008-08-01 19:43:47


package com.pagesociety.bdb.index.query.pssql;
import com.pagesociety.bdb.index.query.*;
import com.pagesociety.persistence.*;
import java.util.ArrayList;
import java.util.List;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class PSSqlParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SELECT", "FROM", "ID", "STAR", "WHERE", "OR", "AND", "NOT", "BETWEEN", "CONTAINS", "ANY", "ALL", "EQ", "GT", "GTE", "LT", "LTE", "NOTEQ", "STARTSWITH", "INT", "SINGLE_QUOTED_STRING", "DOUBLE_QUOTED_STRING", "DOUBLE", "NULL", "WS", "';'", "','", "'('", "')'", "':'", "'['", "']'"
    };
    public static final int DOUBLE_QUOTED_STRING=25;
    public static final int WHERE=8;
    public static final int LT=19;
    public static final int T__29=29;
    public static final int STAR=7;
    public static final int SINGLE_QUOTED_STRING=24;
    public static final int NULL=27;
    public static final int CONTAINS=13;
    public static final int GTE=18;
    public static final int INT=23;
    public static final int NOT=11;
    public static final int ID=6;
    public static final int AND=10;
    public static final int EOF=-1;
    public static final int LTE=20;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int NOTEQ=21;
    public static final int T__32=32;
    public static final int WS=28;
    public static final int ANY=14;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int ALL=15;
    public static final int STARTSWITH=22;
    public static final int OR=9;
    public static final int DOUBLE=26;
    public static final int GT=17;
    public static final int EQ=16;
    public static final int FROM=5;
    public static final int SELECT=4;
    public static final int BETWEEN=12;

    // delegates
    // delegators


        public PSSqlParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PSSqlParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
        }
        

    public String[] getTokenNames() { return PSSqlParser.tokenNames; }
    public String getGrammarFileName() { return "C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g"; }





    /*
    public void emitErrorMessage(String error)
    {
      System.out.println("EMITTING error "+error);
    }

    */




    // $ANTLR start prog
    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:36:1: prog returns [List<PSSqlStatement> values] : (s= stat ';' )+ EOF ;
    public final List<PSSqlStatement> prog() throws RecognitionException {
        List<PSSqlStatement> values = null;

        PSSqlStatement s = null;


        try {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:36:42: ( (s= stat ';' )+ EOF )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:37:2: (s= stat ';' )+ EOF
            {
            values = new ArrayList<PSSqlStatement>();
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:38:2: (s= stat ';' )+
            int cnt1=0;
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==SELECT) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:38:3: s= stat ';'
            	    {
            	    pushFollow(FOLLOW_stat_in_prog49);
            	    s=stat();

            	    state._fsp--;

            	    match(input,29,FOLLOW_29_in_prog50); 
            	    values.add(s);

            	    }
            	    break;

            	default :
            	    if ( cnt1 >= 1 ) break loop1;
                        EarlyExitException eee =
                            new EarlyExitException(1, input);
                        throw eee;
                }
                cnt1++;
            } while (true);

            match(input,EOF,FOLLOW_EOF_in_prog55); 

            }

        }

          catch(RecognitionException re)
          {
            throw re;
          }
        finally {
        }
        return values;
    }
    // $ANTLR end prog


    // $ANTLR start stat
    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:41:1: stat returns [PSSqlStatement value] : s= selectStatement ;
    public final PSSqlStatement stat() throws RecognitionException {
        PSSqlStatement value = null;

        SelectStatement s = null;


        try {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:42:2: (s= selectStatement )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:42:3: s= selectStatement
            {
            pushFollow(FOLLOW_selectStatement_in_stat87);
            s=selectStatement();

            state._fsp--;

            value = s;

            }

        }

          catch(RecognitionException re)
          {
            throw re;
          }
        finally {
        }
        return value;
    }
    // $ANTLR end stat


    // $ANTLR start selectStatement
    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:45:1: selectStatement returns [SelectStatement value] : SELECT s= selectList FROM ID (w= whereClause )? ;
    public final SelectStatement selectStatement() throws RecognitionException {
        SelectStatement value = null;

        Token ID1=null;
        List<String> s = null;

        WhereClause w = null;


        try {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:46:2: ( SELECT s= selectList FROM ID (w= whereClause )? )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:47:2: SELECT s= selectList FROM ID (w= whereClause )?
            {
            match(input,SELECT,FOLLOW_SELECT_in_selectStatement107); 
            pushFollow(FOLLOW_selectList_in_selectStatement111);
            s=selectList();

            state._fsp--;

            match(input,FROM,FOLLOW_FROM_in_selectStatement113); 
            ID1=(Token)match(input,ID,FOLLOW_ID_in_selectStatement115); 
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:47:30: (w= whereClause )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==WHERE) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:47:31: w= whereClause
                    {
                    pushFollow(FOLLOW_whereClause_in_selectStatement120);
                    w=whereClause();

                    state._fsp--;


                    }
                    break;

            }

            value = new SelectStatement(s,(ID1!=null?ID1.getText():null),w);

            }

        }

          catch(RecognitionException re)
          {
            throw re;
          }
        finally {
        }
        return value;
    }
    // $ANTLR end selectStatement


    // $ANTLR start selectList
    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:50:1: selectList returns [List<String> values] : ( STAR | (v= ID ( ',' v= ID )* )* );
    public final List<String> selectList() throws RecognitionException {
        List<String> values = null;

        Token v=null;

         values = new ArrayList<String>();
        try {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:51:44: ( STAR | (v= ID ( ',' v= ID )* )* )
            int alt5=2;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==STAR) ) {
                alt5=1;
            }
            else if ( ((LA5_0>=FROM && LA5_0<=ID)) ) {
                alt5=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:52:2: STAR
                    {
                    match(input,STAR,FOLLOW_STAR_in_selectList148); 

                    }
                    break;
                case 2 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:53:3: (v= ID ( ',' v= ID )* )*
                    {
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:53:3: (v= ID ( ',' v= ID )* )*
                    loop4:
                    do {
                        int alt4=2;
                        int LA4_0 = input.LA(1);

                        if ( (LA4_0==ID) ) {
                            alt4=1;
                        }


                        switch (alt4) {
                    	case 1 :
                    	    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:53:4: v= ID ( ',' v= ID )*
                    	    {
                    	    v=(Token)match(input,ID,FOLLOW_ID_in_selectList156); 
                    	    values.add((v!=null?v.getText():null));
                    	    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:53:31: ( ',' v= ID )*
                    	    loop3:
                    	    do {
                    	        int alt3=2;
                    	        int LA3_0 = input.LA(1);

                    	        if ( (LA3_0==30) ) {
                    	            alt3=1;
                    	        }


                    	        switch (alt3) {
                    	    	case 1 :
                    	    	    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:53:32: ',' v= ID
                    	    	    {
                    	    	    match(input,30,FOLLOW_30_in_selectList159); 
                    	    	    v=(Token)match(input,ID,FOLLOW_ID_in_selectList163); 
                    	    	    values.add((v!=null?v.getText():null));

                    	    	    }
                    	    	    break;

                    	    	default :
                    	    	    break loop3;
                    	        }
                    	    } while (true);


                    	    }
                    	    break;

                    	default :
                    	    break loop4;
                        }
                    } while (true);


                    }
                    break;

            }
        }

          catch(RecognitionException re)
          {
            throw re;
          }
        finally {
        }
        return values;
    }
    // $ANTLR end selectList


    // $ANTLR start whereClause
    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:56:1: whereClause returns [WhereClause value] : WHERE p= orPredicate ;
    public final WhereClause whereClause() throws RecognitionException {
        WhereClause value = null;

        PredicateExpr p = null;


        try {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:57:2: ( WHERE p= orPredicate )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:57:3: WHERE p= orPredicate
            {
            match(input,WHERE,FOLLOW_WHERE_in_whereClause184); 
            pushFollow(FOLLOW_orPredicate_in_whereClause188);
            p=orPredicate();

            state._fsp--;

            value = new WhereClause(p);

            }

        }

          catch(RecognitionException re)
          {
            throw re;
          }
        finally {
        }
        return value;
    }
    // $ANTLR end whereClause


    // $ANTLR start orPredicate
    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:62:1: orPredicate returns [PredicateExpr pred] : p1= andPredicate ( OR p2= andPredicate )* ;
    public final PredicateExpr orPredicate() throws RecognitionException {
        PredicateExpr pred = null;

        PredicateExpr p1 = null;

        PredicateExpr p2 = null;


        try {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:63:2: (p1= andPredicate ( OR p2= andPredicate )* )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:63:3: p1= andPredicate ( OR p2= andPredicate )*
            {
            pushFollow(FOLLOW_andPredicate_in_orPredicate208);
            p1=andPredicate();

            state._fsp--;

            pred =p1;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:63:36: ( OR p2= andPredicate )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0==OR) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:63:37: OR p2= andPredicate
            	    {
            	    match(input,OR,FOLLOW_OR_in_orPredicate212); 
            	    pushFollow(FOLLOW_andPredicate_in_orPredicate216);
            	    p2=andPredicate();

            	    state._fsp--;

            	    pred = new UnionPredicateExpr(pred,p2);

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);


            }

        }

          catch(RecognitionException re)
          {
            throw re;
          }
        finally {
        }
        return pred;
    }
    // $ANTLR end orPredicate


    // $ANTLR start andPredicate
    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:66:1: andPredicate returns [PredicateExpr pred] : p1= negatablePredicate ( AND p2= negatablePredicate )* ;
    public final PredicateExpr andPredicate() throws RecognitionException {
        PredicateExpr pred = null;

        PredicateExpr p1 = null;

        PredicateExpr p2 = null;


        try {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:67:2: (p1= negatablePredicate ( AND p2= negatablePredicate )* )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:67:3: p1= negatablePredicate ( AND p2= negatablePredicate )*
            {
            pushFollow(FOLLOW_negatablePredicate_in_andPredicate234);
            p1=negatablePredicate();

            state._fsp--;

            pred =p1;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:67:41: ( AND p2= negatablePredicate )*
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( (LA7_0==AND) ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:67:42: AND p2= negatablePredicate
            	    {
            	    match(input,AND,FOLLOW_AND_in_andPredicate237); 
            	    pushFollow(FOLLOW_negatablePredicate_in_andPredicate241);
            	    p2=negatablePredicate();

            	    state._fsp--;

            	    pred = new IntersectionPredicateExpr(pred,p2);

            	    }
            	    break;

            	default :
            	    break loop7;
                }
            } while (true);


            }

        }

          catch(RecognitionException re)
          {
            throw re;
          }
        finally {
        }
        return pred;
    }
    // $ANTLR end andPredicate


    // $ANTLR start negatablePredicate
    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:70:1: negatablePredicate returns [PredicateExpr pred] : ( NOT )? p= terminalPredicate ;
    public final PredicateExpr negatablePredicate() throws RecognitionException {
        PredicateExpr pred = null;

        PredicateExpr p = null;


         boolean negated = false; 
        try {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:72:2: ( ( NOT )? p= terminalPredicate )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:72:4: ( NOT )? p= terminalPredicate
            {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:72:4: ( NOT )?
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==NOT) ) {
                alt8=1;
            }
            switch (alt8) {
                case 1 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:72:5: NOT
                    {
                    match(input,NOT,FOLLOW_NOT_in_negatablePredicate268); 
                    negated=true;

                    }
                    break;

            }

            pushFollow(FOLLOW_terminalPredicate_in_negatablePredicate275);
            p=terminalPredicate();

            state._fsp--;

            if(negated)pred = new NegatedPredicate(p);else pred =p;

            }

        }

          catch(RecognitionException re)
          {
            throw re;
          }
        finally {
        }
        return pred;
    }
    // $ANTLR end negatablePredicate


    // $ANTLR start terminalPredicate
    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:75:1: terminalPredicate returns [PredicateExpr pred] : (p= simplePredicate | setPredicate | rangePredicate | '(' p= orPredicate ')' );
    public final PredicateExpr terminalPredicate() throws RecognitionException {
        PredicateExpr pred = null;

        PredicateExpr p = null;


        try {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:76:2: (p= simplePredicate | setPredicate | rangePredicate | '(' p= orPredicate ')' )
            int alt9=4;
            int LA9_0 = input.LA(1);

            if ( (LA9_0==ID) ) {
                switch ( input.LA(2) ) {
                case CONTAINS:
                    {
                    alt9=2;
                    }
                    break;
                case BETWEEN:
                    {
                    alt9=3;
                    }
                    break;
                case EQ:
                case GT:
                case GTE:
                case LT:
                case LTE:
                case NOTEQ:
                case STARTSWITH:
                    {
                    alt9=1;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 1, input);

                    throw nvae;
                }

            }
            else if ( (LA9_0==31) ) {
                alt9=4;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;
            }
            switch (alt9) {
                case 1 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:76:3: p= simplePredicate
                    {
                    pushFollow(FOLLOW_simplePredicate_in_terminalPredicate293);
                    p=simplePredicate();

                    state._fsp--;

                    pred =p;

                    }
                    break;
                case 2 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:77:3: setPredicate
                    {
                    pushFollow(FOLLOW_setPredicate_in_terminalPredicate298);
                    setPredicate();

                    state._fsp--;


                    }
                    break;
                case 3 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:78:3: rangePredicate
                    {
                    pushFollow(FOLLOW_rangePredicate_in_terminalPredicate302);
                    rangePredicate();

                    state._fsp--;


                    }
                    break;
                case 4 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:79:3: '(' p= orPredicate ')'
                    {
                    match(input,31,FOLLOW_31_in_terminalPredicate306); 
                    pushFollow(FOLLOW_orPredicate_in_terminalPredicate310);
                    p=orPredicate();

                    state._fsp--;

                    pred =p; 
                    match(input,32,FOLLOW_32_in_terminalPredicate312); 

                    }
                    break;

            }
        }

          catch(RecognitionException re)
          {
            throw re;
          }
        finally {
        }
        return pred;
    }
    // $ANTLR end terminalPredicate


    // $ANTLR start simplePredicate
    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:82:1: simplePredicate returns [PredicateExpr pred] : ID op= s_predicate_op v= value ;
    public final PredicateExpr simplePredicate() throws RecognitionException {
        PredicateExpr pred = null;

        Token ID2=null;
        int op = 0;

        Object v = null;


        try {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:83:2: ( ID op= s_predicate_op v= value )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:83:3: ID op= s_predicate_op v= value
            {
            ID2=(Token)match(input,ID,FOLLOW_ID_in_simplePredicate326); 
            pushFollow(FOLLOW_s_predicate_op_in_simplePredicate330);
            op=s_predicate_op();

            state._fsp--;

            pushFollow(FOLLOW_value_in_simplePredicate334);
            v=value();

            state._fsp--;

            pred = new SimplePredicateExpr(op,(ID2!=null?ID2.getText():null),v);

            }

        }

          catch(RecognitionException re)
          {
            throw re;
          }
        finally {
        }
        return pred;
    }
    // $ANTLR end simplePredicate


    // $ANTLR start rangePredicate
    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:86:1: rangePredicate returns [PredicateExpr pred] : ID BETWEEN sv= value AND rv= value ;
    public final PredicateExpr rangePredicate() throws RecognitionException {
        PredicateExpr pred = null;

        Token ID3=null;
        Object sv = null;

        Object rv = null;


        try {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:87:2: ( ID BETWEEN sv= value AND rv= value )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:87:3: ID BETWEEN sv= value AND rv= value
            {
            ID3=(Token)match(input,ID,FOLLOW_ID_in_rangePredicate350); 
            match(input,BETWEEN,FOLLOW_BETWEEN_in_rangePredicate352); 
            pushFollow(FOLLOW_value_in_rangePredicate356);
            sv=value();

            state._fsp--;

            match(input,AND,FOLLOW_AND_in_rangePredicate358); 
            pushFollow(FOLLOW_value_in_rangePredicate362);
            rv=value();

            state._fsp--;

            pred = new RangePredicateExpr(Query.BETWEEN_INCLUSIVE_ASC,(ID3!=null?ID3.getText():null),sv,rv);

            }

        }

          catch(RecognitionException re)
          {
            throw re;
          }
        finally {
        }
        return pred;
    }
    // $ANTLR end rangePredicate


    // $ANTLR start setPredicate
    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:90:1: setPredicate : ID CONTAINS ( ANY | ALL )? set_value ;
    public final void setPredicate() throws RecognitionException {
        try {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:91:2: ( ID CONTAINS ( ANY | ALL )? set_value )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:91:3: ID CONTAINS ( ANY | ALL )? set_value
            {
            match(input,ID,FOLLOW_ID_in_setPredicate374); 
            match(input,CONTAINS,FOLLOW_CONTAINS_in_setPredicate376); 
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:91:15: ( ANY | ALL )?
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( ((LA10_0>=ANY && LA10_0<=ALL)) ) {
                alt10=1;
            }
            switch (alt10) {
                case 1 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:
                    {
                    if ( (input.LA(1)>=ANY && input.LA(1)<=ALL) ) {
                        input.consume();
                        state.errorRecovery=false;
                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        throw mse;
                    }


                    }
                    break;

            }

            pushFollow(FOLLOW_set_value_in_setPredicate385);
            set_value();

            state._fsp--;


            }

        }

          catch(RecognitionException re)
          {
            throw re;
          }
        finally {
        }
        return ;
    }
    // $ANTLR end setPredicate


    // $ANTLR start s_predicate_op
    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:94:1: s_predicate_op returns [int value] : ( EQ | GT | GTE | LT | LTE | NOTEQ | STARTSWITH );
    public final int s_predicate_op() throws RecognitionException {
        int value = 0;

        try {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:95:2: ( EQ | GT | GTE | LT | LTE | NOTEQ | STARTSWITH )
            int alt11=7;
            switch ( input.LA(1) ) {
            case EQ:
                {
                alt11=1;
                }
                break;
            case GT:
                {
                alt11=2;
                }
                break;
            case GTE:
                {
                alt11=3;
                }
                break;
            case LT:
                {
                alt11=4;
                }
                break;
            case LTE:
                {
                alt11=5;
                }
                break;
            case NOTEQ:
                {
                alt11=6;
                }
                break;
            case STARTSWITH:
                {
                alt11=7;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }

            switch (alt11) {
                case 1 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:95:3: EQ
                    {
                    match(input,EQ,FOLLOW_EQ_in_s_predicate_op403); 
                    value = Query.EQ;

                    }
                    break;
                case 2 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:96:3: GT
                    {
                    match(input,GT,FOLLOW_GT_in_s_predicate_op411); 
                    value = Query.GT;

                    }
                    break;
                case 3 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:97:3: GTE
                    {
                    match(input,GTE,FOLLOW_GTE_in_s_predicate_op419); 
                    value = Query.GTE;

                    }
                    break;
                case 4 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:98:3: LT
                    {
                    match(input,LT,FOLLOW_LT_in_s_predicate_op426); 
                    value = Query.LT;

                    }
                    break;
                case 5 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:99:3: LTE
                    {
                    match(input,LTE,FOLLOW_LTE_in_s_predicate_op435); 
                    value = Query.LTE;

                    }
                    break;
                case 6 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:100:3: NOTEQ
                    {
                    match(input,NOTEQ,FOLLOW_NOTEQ_in_s_predicate_op442); 
                    value = Query.EQ;System.err.println("NOT EQUAL AS AN OP IS NOT SUPPORTED YET>NEED TO MODIFY QUERY AND MAKE ANOTHER ITERATOR POTENTIALLY.");

                    }
                    break;
                case 7 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:101:3: STARTSWITH
                    {
                    match(input,STARTSWITH,FOLLOW_STARTSWITH_in_s_predicate_op448); 
                    value = Query.STARTSWITH;

                    }
                    break;

            }
        }

          catch(RecognitionException re)
          {
            throw re;
          }
        finally {
        }
        return value;
    }
    // $ANTLR end s_predicate_op


    // $ANTLR start value
    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:104:1: value returns [Object value] : ( INT | SINGLE_QUOTED_STRING | DOUBLE_QUOTED_STRING | DOUBLE | NULL | ID ':' INT );
    public final Object value() throws RecognitionException {
        Object value = null;

        Token INT4=null;
        Token SINGLE_QUOTED_STRING5=null;
        Token DOUBLE_QUOTED_STRING6=null;
        Token DOUBLE7=null;
        Token ID8=null;
        Token INT9=null;

        try {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:105:2: ( INT | SINGLE_QUOTED_STRING | DOUBLE_QUOTED_STRING | DOUBLE | NULL | ID ':' INT )
            int alt12=6;
            switch ( input.LA(1) ) {
            case INT:
                {
                alt12=1;
                }
                break;
            case SINGLE_QUOTED_STRING:
                {
                alt12=2;
                }
                break;
            case DOUBLE_QUOTED_STRING:
                {
                alt12=3;
                }
                break;
            case DOUBLE:
                {
                alt12=4;
                }
                break;
            case NULL:
                {
                alt12=5;
                }
                break;
            case ID:
                {
                alt12=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;
            }

            switch (alt12) {
                case 1 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:105:3: INT
                    {
                    INT4=(Token)match(input,INT,FOLLOW_INT_in_value462); 
                    value = new Integer((INT4!=null?INT4.getText():null));

                    }
                    break;
                case 2 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:106:3: SINGLE_QUOTED_STRING
                    {
                    SINGLE_QUOTED_STRING5=(Token)match(input,SINGLE_QUOTED_STRING,FOLLOW_SINGLE_QUOTED_STRING_in_value467); 
                    value =(SINGLE_QUOTED_STRING5!=null?SINGLE_QUOTED_STRING5.getText():null);

                    }
                    break;
                case 3 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:107:3: DOUBLE_QUOTED_STRING
                    {
                    DOUBLE_QUOTED_STRING6=(Token)match(input,DOUBLE_QUOTED_STRING,FOLLOW_DOUBLE_QUOTED_STRING_in_value472); 
                    value =(DOUBLE_QUOTED_STRING6!=null?DOUBLE_QUOTED_STRING6.getText():null);

                    }
                    break;
                case 4 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:108:3: DOUBLE
                    {
                    DOUBLE7=(Token)match(input,DOUBLE,FOLLOW_DOUBLE_in_value477); 
                    value =new Float((DOUBLE7!=null?DOUBLE7.getText():null));

                    }
                    break;
                case 5 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:109:3: NULL
                    {
                    match(input,NULL,FOLLOW_NULL_in_value482); 
                    value = null;

                    }
                    break;
                case 6 :
                    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:110:3: ID ':' INT
                    {
                    ID8=(Token)match(input,ID,FOLLOW_ID_in_value487); 
                    match(input,33,FOLLOW_33_in_value488); 
                    INT9=(Token)match(input,INT,FOLLOW_INT_in_value489); 
                    value =new EntityValue((ID8!=null?ID8.getText():null),Long.parseLong((INT9!=null?INT9.getText():null)));

                    }
                    break;

            }
        }

          catch(RecognitionException re)
          {
            throw re;
          }
        finally {
        }
        return value;
    }
    // $ANTLR end value


    // $ANTLR start set_value
    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:113:1: set_value returns [List<Object> values] : '[' (v1= value ( ',' v2= value )* )* ']' ;
    public final List<Object> set_value() throws RecognitionException {
        List<Object> values = null;

        Object v1 = null;

        Object v2 = null;


         values = new ArrayList<Object>();
        try {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:114:44: ( '[' (v1= value ( ',' v2= value )* )* ']' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:115:3: '[' (v1= value ( ',' v2= value )* )* ']'
            {
            match(input,34,FOLLOW_34_in_set_value510); 
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:115:7: (v1= value ( ',' v2= value )* )*
            loop14:
            do {
                int alt14=2;
                int LA14_0 = input.LA(1);

                if ( (LA14_0==ID||(LA14_0>=INT && LA14_0<=NULL)) ) {
                    alt14=1;
                }


                switch (alt14) {
            	case 1 :
            	    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:115:8: v1= value ( ',' v2= value )*
            	    {
            	    pushFollow(FOLLOW_value_in_set_value515);
            	    v1=value();

            	    state._fsp--;

            	    values.add(v1);
            	    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:115:41: ( ',' v2= value )*
            	    loop13:
            	    do {
            	        int alt13=2;
            	        int LA13_0 = input.LA(1);

            	        if ( (LA13_0==30) ) {
            	            alt13=1;
            	        }


            	        switch (alt13) {
            	    	case 1 :
            	    	    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:115:42: ',' v2= value
            	    	    {
            	    	    match(input,30,FOLLOW_30_in_set_value518); 
            	    	    pushFollow(FOLLOW_value_in_set_value522);
            	    	    v2=value();

            	    	    state._fsp--;

            	    	    values.add(v2);

            	    	    }
            	    	    break;

            	    	default :
            	    	    break loop13;
            	        }
            	    } while (true);


            	    }
            	    break;

            	default :
            	    break loop14;
                }
            } while (true);

            match(input,35,FOLLOW_35_in_set_value529); 

            }

        }

          catch(RecognitionException re)
          {
            throw re;
          }
        finally {
        }
        return values;
    }
    // $ANTLR end set_value

    // Delegated rules


 

    public static final BitSet FOLLOW_stat_in_prog49 = new BitSet(new long[]{0x0000000020000000L});
    public static final BitSet FOLLOW_29_in_prog50 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_EOF_in_prog55 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectStatement_in_stat87 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELECT_in_selectStatement107 = new BitSet(new long[]{0x00000000000000E0L});
    public static final BitSet FOLLOW_selectList_in_selectStatement111 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_FROM_in_selectStatement113 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_selectStatement115 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_whereClause_in_selectStatement120 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STAR_in_selectList148 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_selectList156 = new BitSet(new long[]{0x0000000040000042L});
    public static final BitSet FOLLOW_30_in_selectList159 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_ID_in_selectList163 = new BitSet(new long[]{0x0000000040000042L});
    public static final BitSet FOLLOW_WHERE_in_whereClause184 = new BitSet(new long[]{0x0000000080000840L});
    public static final BitSet FOLLOW_orPredicate_in_whereClause188 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_andPredicate_in_orPredicate208 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_OR_in_orPredicate212 = new BitSet(new long[]{0x0000000080000840L});
    public static final BitSet FOLLOW_andPredicate_in_orPredicate216 = new BitSet(new long[]{0x0000000000000202L});
    public static final BitSet FOLLOW_negatablePredicate_in_andPredicate234 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_AND_in_andPredicate237 = new BitSet(new long[]{0x0000000080000840L});
    public static final BitSet FOLLOW_negatablePredicate_in_andPredicate241 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_NOT_in_negatablePredicate268 = new BitSet(new long[]{0x0000000080000840L});
    public static final BitSet FOLLOW_terminalPredicate_in_negatablePredicate275 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_simplePredicate_in_terminalPredicate293 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_setPredicate_in_terminalPredicate298 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_rangePredicate_in_terminalPredicate302 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_terminalPredicate306 = new BitSet(new long[]{0x0000000080000840L});
    public static final BitSet FOLLOW_orPredicate_in_terminalPredicate310 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_32_in_terminalPredicate312 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_simplePredicate326 = new BitSet(new long[]{0x00000000007F0000L});
    public static final BitSet FOLLOW_s_predicate_op_in_simplePredicate330 = new BitSet(new long[]{0x000000000F800040L});
    public static final BitSet FOLLOW_value_in_simplePredicate334 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_rangePredicate350 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_BETWEEN_in_rangePredicate352 = new BitSet(new long[]{0x000000000F800040L});
    public static final BitSet FOLLOW_value_in_rangePredicate356 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_AND_in_rangePredicate358 = new BitSet(new long[]{0x000000000F800040L});
    public static final BitSet FOLLOW_value_in_rangePredicate362 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_setPredicate374 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_CONTAINS_in_setPredicate376 = new BitSet(new long[]{0x000000040000C000L});
    public static final BitSet FOLLOW_set_in_setPredicate378 = new BitSet(new long[]{0x000000040000C000L});
    public static final BitSet FOLLOW_set_value_in_setPredicate385 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQ_in_s_predicate_op403 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GT_in_s_predicate_op411 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GTE_in_s_predicate_op419 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LT_in_s_predicate_op426 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LTE_in_s_predicate_op435 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOTEQ_in_s_predicate_op442 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STARTSWITH_in_s_predicate_op448 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_INT_in_value462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SINGLE_QUOTED_STRING_in_value467 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_QUOTED_STRING_in_value472 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DOUBLE_in_value477 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NULL_in_value482 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ID_in_value487 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_33_in_value488 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_INT_in_value489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_34_in_set_value510 = new BitSet(new long[]{0x000000080F800040L});
    public static final BitSet FOLLOW_value_in_set_value515 = new BitSet(new long[]{0x000000084F800040L});
    public static final BitSet FOLLOW_30_in_set_value518 = new BitSet(new long[]{0x000000084F800040L});
    public static final BitSet FOLLOW_value_in_set_value522 = new BitSet(new long[]{0x000000084F800040L});
    public static final BitSet FOLLOW_35_in_set_value529 = new BitSet(new long[]{0x0000000000000002L});

}