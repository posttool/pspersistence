package com.pagesociety.bdb.index;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.pagesociety.bdb.BDBSecondaryIndex;
import com.pagesociety.bdb.binding.FieldBinding;
import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.Query;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;


public class SimpleSingleFieldIndex extends AbstractSingleFieldIndex
{
	public static final String NAME 			  = SimpleSingleFieldIndex.class.getSimpleName();

	
	public SimpleSingleFieldIndex()
	{
		super(BDBSecondaryIndex.TYPE_NORMAL_INDEX);
	}
	
	
	public void init(Map<String,Object> attributes)
	{
		
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
			Object val = e.getAttribute(name);
			result.add(getQueryKey(val));
		}
	}

	public void get_deep_insert_keys(Entity e,String[] ref_path,List<FieldDefinition>[] ref_path_types,int offset,Set<DatabaseEntry> result) throws DatabaseException
	{
		if(offset == ref_path.length-1)
		{
			Object val = e.getAttribute(ref_path[offset]);
			DatabaseEntry entry = getQueryKey(val);
			//System.out.println("!!!!!!!!!!!!!!!!!!INSERTING INDEX ENTRY "+new String(entry.getData(),0,entry.getSize()));
			result.add(entry);
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
	
	public DatabaseEntry getQueryKey(Object val) throws DatabaseException
	{

		if(val == Query.VAL_MIN)
		{
			return FieldBinding.minValAsEntry(field.getBaseType());
		}
		else if (val == Query.VAL_MAX)
		{
			return FieldBinding.maxValAsEntry(field.getBaseType());
		}

		DatabaseEntry d = new DatabaseEntry();
		if (field.isArray())
		{
			List<Comparable<Object>> array = (List<Comparable<Object>>)val;
			if(array == null)
			{
				d = new DatabaseEntry();
				FieldBinding.valueToEntry(field.getBaseType(), null, d);
			}
			else
				d = get_equality_key_for_array(array);
		}
		else
		{
			FieldBinding.valueToEntry(field.getBaseType(), val, d);
		}
		return d;
	}
	
	/* sort the arrays so we can determine set equality */
	/* for proper set equality the order of the elements doesnt */
	/* matter */
	private DatabaseEntry get_equality_key_for_array(List<Comparable<Object>> array) throws DatabaseException
	{
		Collections.sort(array);
		int s = array.size();
		TupleOutput to = new TupleOutput();
		int type = field.getBaseType();
		
		for(int i=0;i < s;i++)
			FieldBinding.doWriteValueToTuple(type, array.get(i), to);

		return new DatabaseEntry(to.getBufferBytes());
	}

	

	
	/* any index must!!! implement this method for now. this is how we get the meta information about what
	 * attributes it takes without having to construct an instance. See EntityDefinition*/
	public static EntityIndexDefinition getDefinition()
	{
		EntityIndexDefinition definition = new EntityIndexDefinition();
		definition.setName(NAME);
		definition.setIsMultiField(false);
		definition.setDescription("Creates an index on a single field of an entity type"+
									" allowing you to look up a particular instance of that" +
									" entity type by the specified field.Simple query support" +
									" using =,>,<,>=,<=,STARTSWITH(String fields only),BETWEEN operators.");
		
		//NOTE there are no attributes here
		//but something like this: 
		//FieldDef use_lower_case = new FieldDef();
		//use_lower_case.setName("use lower case");
		//use_lower_case.setType(Types.TYPE_BOOLEAN);
		//definition.addAttribute(use_lower_case);

		return definition;
	}


}
