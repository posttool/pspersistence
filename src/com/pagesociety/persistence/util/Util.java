package com.pagesociety.persistence.util;

import java.util.Collections;
import java.util.List;

import com.pagesociety.persistence.Entity;


public class Util
{
	public static void sortByFieldNames(List<Entity> entities, String... field_names)
	{
		EntityComparitor c = new EntityComparitor(field_names);
		Collections.sort(entities, c);
	}
	
	//public List<Entity> union(List<Entity>... results){}
	//public List<Entity> intersection(List<Entity>... results){}

	public static void SYSERR(String msg)
	{
		try{
			throw new Exception();
		}catch(Exception e)
		{
			System.err.println("!!SYSERR---");
			e.printStackTrace();
		}
	}
}
