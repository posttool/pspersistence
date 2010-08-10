package com.pagesociety.bdb.index;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.pagesociety.bdb.BDBPrimaryIndex;
import com.pagesociety.bdb.BDBSecondaryIndex;
import com.pagesociety.bdb.binding.FieldBinding;
import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityIndex;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.sleepycat.db.Cursor;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;


public abstract class AbstractSingleFieldIndex extends BDBSecondaryIndex
{

	protected FieldDefinition field;

	public AbstractSingleFieldIndex(int type)
	{
		super();
		this.type = type;
	}
	
	/*set the runtime attributes of this indexer */
	public void setup(BDBPrimaryIndex primary_index,EntityIndex index) throws PersistenceException
	{
		super.setup(primary_index,index);
		field = super.getFields().get(0);
		init(index.getAttributes());
	}

	
	/* this is called when a field is deleted from a primary index. perhgaps the index is meaningless
	 * at that point and wants to have itself deleted by the caller.
	 */
	public boolean invalidatedByFieldDelete(FieldDefinition f)
	{
		return(field.getName().equals(f.getName()));
	}
	
	/* this gets called on insert into primary*/
	public boolean indexesField(String fieldname)
	{
		return(field.getName().equals(fieldname));
	}
	
	
	public void fieldChangedName(String old_name,String new_name) 
	{
		/*we dont care because our db filename isnt relying on fielname*/
	}

	public List<Object> getDistinctKeys() throws PersistenceException
	{
		List<Object> es 	= new ArrayList<Object>();
		DatabaseEntry key  	= new DatabaseEntry();
		DatabaseEntry pkey 	= new DatabaseEntry();

		try
		{
			Cursor cursor = db_handle.openCursor(null, null);
			OperationStatus op_stat = cursor.getFirst(key, pkey, LockMode.DEFAULT);
			while (op_stat == OperationStatus.SUCCESS)
			{
				es.add(FieldBinding.entryToValue(field.getBaseType(), key));
				op_stat = cursor.getNextNoDup(key, pkey, LockMode.DEFAULT);
			}
			cursor.close();
		}
		catch (DatabaseException de)
		{
			de.printStackTrace();
			throw new PersistenceException("get distinct failed " + de.getMessage());
		}
		return es;
	}
	
	public void validateFields(List<FieldDefinition> fields) throws PersistenceException
	{
		validateField(fields.get(0));
	}
	
	public void validateField(FieldDefinition field) throws PersistenceException
	{
		/* subclasses can override to constrain field types */
	}
	
	
	public abstract void init(Map<String,Object> attributes) throws PersistenceException;
	public abstract void getInsertKeys(Entity entity,Set<DatabaseEntry> result) throws DatabaseException;

}
