package com.pagesociety.bdb.index;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pagesociety.bdb.BDBSecondaryIndex;
import com.pagesociety.bdb.binding.FieldBinding;
import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.pagesociety.persistence.Query;
import com.pagesociety.persistence.Types;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.util.FastOutputStream;

public class SimpleMultiFieldIndex extends AbstractMultiFieldIndex
{

	public static final String NAME = SimpleMultiFieldIndex.class.getSimpleName();

	public static final String ATTR_USE_LOWER_CASE 	= "use lower case";
	
//	private boolean _use_lower_case = false;

	public SimpleMultiFieldIndex()
	{
		super(BDBSecondaryIndex.TYPE_NORMAL_INDEX);
	}
	
	public void init(Map<String,Object> attributes)
	{
// LOOP & CHECK FOR STRING FIELDS
//		Object lc = attributes.get(ATTR_USE_LOWER_CASE);
//		if (lc!=null)
//			if (field.getBaseType()==Types.TYPE_STRING || field.getBaseType()==Types.TYPE_TEXT)
//				_use_lower_case = (Boolean)lc;
//			else
//				throw new PersistenceException("CAN'T USE LOWER CASE ATTRIBUTE ON NON TEXT INDEX");
	}
	
	public void getInsertKeys(Entity e,Set<DatabaseEntry> result) throws DatabaseException
	{
		if(isDeepIndex())
		{
			//System.out.println("!!!!GET INSERT KEYS FOR "+e.getType()+" "+e.getId());
			deep_insert_keys_permutator_executor ip = new deep_insert_keys_permutator_executor(e,result);
			ip.exec();
		}
		else
		{
			List<FieldDefinition> fields = getFields();	
			int s = fields.size();
			TupleOutput tto = new TupleOutput();
			for(int i = 0; i < s;i++)
			{
				FieldDefinition f = fields.get(i);
				Object val = e.getAttributes().get(f.getName());
				if(f.isArray())
				{
					if(val == null)
						FieldBinding.doWriteValueToTuple(f.getBaseType(), null, tto);
					else
					{
						List<Comparable<Object>> array = (List<Comparable<Object>>)val;				
						Collections.sort(array);
						for(int ii = 0; ii < array.size();ii++)
							FieldBinding.doWriteValueToTuple(f.getBaseType(), array.get(ii), tto);	
					}
				}
				else
					FieldBinding.doWriteValueToTuple(f.getBaseType(), val, tto);
			}	
			DatabaseEntry d = new DatabaseEntry(tto.toByteArray());
			//System.out.println(">>> KEY IS "+new String(d.getData()));
			result.add(d);
		}
	}

	
//	private Object transform_value(Object val, Field field)
//	{
//		if (_use_lower_case && val instanceof String)
//		{
//			return ((String)val).toLowerCase();
//		}
//		
//		return val;
//	}
	private class deep_insert_keys_permutator_executor
	{		
		Entity e;
		private int[] c;
		private  DatabaseEntry[][] values_as_entries;
		private boolean is_valid;
		private Set<DatabaseEntry> keys;
		private deep_insert_keys_permutator_executor(Entity e,Set<DatabaseEntry> keys) throws DatabaseException
		{
			this.e	     		   	= e;
			int s 				   	= fields.size();
			this.c 				   	= new int[s];
			this.values_as_entries 	= new DatabaseEntry[s][];
			this.is_valid 			= true;
			this.keys				= keys;
			for(int i = 0; i < fields.size();i++)
			{
				FieldDefinition f = getFields().get(i);
				Set<Object> vals = new HashSet<Object>();
				if(f.getName().indexOf('.')!=-1)
				{
					String[] ref_path 					   = (String[])getAttribute(ATTRIBUTE_DEEP_INDEX_PATH_LOCATOR_PREFIX+i);
					List<FieldDefinition>[] ref_path_types = (List<FieldDefinition>[])getAttribute(ATTRIBUTE_DEEP_INDEX_PATH_TYPE_LOCATOR_PREFIX+i);
					if(ref_path == null || ref_path_types == null)
						throw new DatabaseException("BAD DEEP INDEX META INFO! WTF");
					
					
					get_deep_insert_vals(e, ref_path, ref_path_types, 0, vals);
					if(vals.size() == 0)
					{
						values_as_entries[i] 	= new DatabaseEntry[1];
						values_as_entries[i][0] = new DatabaseEntry();
						//NOTE: TODO: IDEALLY WE WOULD HAVE SOME SPECIAL KEY TO INSERT HERE
						//WHICH MEANS THIS REF PATH ENDED WITHOUT A TERMINAL VALUE...IT IS
						//PRETY PHILOSPHICAL
						//StringBinding.stringToEntry("null_"+f.getName()/*+String.valueOf(Math.random())*/,values_as_entries[i][0] );
						FieldBinding.valueToEntry(f.getBaseType(), null,values_as_entries[i][0]);		
					}
					else
					{
						int vs = vals.size();
						Iterator<Object> iter = vals.iterator();
						values_as_entries[i] = new DatabaseEntry[vs];
						int ii = 0;
						while(iter.hasNext())
						{
							Object v = iter.next();
							values_as_entries[i][ii] = new DatabaseEntry();
							FieldBinding.valueToEntry(f.getBaseType(), v,values_as_entries[i][ii]);
							ii++;
						}
					}
				}
				else if(f.isArray())
				{
					List<Object> vvals = (List<Object>)e.getAttribute(f.getName());
					//System.out.println("HIT ARRAY FIELD "+f.getName()+" WITH VALUE "+vals);
					if(vvals == null || vvals.size() == 0)
					{
						values_as_entries[i] 	= new DatabaseEntry[1];
						values_as_entries[i][0] = new DatabaseEntry();
						FieldBinding.valueToEntry(f.getBaseType(), null,values_as_entries[i][0]);		
					}
					else
					{
						int vs = vvals.size();
						values_as_entries[i] = new DatabaseEntry[vs];
						for(int ii = 0; ii < vs;ii++)
						{
							values_as_entries[i][ii] = new DatabaseEntry();
							FieldBinding.valueToEntry(f.getBaseType(), vvals.get(ii),values_as_entries[i][ii]);
						}
					}	
				}
				else
				{
					Object val = e.getAttribute(f.getName());
					//System.out.println("HIT SINGLE FIELD "+f.getName()+" WITH VALUE "+val);
					values_as_entries[i] 	= new DatabaseEntry[1];
					values_as_entries[i][0] = new DatabaseEntry();
					FieldBinding.valueToEntry(f.getBaseType(), val,values_as_entries[i][0]);
				}
				
				c[i] = 0;
			}
		}


