// $ANTLR 3.1b1 C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g 2008-08-01 19:43:48
package com.pagesociety.bdb.index.query.pssql;

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class PSSqlLexer extends Lexer {
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
    public static final int T__32=32;
    public static final int NOTEQ=21;
    public static final int T__33=33;
    public static final int ANY=14;
    public static final int WS=28;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int ALL=15;
    public static final int OR=9;
    public static final int STARTSWITH=22;
    public static final int DOUBLE=26;
    public static final int GT=17;
    public static final int EQ=16;
    public static final int FROM=5;
    public static final int SELECT=4;
    public static final int BETWEEN=12;

    // delegates
    // delegators

    public PSSqlLexer() {;} 
    public PSSqlLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public PSSqlLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g"; }

    // $ANTLR start T__29
    public final void mT__29() throws RecognitionException {
        try {
            int _type = T__29;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:5:7: ( ';' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:5:9: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end T__29

    // $ANTLR start T__30
    public final void mT__30() throws RecognitionException {
        try {
            int _type = T__30;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:6:7: ( ',' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:6:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end T__30

    // $ANTLR start T__31
    public final void mT__31() throws RecognitionException {
        try {
            int _type = T__31;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:7:7: ( '(' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:7:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end T__31

    // $ANTLR start T__32
    public final void mT__32() throws RecognitionException {
        try {
            int _type = T__32;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:8:7: ( ')' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:8:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end T__32

    // $ANTLR start T__33
    public final void mT__33() throws RecognitionException {
        try {
            int _type = T__33;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:9:7: ( ':' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:9:9: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end T__33

    // $ANTLR start T__34
    public final void mT__34() throws RecognitionException {
        try {
            int _type = T__34;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:10:7: ( '[' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:10:9: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end T__34

    // $ANTLR start T__35
    public final void mT__35() throws RecognitionException {
        try {
            int _type = T__35;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:11:7: ( ']' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:11:9: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end T__35

    // $ANTLR start SELECT
    public final void mSELECT() throws RecognitionException {
        try {
            int _type = SELECT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:124:7: ( ( 'S' | 's' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' ) )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:124:9: ( 'S' | 's' ) ( 'E' | 'e' ) ( 'L' | 'l' ) ( 'E' | 'e' ) ( 'C' | 'c' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end SELECT

    // $ANTLR start FROM
    public final void mFROM() throws RecognitionException {
        try {
            int _type = FROM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:125:5: ( ( 'F' | 'f' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'M' | 'm' ) )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:125:7: ( 'F' | 'f' ) ( 'R' | 'r' ) ( 'O' | 'o' ) ( 'M' | 'm' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end FROM

    // $ANTLR start WHERE
    public final void mWHERE() throws RecognitionException {
        try {
            int _type = WHERE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:126:6: ( ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' ) )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:126:8: ( 'W' | 'w' ) ( 'H' | 'h' ) ( 'E' | 'e' ) ( 'R' | 'r' ) ( 'E' | 'e' )
            {
            if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end WHERE

    // $ANTLR start CONTAINS
    public final void mCONTAINS() throws RecognitionException {
        try {
            int _type = CONTAINS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:127:9: ( ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'S' | 's' ) )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:127:10: ( 'C' | 'c' ) ( 'O' | 'o' ) ( 'N' | 'n' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'I' | 'i' ) ( 'N' | 'n' ) ( 'S' | 's' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end CONTAINS

    // $ANTLR start ANY
    public final void mANY() throws RecognitionException {
        try {
            int _type = ANY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:128:4: ( ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'Y' | 'y' ) )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:128:6: ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'Y' | 'y' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end ANY

    // $ANTLR start ALL
    public final void mALL() throws RecognitionException {
        try {
            int _type = ALL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:129:4: ( ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'L' | 'l' ) )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:129:6: ( 'A' | 'a' ) ( 'L' | 'l' ) ( 'L' | 'l' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end ALL

    // $ANTLR start AND
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:130:4: ( ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' ) )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:130:6: ( 'A' | 'a' ) ( 'N' | 'n' ) ( 'D' | 'd' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end AND

    // $ANTLR start OR
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:131:3: ( ( 'O' | 'o' ) ( 'R' | 'r' ) )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:131:5: ( 'O' | 'o' ) ( 'R' | 'r' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end OR

    // $ANTLR start BETWEEN
    public final void mBETWEEN() throws RecognitionException {
        try {
            int _type = BETWEEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:132:8: ( ( 'B' | 'b' ) ( 'E' | 'e' ) ( 'T' | 't' ) ( 'W' | 'w' ) ( 'E' | 'e' ) ( 'E' | 'e' ) ( 'N' | 'n' ) )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:132:9: ( 'B' | 'b' ) ( 'E' | 'e' ) ( 'T' | 't' ) ( 'W' | 'w' ) ( 'E' | 'e' ) ( 'E' | 'e' ) ( 'N' | 'n' )
            {
            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end BETWEEN

    // $ANTLR start STARTSWITH
    public final void mSTARTSWITH() throws RecognitionException {
        try {
            int _type = STARTSWITH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:133:11: ( ( 'S' | 's' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'T' | 't' ) ( 'S' | 's' ) ( 'W' | 'w' ) ( 'I' | 'i' ) ( 'T' | 't' ) ( 'H' | 'h' ) )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:133:13: ( 'S' | 's' ) ( 'T' | 't' ) ( 'A' | 'a' ) ( 'R' | 'r' ) ( 'T' | 't' ) ( 'S' | 's' ) ( 'W' | 'w' ) ( 'I' | 'i' ) ( 'T' | 't' ) ( 'H' | 'h' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end STARTSWITH

    // $ANTLR start NOT
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:134:4: ( ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' ) )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:134:6: ( 'N' | 'n' ) ( 'O' | 'o' ) ( 'T' | 't' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end NOT

    // $ANTLR start NULL
    public final void mNULL() throws RecognitionException {
        try {
            int _type = NULL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:135:5: ( ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' ) )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:135:7: ( 'N' | 'n' ) ( 'U' | 'u' ) ( 'L' | 'l' ) ( 'L' | 'l' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end NULL

    // $ANTLR start ID
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:136:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )* )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:136:9: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:136:32: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end ID

    // $ANTLR start INT
    public final void mINT() throws RecognitionException {
        try {
            int _type = INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:139:5: ( ( '0' .. '9' )+ )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:139:9: ( '0' .. '9' )+
            {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:139:9: ( '0' .. '9' )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='0' && LA2_0<='9')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:139:10: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end INT

    // $ANTLR start DOUBLE
    public final void mDOUBLE() throws RecognitionException {
        try {
            int _type = DOUBLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:140:7: ( ( '0' .. '9' )* '\\.' ( '0' .. '9' )* )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:140:9: ( '0' .. '9' )* '\\.' ( '0' .. '9' )*
            {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:140:9: ( '0' .. '9' )*
            loop3:
            do {
                int alt3=2;
                int LA3_0 = input.LA(1);

                if ( ((LA3_0>='0' && LA3_0<='9')) ) {
                    alt3=1;
                }


                switch (alt3) {
            	case 1 :
            	    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:140:10: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);

            match('.'); 
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:140:26: ( '0' .. '9' )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( ((LA4_0>='0' && LA4_0<='9')) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:140:27: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end DOUBLE

    // $ANTLR start SINGLE_QUOTED_STRING
    public final void mSINGLE_QUOTED_STRING() throws RecognitionException {
        try {
            int _type = SINGLE_QUOTED_STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:141:21: ( '\\'' ( . )* '\\'' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:141:23: '\\'' ( . )* '\\''
            {
            match('\''); 
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:141:28: ( . )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0=='\'') ) {
                    alt5=2;
                }
                else if ( ((LA5_0>='\u0000' && LA5_0<='&')||(LA5_0>='(' && LA5_0<='\uFFFE')) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:141:28: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);

            match('\''); 
            setText(getText().substring(1, getText().length()-1));

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end SINGLE_QUOTED_STRING

    // $ANTLR start DOUBLE_QUOTED_STRING
    public final void mDOUBLE_QUOTED_STRING() throws RecognitionException {
        try {
            int _type = DOUBLE_QUOTED_STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:142:21: ( '\"' ( . )* '\"' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:142:24: '\"' ( . )* '\"'
            {
            match('\"'); 
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:142:28: ( . )*
            loop6:
            do {
                int alt6=2;
                int LA6_0 = input.LA(1);

                if ( (LA6_0=='\"') ) {
                    alt6=2;
                }
                else if ( ((LA6_0>='\u0000' && LA6_0<='!')||(LA6_0>='#' && LA6_0<='\uFFFE')) ) {
                    alt6=1;
                }


                switch (alt6) {
            	case 1 :
            	    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:142:28: .
            	    {
            	    matchAny(); 

            	    }
            	    break;

            	default :
            	    break loop6;
                }
            } while (true);

            match('\"'); 
            setText(getText().substring(1, getText().length()-1));

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end DOUBLE_QUOTED_STRING

    // $ANTLR start WS
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:144:5: ( ( ' ' | '\\t' | '\\r' | '\\n' )+ )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:144:9: ( ' ' | '\\t' | '\\r' | '\\n' )+
            {
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:144:9: ( ' ' | '\\t' | '\\r' | '\\n' )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( ((LA7_0>='\t' && LA7_0<='\n')||LA7_0=='\r'||LA7_0==' ') ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||input.LA(1)=='\r'||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt7 >= 1 ) break loop7;
                        EarlyExitException eee =
                            new EarlyExitException(7, input);
                        throw eee;
                }
                cnt7++;
            } while (true);

             _channel=HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end WS

    // $ANTLR start STAR
    public final void mSTAR() throws RecognitionException {
        try {
            int _type = STAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:153:5: ( '*' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:153:7: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end STAR

    // $ANTLR start EQ
    public final void mEQ() throws RecognitionException {
        try {
            int _type = EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:157:3: ( '=' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:157:5: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end EQ

    // $ANTLR start GT
    public final void mGT() throws RecognitionException {
        try {
            int _type = GT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:158:3: ( '>' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:158:5: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end GT

    // $ANTLR start GTE
    public final void mGTE() throws RecognitionException {
        try {
            int _type = GTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:159:4: ( '>=' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:159:6: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end GTE

    // $ANTLR start LT
    public final void mLT() throws RecognitionException {
        try {
            int _type = LT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:160:3: ( '<' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:160:5: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end LT

    // $ANTLR start LTE
    public final void mLTE() throws RecognitionException {
        try {
            int _type = LTE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:161:4: ( '<=' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:161:6: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end LTE

    // $ANTLR start NOTEQ
    public final void mNOTEQ() throws RecognitionException {
        try {
            int _type = NOTEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:162:6: ( '!=' )
            // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:162:8: '!='
            {
            match("!="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end NOTEQ

    public void mTokens() throws RecognitionException {
        // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:8: ( T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | SELECT | FROM | WHERE | CONTAINS | ANY | ALL | AND | OR | BETWEEN | STARTSWITH | NOT | NULL | ID | INT | DOUBLE | SINGLE_QUOTED_STRING | DOUBLE_QUOTED_STRING | WS | STAR | EQ | GT | GTE | LT | LTE | NOTEQ )
        int alt8=32;
        alt8 = dfa8.predict(input);
        switch (alt8) {
            case 1 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:10: T__29
                {
                mT__29(); 

                }
                break;
            case 2 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:16: T__30
                {
                mT__30(); 

                }
                break;
            case 3 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:22: T__31
                {
                mT__31(); 

                }
                break;
            case 4 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:28: T__32
                {
                mT__32(); 

                }
                break;
            case 5 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:34: T__33
                {
                mT__33(); 

                }
                break;
            case 6 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:40: T__34
                {
                mT__34(); 

                }
                break;
            case 7 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:46: T__35
                {
                mT__35(); 

                }
                break;
            case 8 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:52: SELECT
                {
                mSELECT(); 

                }
                break;
            case 9 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:59: FROM
                {
                mFROM(); 

                }
                break;
            case 10 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:64: WHERE
                {
                mWHERE(); 

                }
                break;
            case 11 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:70: CONTAINS
                {
                mCONTAINS(); 

                }
                break;
            case 12 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:79: ANY
                {
                mANY(); 

                }
                break;
            case 13 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:83: ALL
                {
                mALL(); 

                }
                break;
            case 14 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:87: AND
                {
                mAND(); 

                }
                break;
            case 15 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:91: OR
                {
                mOR(); 

                }
                break;
            case 16 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:94: BETWEEN
                {
                mBETWEEN(); 

                }
                break;
            case 17 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:102: STARTSWITH
                {
                mSTARTSWITH(); 

                }
                break;
            case 18 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:113: NOT
                {
                mNOT(); 

                }
                break;
            case 19 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:117: NULL
                {
                mNULL(); 

                }
                break;
            case 20 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:122: ID
                {
                mID(); 

                }
                break;
            case 21 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:125: INT
                {
                mINT(); 

                }
                break;
            case 22 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:129: DOUBLE
                {
                mDOUBLE(); 

                }
                break;
            case 23 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:136: SINGLE_QUOTED_STRING
                {
                mSINGLE_QUOTED_STRING(); 

                }
                break;
            case 24 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:157: DOUBLE_QUOTED_STRING
                {
                mDOUBLE_QUOTED_STRING(); 

                }
                break;
            case 25 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:178: WS
                {
                mWS(); 

                }
                break;
            case 26 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:181: STAR
                {
                mSTAR(); 

                }
                break;
            case 27 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:186: EQ
                {
                mEQ(); 

                }
                break;
            case 28 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:189: GT
                {
                mGT(); 

                }
                break;
            case 29 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:192: GTE
                {
                mGTE(); 

                }
                break;
            case 30 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:196: LT
                {
                mLT(); 

                }
                break;
            case 31 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:199: LTE
                {
                mLTE(); 

                }
                break;
            case 32 :
                // C:\\eclipse_workspace\\PSPersistence\\src\\com\\pagesociety\\bdb\\index\\query\\pssql\\PSSql.g:1:203: NOTEQ
                {
                mNOTEQ(); 

                }
                break;

        }

    }


    protected DFA8 dfa8 = new DFA8(this);
    static final String DFA8_eotS =
        "\10\uffff\10\20\1\uffff\1\46\6\uffff\1\50\1\52\1\uffff\7\20\1\63"+
        "\3\20\5\uffff\5\20\1\74\1\75\1\76\1\uffff\1\20\1\100\3\20\1\104"+
        "\2\20\3\uffff\1\20\1\uffff\1\110\2\20\1\uffff\1\113\2\20\1\uffff"+
        "\1\116\1\20\1\uffff\2\20\1\uffff\2\20\1\124\1\20\1\126\1\uffff\1"+
        "\20\1\uffff\1\130\1\uffff";
    static final String DFA8_eofS =
        "\131\uffff";
    static final String DFA8_minS =
        "\1\11\7\uffff\1\105\1\122\1\110\1\117\1\114\1\122\1\105\1\117\1"+
        "\uffff\1\56\6\uffff\2\75\1\uffff\1\114\1\101\1\117\1\105\1\116\1"+
        "\104\1\114\1\60\2\124\1\114\5\uffff\1\105\1\122\1\115\1\122\1\124"+
        "\3\60\1\uffff\1\127\1\60\1\114\1\103\1\124\1\60\1\105\1\101\3\uffff"+
        "\1\105\1\uffff\1\60\1\124\1\123\1\uffff\1\60\1\111\1\105\1\uffff"+
        "\1\60\1\127\1\uffff\2\116\1\uffff\1\111\1\123\1\60\1\124\1\60\1"+
        "\uffff\1\110\1\uffff\1\60\1\uffff";
    static final String DFA8_maxS =
        "\1\172\7\uffff\1\164\1\162\1\150\1\157\1\156\1\162\1\145\1\165"+
        "\1\uffff\1\71\6\uffff\2\75\1\uffff\1\154\1\141\1\157\1\145\1\156"+
        "\1\171\1\154\1\172\2\164\1\154\5\uffff\1\145\1\162\1\155\1\162\1"+
        "\164\3\172\1\uffff\1\167\1\172\1\154\1\143\1\164\1\172\1\145\1\141"+
        "\3\uffff\1\145\1\uffff\1\172\1\164\1\163\1\uffff\1\172\1\151\1\145"+
        "\1\uffff\1\172\1\167\1\uffff\2\156\1\uffff\1\151\1\163\1\172\1\164"+
        "\1\172\1\uffff\1\150\1\uffff\1\172\1\uffff";
    static final String DFA8_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\10\uffff\1\24\1\uffff\1\26"+
        "\1\27\1\30\1\31\1\32\1\33\2\uffff\1\40\13\uffff\1\25\1\35\1\34\1"+
        "\37\1\36\10\uffff\1\17\10\uffff\1\14\1\16\1\15\1\uffff\1\22\3\uffff"+
        "\1\11\3\uffff\1\23\2\uffff\1\12\2\uffff\1\10\5\uffff\1\20\1\uffff"+
        "\1\13\1\uffff\1\21";
    static final String DFA8_specialS =
        "\131\uffff}>";
    static final String[] DFA8_transitionS = {
            "\2\25\2\uffff\1\25\22\uffff\1\25\1\32\1\24\4\uffff\1\23\1\3"+
            "\1\4\1\26\1\uffff\1\2\1\uffff\1\22\1\uffff\12\21\1\5\1\1\1\31"+
            "\1\27\1\30\2\uffff\1\14\1\16\1\13\2\20\1\11\7\20\1\17\1\15\3"+
            "\20\1\10\3\20\1\12\3\20\1\6\1\uffff\1\7\1\uffff\1\20\1\uffff"+
            "\1\14\1\16\1\13\2\20\1\11\7\20\1\17\1\15\3\20\1\10\3\20\1\12"+
            "\3\20",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\33\16\uffff\1\34\20\uffff\1\33\16\uffff\1\34",
            "\1\35\37\uffff\1\35",
            "\1\36\37\uffff\1\36",
            "\1\37\37\uffff\1\37",
            "\1\41\1\uffff\1\40\35\uffff\1\41\1\uffff\1\40",
            "\1\42\37\uffff\1\42",
            "\1\43\37\uffff\1\43",
            "\1\44\5\uffff\1\45\31\uffff\1\44\5\uffff\1\45",
            "",
            "\1\22\1\uffff\12\21",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\47",
            "\1\51",
            "",
            "\1\53\37\uffff\1\53",
            "\1\54\37\uffff\1\54",
            "\1\55\37\uffff\1\55",
            "\1\56\37\uffff\1\56",
            "\1\57\37\uffff\1\57",
            "\1\61\24\uffff\1\60\12\uffff\1\61\24\uffff\1\60",
            "\1\62\37\uffff\1\62",
            "\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "\1\64\37\uffff\1\64",
            "\1\65\37\uffff\1\65",
            "\1\66\37\uffff\1\66",
            "",
            "",
            "",
            "",
            "",
            "\1\67\37\uffff\1\67",
            "\1\70\37\uffff\1\70",
            "\1\71\37\uffff\1\71",
            "\1\72\37\uffff\1\72",
            "\1\73\37\uffff\1\73",
            "\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "",
            "\1\77\37\uffff\1\77",
            "\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "\1\101\37\uffff\1\101",
            "\1\102\37\uffff\1\102",
            "\1\103\37\uffff\1\103",
            "\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "\1\105\37\uffff\1\105",
            "\1\106\37\uffff\1\106",
            "",
            "",
            "",
            "\1\107\37\uffff\1\107",
            "",
            "\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "\1\111\37\uffff\1\111",
            "\1\112\37\uffff\1\112",
            "",
            "\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "\1\114\37\uffff\1\114",
            "\1\115\37\uffff\1\115",
            "",
            "\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "\1\117\37\uffff\1\117",
            "",
            "\1\120\37\uffff\1\120",
            "\1\121\37\uffff\1\121",
            "",
            "\1\122\37\uffff\1\122",
            "\1\123\37\uffff\1\123",
            "\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "\1\125\37\uffff\1\125",
            "\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            "",
            "\1\127\37\uffff\1\127",
            "",
            "\12\20\7\uffff\32\20\4\uffff\1\20\1\uffff\32\20",
            ""
    };

    static final short[] DFA8_eot = DFA.unpackEncodedString(DFA8_eotS);
    static final short[] DFA8_eof = DFA.unpackEncodedString(DFA8_eofS);
    static final char[] DFA8_min = DFA.unpackEncodedStringToUnsignedChars(DFA8_minS);
    static final char[] DFA8_max = DFA.unpackEncodedStringToUnsignedChars(DFA8_maxS);
    static final short[] DFA8_accept = DFA.unpackEncodedString(DFA8_acceptS);
    static final short[] DFA8_special = DFA.unpackEncodedString(DFA8_specialS);
    static final short[][] DFA8_transition;

    static {
        int numStates = DFA8_transitionS.length;
        DFA8_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA8_transition[i] = DFA.unpackEncodedString(DFA8_transitionS[i]);
        }
    }

    class DFA8 extends DFA {

        public DFA8(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 8;
            this.eot = DFA8_eot;
            this.eof = DFA8_eof;
            this.min = DFA8_min;
            this.max = DFA8_max;
            this.accept = DFA8_accept;
            this.special = DFA8_special;
            this.transition = DFA8_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__29 | T__30 | T__31 | T__32 | T__33 | T__34 | T__35 | SELECT | FROM | WHERE | CONTAINS | ANY | ALL | AND | OR | BETWEEN | STARTSWITH | NOT | NULL | ID | INT | DOUBLE | SINGLE_QUOTED_STRING | DOUBLE_QUOTED_STRING | WS | STAR | EQ | GT | GTE | LT | LTE | NOTEQ );";
        }
    }
 

}