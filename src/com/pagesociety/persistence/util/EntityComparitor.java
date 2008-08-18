package com.pagesociety.persistence.util;

import java.util.Comparator;

import com.pagesociety.persistence.Entity;


public class EntityComparitor implements Comparator<Entity>
{
	String[] _fields;

	public EntityComparitor(String... field_names)
	{
		_fields = field_names;
	}
/* there is another kind of comparator that needs to be written that does it
 * on field type without converting to string
 * */
	public int compare(Entity e1, Entity e2)
	{
		StringBuffer eb1 = new StringBuffer();
		StringBuffer eb2 = new StringBuffer();
		for (int i = 0; i < _fields.length; i++)
		{
			Object eo1 = e1.getAttribute(_fields[i]);
			Object eo2 = e2.getAttribute(_fields[i]);
			eb1.append(eo1);
			eb2.append(eo2);
		}
		return eb1.toString().compareTo(eb2.toString());
	
	}
	
	public int compare_desc(Entity e1, Entity e2)
	{
		StringBuffer eb1 = new StringBuffer();
		StringBuffer eb2 = new StringBuffer();
		for (int i = 0; i < _fields.length; i++)
		{
			Object eo1 = e1.getAttribute(_fields[i]);
			Object eo2 = e2.getAttribute(_fields[i]);
			eb1.append(eo1);
			eb2.append(eo2);
		}
		return -(eb1.toString().compareTo(eb2.toString()));
	}
	

	
}
