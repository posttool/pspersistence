package com.pagesociety.bdb.index.query;

import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.pagesociety.persistence.Types;

public class EntityComparatorFactory
{
	private static EntityComparatorFactory _instance = new EntityComparatorFactory();
	
	public static Comparator<Entity> getComparator(EntityDefinition def, String sort_attribute) throws PersistenceException
	{
		FieldDefinition f = def.getField(sort_attribute);
		if(f == null)
		{
			throw new PersistenceException("ATTRIBUTE NAMED "+sort_attribute+" DOES NOT EXIST IN ENTITY "+def.getName());
		}
	
		if(f.isArray())
		{
			return _instance.new ArraySizeComparator(sort_attribute);
		}
		switch(f.getType())
		{
			case Types.TYPE_DATE:
				return _instance.new DateComparator(sort_attribute);
			case Types.TYPE_STRING:
			case Types.TYPE_TEXT:
				return _instance.new StringComparator(sort_attribute);
			case Types.TYPE_INT:
				return _instance.new IntComparator(sort_attribute);
			case Types.TYPE_LONG:
				return _instance.new LongComparator(sort_attribute);
			case Types.TYPE_REFERENCE:
				return _instance.new ReferenceComparator(sort_attribute);
			case Types.TYPE_BOOLEAN:
				return _instance.new BooleanComparator(sort_attribute);
			case Types.TYPE_FLOAT:
				return _instance.new FloatComparator(sort_attribute);
			case Types.TYPE_DOUBLE:
				return _instance.new DoubleComparator(sort_attribute);
			case Types.TYPE_BLOB:
				return _instance.new BlobComparator(sort_attribute);
			default:
				throw new PersistenceException("UNKNOWN FIELD TYPE 0x"+Integer.toHexString(f.getType()));

		}

	}

	
	private static final int PASSED_NULL_CHECKS = 0x08;
	private class EntityComparator implements Comparator<Entity>
	{
		protected String compare_attribute;
		protected Object v1;
		protected Object v2;
		public EntityComparator(String compare_attribute)
		{
			this.compare_attribute = compare_attribute;
		}
		
		public int compare(Entity e1,Entity e2)
		{
			v1 = e1.getAttribute(compare_attribute);
			v2 = e2.getAttribute(compare_attribute);
			if(v2 == v1)
				return 0;
			if(v1 == null && v2 != null)
				return -1;
			if(v2 == null && v1 != null)
				return 1;

			
			return PASSED_NULL_CHECKS;
		}
	}
	
	private class ArraySizeComparator  extends EntityComparator
	{
		public ArraySizeComparator(String compare_attribute)
		{
			super(compare_attribute);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			List<?> l1 = (List<?>)v1;
			List<?> l2 = (List<?>)v2;
			return l1.size() - l2.size();		
		}
	}
	
	private class DateComparator  extends EntityComparator
	{
		public DateComparator(String compare_attribute)
		{
			super(compare_attribute);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			Date d1 = ((Date)v1);
			Date d2 = ((Date)v2);
			return d1.compareTo(d2); 
		}
	}
	
	private class StringComparator  extends EntityComparator
	{
		public StringComparator(String compare_attribute)
		{
			super(compare_attribute);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			String s1 = (String)v1;
			String s2 = (String)v2;
			return s1.compareTo(s2);
		}
	}
	
	private class IntComparator  extends EntityComparator
	{
		public IntComparator(String compare_attribute)
		{
			super(compare_attribute);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			Integer i1  = (Integer)v1;
			Integer i2 = (Integer)v2;
			return i1.compareTo(i2);
		}
		
	}
	
	private class LongComparator extends EntityComparator
	{
		public LongComparator(String compare_attribute)
		{
			super(compare_attribute);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			long l1  = (Long)v1;
			long l2  = (Long)v2;
			return (int)(l1 - l2);
		}
	}
	
	private class ReferenceComparator  extends EntityComparator
	{
		public ReferenceComparator(String compare_attribute)
		{
			super(compare_attribute);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			Entity ee1  = (Entity)v1;
			Entity ee2  = (Entity)v2;
			return (int)(ee1.getId() - ee2.getId());
		}
	}
	
	private class BooleanComparator  extends EntityComparator
	{
		public BooleanComparator(String compare_attribute)
		{
			super(compare_attribute);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			Boolean b1  = (Boolean)v1;
			Boolean b2  = (Boolean)v2;
			return b1.compareTo(b2);
		}
	}

	private class FloatComparator  extends EntityComparator
	{
		public FloatComparator(String compare_attribute)
		{
			super(compare_attribute);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			Float f1  = (Float)v1;
			Float f2  = (Float)v2;
			return f1.compareTo(f2);
		}
	}
	
	private class DoubleComparator  extends EntityComparator
	{
		public DoubleComparator(String compare_attribute)
		{
			super(compare_attribute);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			Double d1  = (Double)v1;
			Double d2  = (Double)v2;
			return d1.compareTo(d2);
		}
	}
	
	private class BlobComparator  extends EntityComparator
	{
		public BlobComparator(String compare_attribute)
		{
			super(compare_attribute);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			byte[] ba1  = (byte[])v1;
			byte[] ba2  = (byte[])v2;
			return ba1.length - ba2.length; 
		}
	}



}