		private void tick(int p)
		{
			if(p == -1)
			{
				is_valid = false;
				return;
			}
			c[p]++;			
			if(c[p] == values_as_entries[p].length)
			{
				tick(p-1);
				c[p] = 0;
			}
		}
				
		private void exec()
		{
			do
			{
				FastOutputStream fos = new FastOutputStream();
				DatabaseEntry d 	 = new DatabaseEntry();
				for(int i = 0; i < c.length;i++)
				{
					DatabaseEntry dbe   = values_as_entries[i][c[i]];
					if(dbe == null)
						continue;
					fos.writeFast(dbe.getData(),dbe.getOffset(),dbe.getSize());
				}
				d.setData(fos.toByteArray(),fos.getBufferOffset(),fos.getBufferLength());
				//System.out.println("ADDING MULTI ARRAY KEY "+new String(d.getData()));
				keys.add(d);
				tick(c.length-1);
			}while(is_valid);
		}
	}

	public void get_deep_insert_vals(Entity e,String[] ref_path,List<FieldDefinition>[] ref_path_types,int offset,Set<Object> vals) throws DatabaseException
	{
		
		if(offset == ref_path.length-1)
		{
			Object val = e.getAttribute(ref_path[offset]);
			//DatabaseEntry entry = getQueryKey(val);
			//System.out.println("!!!!!!!!!!!!!!!!!!INSERTING INDEX ENTRY "+new String(entry.getData(),0,entry.getSize()));
			vals.add(val);
			return;
		}
		
		FieldDefinition ref_type = ref_path_types[offset].get(0);
		if(ref_type.isArray())
		{
			List<Entity> vvals = (List<Entity>)e.getAttribute(ref_path[offset]);

			if(vvals == null){//abandon the path
				return;
			}//TODO: SHOULD WE INSERT A NULL HERE FOR THIS DUDE IF ONE DONT EXIST??//
					   //....see BDBSecondaryIndex.deleteIndexEntry for related note
					   //we should be inserting something at some point
			int s = vvals.size();
			for(int i = 0;i < s;i++)
				get_deep_insert_vals(vvals.get(i), ref_path, ref_path_types, offset+1,vals);
		}
		else
		{
			Entity val = (Entity)e.getAttribute(ref_path[offset]);
			//System.out.println("TRYING TO GET "+e.getType()+" ATT "+ref_path[offset]+" IT IS "+val);
			if(val == null)//abandon the path
				return;//TODO: SHOULD WE INSERT A NULL HERE FOR THIS DUDE IF ONE DONT EXIST??//
			get_deep_insert_vals(val, ref_path, ref_path_types, offset+1,vals);
		}
		
	}
	
	
	public DatabaseEntry getQueryKey(List<Object> values) throws DatabaseException
	{
		List<FieldDefinition> fields = getFields();	
		//notice how we use values size here instead of fields.size() as above
		//this is to support generation of keys for use with the startswith iterator
		//where we need to be able to generate partial keys
		int s = values.size();
		
		TupleOutput tto = new TupleOutput();
		for(int i = 0; i < s;i++)
		{
			FieldDefinition f = fields.get(i);
			Object val = values.get(i);
			
			if(val == Query.VAL_MIN)
			{
				FieldBinding.writeMinVal(f.getBaseType(),tto);
				continue;
			}
			else if(val == Query.VAL_MAX)
			{
				FieldBinding.writeMaxVal(f.getBaseType(),tto);
				continue;
			}
			

			if(f.isArray())
			{
				if(val == null)
					FieldBinding.doWriteValueToTuple(f.getBaseType(), null, tto);
				else
				{
					List<Comparable<Object>> array = (List<Comparable<Object>>)val;				
					Collections.sort(array);
					for(int ii = 0; ii < array.size();ii++)
						FieldBinding.doWriteValueToTuple(f.getBaseType(), array.get(ii), tto);	
				}
			}
			else
				FieldBinding.doWriteValueToTuple(f.getBaseType(), val, tto);
		}	
		DatabaseEntry d = new DatabaseEntry(tto.toByteArray());
	//	System.out.println("!!! QUERY KEY IS "+new String(d.getData()));
		return d;
	
	}


	
	
	
	/* No operators since this is an equality index so Type.EQ is implicit for all fields*/
	public List<FieldDefinition> getQueryParameters()
	{
		return getFields();
	}
	
	/*only accepts EQUALITY AND BETWEEN */
	public static EntityIndexDefinition getDefinition()
	{
		EntityIndexDefinition definition = new EntityIndexDefinition();
		definition.setName(NAME);
		definition.setIsMultiField(true);
		definition.setDescription(" Creates an index on multiple fields in an entity"+
								  " ordered by the first specified field. The entities can "+
								  " be queried for equality accross index member fields."+
								  " with one field being able to be a range field. There are"+
								  " no set query operations supported.It just makes a compound key"+
								  " of all fields");
//TODO		
//		FieldDefinition use_lower_case = new FieldDefinition();
//		use_lower_case.setName(ATTR_USE_LOWER_CASE);
//		use_lower_case.setType(Types.TYPE_BOOLEAN);
//		definition.addAttribute(use_lower_case);
		

		return definition;
	}


}
