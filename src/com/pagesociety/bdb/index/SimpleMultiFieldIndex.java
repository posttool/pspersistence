package com.pagesociety.bdb.index;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.pagesociety.bdb.BDBSecondaryIndex;
import com.pagesociety.bdb.binding.FieldBinding;
import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.pagesociety.persistence.Query;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;

public class SimpleMultiFieldIndex extends AbstractMultiFieldIndex
{

	public static final String NAME = SimpleMultiFieldIndex.class.getSimpleName();


	public SimpleMultiFieldIndex()
	{
		super(BDBSecondaryIndex.TYPE_NORMAL_INDEX);
	}
	
	public void init(Map<String,String> attributes)
	{

	}
	
	public void getInsertKeys(Entity e,Set<DatabaseEntry> result) throws DatabaseException
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
		

		return definition;
	}


}
