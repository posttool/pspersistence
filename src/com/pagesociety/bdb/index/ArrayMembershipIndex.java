package com.pagesociety.bdb.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pagesociety.bdb.BDBSecondaryIndex;
import com.pagesociety.bdb.binding.FieldBinding;
import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;

public class ArrayMembershipIndex extends AbstractSingleFieldIndex
{

	public static final String NAME 				= ArrayMembershipIndex.class.getSimpleName();
	
	public ArrayMembershipIndex()
	{
		super(BDBSecondaryIndex.TYPE_SET_INDEX);
	}
	
	public void init(Map<String,Object> attributes)
	{
		
	}

	public void validateField(FieldDefinition field) throws PersistenceException
	{
		if(!field.isArray())
			throw new PersistenceException(getDefinition().getName()+" CAN ONLY INDEX FIELDS OF TYPE ARRAY.FIELD "+field.getName()+" IS NOT OF TYPE ARRAY");
	}
	
	@SuppressWarnings("unchecked")
	public void getInsertKeys(Entity e,Set<DatabaseEntry> result) throws DatabaseException
	{
		
		String name = field.getName();
		if(isDeepIndex())
		{
			//System.out.println("GETTING DEEP INSERT KEYS FOR "+e);
			String[] ref_path 					   = (String[])getAttribute(ATTRIBUTE_DEEP_INDEX_PATH_LOCATOR_PREFIX+"0");
			List<FieldDefinition>[] ref_path_types = (List<FieldDefinition>[])getAttribute(ATTRIBUTE_DEEP_INDEX_PATH_TYPE_LOCATOR_PREFIX+"0");
			if(ref_path == null || ref_path_types == null)
				throw new DatabaseException("BAD DEEP INDEX META INFO! WTF");
			get_deep_insert_keys(e, ref_path, ref_path_types, 0, result);
		}
		else
		{
		
			/* we know the field is of TYPE_ARRAY */
			List<?> values = (List<?>) e.getAttribute(field.getName());
			DatabaseEntry d;
			if(values == null)
			{
				d = new DatabaseEntry();
				FieldBinding.valueToEntry(field.getBaseType(), null, d);
				result.add(d);
			}
			else
			{
				int s = values.size();
				for(int i = 0; i < s;i++)
				{
					Object val = values.get(i);
					d = new DatabaseEntry();
					FieldBinding.valueToEntry(field.getBaseType(), val, d);
					result.add(d);
				}
			}
		}
	}
	
	public void get_deep_insert_keys(Entity e,String[] ref_path,List<FieldDefinition>[] ref_path_types,int offset,Set<DatabaseEntry> result) throws DatabaseException
	{
		if(offset == ref_path.length-1)
		{
			List<Object> 		vals 	= (List<Object>)e.getAttribute(ref_path[offset]);
			List<DatabaseEntry> entries = getQueryKeys(vals);
			int s=entries.size();
			for(int i=0;i < s;i++)
			{
				//System.out.println("!!!!!!!!!!!!!!!!!!INSERTING ARRAY INDEX ENTRY "+new String(entries.get(i).getData(),0,entries.get(i).getSize()));
				result.add(entries.get(i));				
			}

			return;
		}
		
		FieldDefinition ref_type = ref_path_types[offset].get(0);
		if(ref_type.isArray())
		{
			List<Entity> vals = (List<Entity>)e.getAttribute(ref_path[offset]);

			if(vals == null)//abandon the path
				return;//TODO: SHOULD WE INSERT A NULL HERE FOR THIS DUDE IF ONE DONT EXIST??//
					   //....see BDBSecondaryIndex.deleteIndexEntry for related note
					   //we should be inserting something at some point
			int s = vals.size();
			for(int i = 0;i < s;i++)
				get_deep_insert_keys(vals.get(i), ref_path, ref_path_types, offset+1,result);
		}
		else
		{
			Entity val = (Entity)e.getAttribute(ref_path[offset]);
			//System.out.println("TRYING TO GET "+e.getType()+" ATT "+ref_path[offset]+" IT IS "+val);
			if(val == null)//abandon the path
				return;//TODO: SHOULD WE INSERT A NULL HERE FOR THIS DUDE IF ONE DONT EXIST??//
			get_deep_insert_keys(val, ref_path, ref_path_types, offset+1,result);
		}
	}

	
	public List<DatabaseEntry> getQueryKeys(List<Object> values) throws DatabaseException
	{
	
		List<DatabaseEntry> ret;
		DatabaseEntry d; 
		if(values == null)
		{
			d = new DatabaseEntry();
			FieldBinding.valueToEntry(field.getBaseType(), null, d);
			ret = new ArrayList<DatabaseEntry>(1);
			ret.add(d);
			return ret;
		}
		else
		{
			int s = values.size();
			ret = new ArrayList<DatabaseEntry>(s);
			for(int i = 0; i < s;i++)
			{
				Object val = values.get(i);
				d = new DatabaseEntry();
				FieldBinding.valueToEntry(field.getBaseType(), val, d);
				ret.add(d);
			}
		}
		return ret;
	}	

	
	
	/* any index must!!! implement this method for now. this is how we get the meta information about what
	 * attributes it takes without having to construct an instance. See EntityDefinition*/
	public static EntityIndexDefinition getDefinition()
	{
		EntityIndexDefinition definition = new EntityIndexDefinition();
		definition.setName(NAME);
		definition.setIsMultiField(false);
		definition.setDescription("Creates an index on the members of an array field which you can query "+
									" by subset.i.e.  SET_CONTAINS_ALL {1,3,9}, SET_CONTAINS_ANY {1,3,9} etc.");
		
		//NOTE there are no attributes here
		//but something like this: 
		//FieldDef use_lower_case = new FieldDef();
		//use_lower_case.setName("use lower case");
		//use_lower_case.setType(Types.TYPE_BOOLEAN);
		//definition.addAttribute(use_lower_case);

		return definition;
	}


}
