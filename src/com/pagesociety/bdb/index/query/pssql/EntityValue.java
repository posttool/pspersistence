package com.pagesociety.bdb.index.query.pssql;

public class EntityValue 
{
	private String type;
	private long id;
	public EntityValue(String type,long id)
	{
		this.type = type;
		this.id	  = id;
	}
	
	public String getType()
	{
		return type;
		
	}
	public long getId()
	{
		return id;
	}
}
