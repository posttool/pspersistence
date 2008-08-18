package com.pagesociety.persistence.util;

import java.util.Comparator;

import com.pagesociety.persistence.Entity;

public class EntityIdComparator implements Comparator<Entity>
{

	public int compare(Entity e1, Entity e2)
	{
		return (int)(e1.getId() - e2.getId());
	}

}
