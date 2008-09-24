package com.pagesociety.bdb.index.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.pagesociety.persistence.Query;
import com.pagesociety.persistence.Types;

public class EntityComparatorFactory
{
	private static EntityComparatorFactory _instance = new EntityComparatorFactory();
	

	public static Comparator<Entity> getComparator(EntityDefinition def, String sort_attribute,int order) throws PersistenceException
	{
		FieldDefinition f = def.getField(sort_attribute);
		if(f == null)
		{
			throw new PersistenceException("ATTRIBUTE NAMED "+sort_attribute+" DOES NOT EXIST IN ENTITY "+def.getName());
		}
		
		sort_order sso = null;
		switch(order)
		{
			case Query.ASC:
				sso = _instance.new asc_sort_order();
				break;
			case Query.DESC:
				sso = _instance.new desc_sort_order();
				break;
			default:
				sso = _instance.new asc_sort_order();
		}
		final sort_order so = sso;
		
		if(f.isArray())
		{
			return _instance.new ArraySizeComparator(sort_attribute,so);
		}
		switch(f.getType())
		{
			case Types.TYPE_DATE:
				return _instance.new DateComparator(sort_attribute,so);
			case Types.TYPE_STRING:
			case Types.TYPE_TEXT:
				return _instance.new StringComparator(sort_attribute,so);
			case Types.TYPE_INT:
				return _instance.new IntComparator(sort_attribute,so);
			case Types.TYPE_LONG:
				return _instance.new LongComparator(sort_attribute,so);
			case Types.TYPE_REFERENCE:
				return _instance.new ReferenceComparator(sort_attribute,so);
			case Types.TYPE_BOOLEAN:
				return _instance.new BooleanComparator(sort_attribute,so);
			case Types.TYPE_FLOAT:
				return _instance.new FloatComparator(sort_attribute,so);
			case Types.TYPE_DOUBLE:
				return _instance.new DoubleComparator(sort_attribute,so);
			case Types.TYPE_BLOB:
				return _instance.new BlobComparator(sort_attribute,so);
			default:
				throw new PersistenceException("UNKNOWN FIELD TYPE 0x"+Integer.toHexString(f.getType()));

		}

	}

	
	private static final int PASSED_NULL_CHECKS = 0x08;
	private class EntityComparator implements Comparator<Entity>
	{
		protected final sort_order so;
		protected final String compare_attribute;
		protected Object v1;
		protected Object v2;
		public EntityComparator(final String compare_attribute,final sort_order so)
		{
			this.compare_attribute = compare_attribute;
			this.so = so;
		}
		
		public int compare(Entity e1,Entity e2)
		{
			v1 = e1.getAttribute(compare_attribute);
			v2 = e2.getAttribute(compare_attribute);

			if(v1 == null && v2 != null)
				return -1;
			if(v2 == null && v1 != null)
				return 1;
			if((v1 == null && v2 == null) || v1.equals(v2))
				return 0;
			
			return PASSED_NULL_CHECKS;
		}
	}
	
	private class ArraySizeComparator  extends EntityComparator
	{
		
		public ArraySizeComparator(String compare_attribute, final sort_order so)
		{
			super(compare_attribute,so);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			List<?> l1 = (List<?>)v1;
			List<?> l2 = (List<?>)v2;
			return so.exec((l1.size() < l2.size())?-1:1);		
		}
	}
	
	private class DateComparator  extends EntityComparator
	{
		public DateComparator(String compare_attribute, final sort_order so)
		{
			super(compare_attribute,so);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			Date d1 = ((Date)v1);
			Date d2 = ((Date)v2);
			return so.exec(d1.compareTo(d2)); 
		}
	}
	
	private class StringComparator  extends EntityComparator
	{
		public StringComparator(String compare_attribute,final sort_order so)
		{
			super(compare_attribute,so);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			String s1 = (String)v1;
			String s2 = (String)v2;
			return so.exec(s1.compareTo(s2));
		}
	}
	
	private class IntComparator  extends EntityComparator
	{
		public IntComparator(String compare_attribute,final sort_order so)
		{
			super(compare_attribute,so);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			Integer i1  = (Integer)v1;
			int i2 = (Integer)v2;
			return so.exec((i1 < i2) ? -1:1);
		}
		
	}
	
	private class LongComparator extends EntityComparator
	{
		public LongComparator(String compare_attribute,final sort_order so)
		{
			super(compare_attribute,so);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			long l1  = (Long)v1;
			long l2  = (Long)v2;
			return so.exec((l1<l2) ? -1:1);
		}
	}
	
	private class ReferenceComparator  extends EntityComparator
	{
		public ReferenceComparator(String compare_attribute,final sort_order so)
		{
			super(compare_attribute,so);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			Entity ee1  = (Entity)v1;
			Entity ee2  = (Entity)v2;
			return so.exec((ee1.compareTo(ee2)));
		}
	}
	
	private class BooleanComparator  extends EntityComparator
	{
		public BooleanComparator(String compare_attribute,final sort_order so)
		{
			super(compare_attribute,so);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			Boolean b1  = (Boolean)v1;
			return so.exec((b1?1:-1));
		}
	}

	private class FloatComparator  extends EntityComparator
	{
		public FloatComparator(String compare_attribute,final sort_order so)
		{
			super(compare_attribute,so);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			Float f1  = (Float)v1;
			Float f2  = (Float)v2;
			return so.exec(f1.compareTo(f2));
		}
	}
	
	private class DoubleComparator  extends EntityComparator
	{
		public DoubleComparator(String compare_attribute,final sort_order so)
		{
			super(compare_attribute,so);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			Double d1  = (Double)v1;
			Double d2  = (Double)v2;
			return so.exec(d1.compareTo(d2));
		}
	}
	
	private class BlobComparator  extends EntityComparator
	{
		public BlobComparator(String compare_attribute,final sort_order so)
		{
			super(compare_attribute,so);
		}
		
		public int compare(Entity e1, Entity e2)
		{
			int i = super.compare(e1, e2);
			if(i != PASSED_NULL_CHECKS )
				return i;
			byte[] ba1  = (byte[])v1;
			byte[] ba2  = (byte[])v2;
			return so.exec(ba1.length < ba2.length?-1:1); 
		}
	}

	private interface sort_order
	{
		public abstract int exec(int c);
	}
	
	private class asc_sort_order implements sort_order
	{
		
		public final int exec(int c)
		{
			return c;
		}
		
	}
	
	private class desc_sort_order implements sort_order
	{
		
		public final int exec(int c)
		{
			return -c;
		}
		
	}
	

	
	public static void main(String[] args)
	{
		int n = 100000;
		List<Entity> ee = new ArrayList<Entity>();
		Random R = new Random();
		for(int i = 0;i < n;i++)
		{
			Entity e  = Entity.createInstance();
			e.setAttribute("L", R.nextLong());
			ee.add(e);
		}
		EntityDefinition def 	= new EntityDefinition("TEST");
		FieldDefinition fd 		= new FieldDefinition("L",Types.TYPE_LONG);
		def.addField(fd);
		
		Comparator<Entity> comp = null;
		try{
			comp = EntityComparatorFactory.getComparator(def, "L", 1);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		long t1;
		long t2;
		t1 = System.currentTimeMillis();
		Collections.sort(ee,comp);
		t2 = System.currentTimeMillis();
		//for(int i = 0;i < n;i++)
		//{
		//	System.out.println(ee.get(i).getAttribute("L"));
		//}

		System.out.println("TIME: "+(t2-t1));

	}
}
