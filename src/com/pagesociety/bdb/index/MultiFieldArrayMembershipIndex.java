package com.pagesociety.bdb.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pagesociety.bdb.BDBSecondaryIndex;
import com.pagesociety.bdb.binding.FieldBinding;
import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.FieldDefinition;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.util.FastOutputStream;

public class MultiFieldArrayMembershipIndex extends AbstractMultiFieldIndex
{

	public static final String NAME = MultiFieldArrayMembershipIndex.class.getSimpleName();


	public MultiFieldArrayMembershipIndex()
	{
		super(BDBSecondaryIndex.TYPE_SET_INDEX);
	}
	
	
	public void init(Map<String,String> attributes)
	{
		/* this is how we deal with default parameters */
	}
	
	//private static int num_insert_keys_requested = 0;
	public void getInsertKeys(Entity e,Set<DatabaseEntry> result) throws DatabaseException
	{
		insert_keys_permutator_executor ip = new insert_keys_permutator_executor(e,result);
		ip.exec();
		//System.out.println("num insert keys requested is "+(++num_insert_keys_requested));
	}

	private class insert_keys_permutator_executor
	{		
		Entity e;
		private int[] c;
		private  DatabaseEntry[][] values_as_entries;
		private boolean is_valid;
		private Set<DatabaseEntry> keys;
		private insert_keys_permutator_executor(Entity e,Set<DatabaseEntry> keys) throws DatabaseException
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
				if(f.isArray())
				{
					List<Object> vals = (List<Object>)e.getAttribute(f.getName());
					//System.out.println("HIT ARRAY FIELD "+f.getName()+" WITH VALUE "+vals);
					if(vals == null || vals.size() == 0)
					{
						values_as_entries[i] 	= new DatabaseEntry[1];
						values_as_entries[i][0] = new DatabaseEntry();
						FieldBinding.valueToEntry(f.getBaseType(), null,values_as_entries[i][0]);		
					}
					else
					{
						int vs = vals.size();
						values_as_entries[i] = new DatabaseEntry[vs];
						for(int ii = 0; ii < vs;ii++)
						{
							values_as_entries[i][ii] = new DatabaseEntry();
							FieldBinding.valueToEntry(f.getBaseType(), vals.get(ii),values_as_entries[i][ii]);
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
					fos.writeFast(dbe.getData(),dbe.getOffset(),dbe.getSize());
				}
				d.setData(fos.toByteArray(),fos.getBufferOffset(),fos.getBufferLength());
				//System.out.println("ADDING MULTI ARRAY KEY "+new String(d.getData()));
				keys.add(d);
				tick(c.length-1);
			}while(is_valid);
		}
	}


	public List<DatabaseEntry> getQueryKeys(List<Object> vals) throws DatabaseException
	{
		query_keys_permutator_executor e = new query_keys_permutator_executor(vals);
		return e.exec();
	}
	

	private class query_keys_permutator_executor
	{		
		List<Object> values;
		private  int[] c;
		private  DatabaseEntry[][] values_as_entries;
		private boolean is_valid;
		
		private query_keys_permutator_executor(List<Object> values) throws DatabaseException
		{
			this.values 		   	= values;
			int s 				   	= values.size();
			this.c 				   	= new int[s];
			this.values_as_entries 	= new DatabaseEntry[s][];
			this.is_valid 			= true;
			
			for(int i = 0; i < values.size();i++)
			{
				FieldDefinition f = getFields().get(i);
				if(f.isArray())
				{
				
					List<Object> vals = (List<Object>)values.get(i);
					if(vals == null || vals.size() == 0)
					{
						values_as_entries[i] 	= new DatabaseEntry[1];
						values_as_entries[i][0] = new DatabaseEntry();
						FieldBinding.valueToEntry(f.getBaseType(), null,values_as_entries[i][0]);		
					}
					else
					{
						int vs = vals.size();
						values_as_entries[i] = new DatabaseEntry[vs];
						for(int ii = 0; ii < vs;ii++)
						{
							values_as_entries[i][ii] = new DatabaseEntry();
							FieldBinding.valueToEntry(f.getBaseType(), vals.get(ii),values_as_entries[i][ii]);
						}
					}
				}
				else
				{
					Object val = values.get(i);
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
				
		private List<DatabaseEntry> exec()
		{
			List<DatabaseEntry> keys = new ArrayList<DatabaseEntry>();
			do
			{
				FastOutputStream fos = new FastOutputStream();
				DatabaseEntry d 	 = new DatabaseEntry();
				for(int i = 0; i < c.length;i++)
				{
					DatabaseEntry dbe   = values_as_entries[i][c[i]];
					fos.writeFast(dbe.getData(),dbe.getOffset(),dbe.getSize());
				}
				d.setData(fos.toByteArray(),fos.getBufferOffset(),fos.getBufferLength());
				keys.add(d);
				//System.out.println("ADDING QUERY KEY "+new String(d.getData()));
				tick(c.length-1);
			}while(is_valid);
			return keys;
		}
	}
	
	public static EntityIndexDefinition getDefinition()
	{
		EntityIndexDefinition definition = new EntityIndexDefinition();
		definition.setName(NAME);
		definition.setIsMultiField(true);
		definition.setDescription(" Creates an index on multiple fields in an entity"+
								  " ordered by the first field. The entities can "+
								  " be queried for equality accross index member fields."+
								  " i.e. First Name=\"Dave\" Last Name=\"Rogers\"");

		//we dont do this anymore but this is how you add settable paramters to an index definition
		//FieldDefinition array_index_strategy = new FieldDefinition();
		//array_index_strategy.setName(KEY_PARAM_ARRAY_INDEXING_STRATEGY);
		//array_index_strategy.setType(Types.TYPE_INT);
		//array_index_strategy.setDescription("0=Index Arrays For Equality," +
		//									"1=Index Arrays for Containment");
		//definition.addAttribute(array_index_strategy);
		return definition;
	}


}
