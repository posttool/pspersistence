package com.pagesociety.bdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.QueryResult;

public class BDBQueryResult extends QueryResult
{
	public static final BDBQueryResult EMPTY_RESULT = new BDBQueryResult();
	
	private List<Entity> entities;
	private Object token;
	
	public BDBQueryResult()
	{
		entities = new ArrayList<Entity>(64);
	}
	
	public BDBQueryResult(int size)
	{
		entities = new ArrayList<Entity>(size);
	}
	
	public BDBQueryResult(Collection<Entity> results)
	{
		entities = new ArrayList<Entity>(results);
	}
	

	public List<Entity> getEntities()
	{
		return entities;
	}

	public Object getNextResultsToken()
	{
		return token;
	}
	
	public void setNextResultsToken(BDBQueryToken token)
	{
		this.token = token.toString();
	}

	public void add(Entity e)
	{
		entities.add(e);
	}
	
	public void addAll(List<Entity> ee)
	{
		entities.addAll(ee);
	}
	public int size()
	{
		return entities.size();
	}
}
