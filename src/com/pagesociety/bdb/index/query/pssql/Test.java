package com.pagesociety.bdb.index.query.pssql;

import java.io.*;
import java.util.List;

public class Test {
    public static void main(String args[]) {
    	
    	String pssql = "Select * from User where a>20 and b< 30 or c>30;";
    	PSSqlExecutor p = new PSSqlExecutor(null);
    	
    	try{
    		p.execute(pssql);
    	}catch(Exception e )
    	{
    		e.printStackTrace();
    	}
    }
    
    
    
 
}