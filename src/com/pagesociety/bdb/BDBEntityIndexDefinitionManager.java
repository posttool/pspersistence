package com.pagesociety.bdb;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.pagesociety.bdb.index.EntityIndexDefinition;
import com.pagesociety.persistence.PersistenceException;

public class BDBEntityIndexDefinitionManager 
{
	private static final Logger logger = Logger.getLogger(BDBEntityIndexDefinitionManager.class);
	
	private HashMap<String,EntityIndexDefinition> _map_cache = new HashMap<String,EntityIndexDefinition>();
	private List<EntityIndexDefinition> _list_cache = new ArrayList<EntityIndexDefinition>();
	/* add new index implementations here */
	private static final String[] INDEX_IMPLEMENTATIONS = new String[]
	                                                                 {
		"com.pagesociety.bdb.index.SimpleSingleFieldIndex",
		"com.pagesociety.bdb.index.SimpleMultiFieldIndex", 
		"com.pagesociety.bdb.index.MultiFieldArrayMembershipIndex",
		"com.pagesociety.bdb.index.ArrayMembershipIndex"
	                                                                 };
	
	protected List<EntityIndexDefinition> reloadDefinitions() throws PersistenceException
	{
		return loadDefinitions();
	}
	
	protected List<EntityIndexDefinition> loadDefinitions() throws PersistenceException
	{

		_list_cache = new ArrayList<EntityIndexDefinition>();
		Class<?> c 		= null;
    	Method m 		= null;
    	EntityIndexDefinition d = null;
    	String classname = null;
    	for(int i = 0; i < INDEX_IMPLEMENTATIONS.length;i++)
		{
    		classname = INDEX_IMPLEMENTATIONS[i];
			try{
				c  = Class.forName(classname);			
				m  = c.getDeclaredMethod("getDefinition", (Class[])null);	
				d  = (EntityIndexDefinition)m.invoke(null, (Object[])null);	
				d.setClassName(classname);
			}catch(Exception e)
			{
				e.printStackTrace();
				throw new PersistenceException("PROBLEM GETTING INDEX TYPE "+classname+". SKIPPING");
			}
			/* this might have to be by classname */
			
			_map_cache.put(d.getName(), d);
			_list_cache.add(d);
		}
		return _list_cache;
	}

	protected List<EntityIndexDefinition> getDefinitions() throws PersistenceException
	{
		return _list_cache;
	}
	
	protected EntityIndexDefinition getDefinition(String name)
	{
		return _map_cache.get(name);
	}
	
	protected EntityIndexDefinition getDefinition(int i)
	{
		return _list_cache.get(i);
	}
		
}
