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
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.db.Cursor;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;


public abstract class AbstractMultiFieldIndex extends BDBSecondaryIndex
{
	protected List<FieldDefinition> fields;

	public AbstractMultiFieldIndex(int type)
	{
		super();
		this.type = type;
	}
	/*set the runtime attributes of this indexer */
	public void setup(BDBPrimaryIndex primary_index,EntityIndex index) throws PersistenceException
	{
		super.setup(primary_index,index);
		fields = super.getFields();
		init(index.getAttributes());
	}
	
	
	/* this is called when a field is deleted from a primary index. perhgaps the index is meaningless
	 * at that point and wants to have itself deleted by the caller.
	 */
	public boolean invalidatedByFieldDelete(FieldDefinition f)
	{
		String fieldname = f.getName();
		int s = fields.size();
		for(int i = 0; i < s;i++)
		{
			if(fieldname.equals(fields.get(i).getName()))
				return true;
		}
		return false;
	}
	
	/* this gets called on insert into primary*/
	public boolean indexesField(String fieldname)
	{
		int s = fields.size();
		for(int i = 0; i < s;i++)
		{
			if(fieldname.equals(fields.get(i).getName()))
				return true;
		}
		return false;	
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
		List<FieldDefinition> fields = getFields();
		try
		{
			Cursor cursor = db_handle.openCursor(null, null);
			OperationStatus op_stat = cursor.getFirst(key, pkey, LockMode.DEFAULT);
			while (op_stat == OperationStatus.SUCCESS)
			{
				Object[] multi_key = new Object[fields.size()];
				TupleInput ti = new TupleInput(key.getData());
	/*TODO: this needs to be cleaned up for get distinct keys for multi depending on set
	 * containment.maybe get distinct is unimplemented for set containment stuff.
	 */
				for(int i = 0; i<multi_key.length;i++)
				{
					//need to check if fields i is an array and look at array indexing strategy
					multi_key[i] = FieldBinding.readValueFromTuple(ti, fields.get(i));
				}
				es.add(multi_key);
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

	public abstract void init(Map<String,Object> attributes)  throws PersistenceException;
	public abstract void getInsertKeys(Entity e,Set<DatabaseEntry> result) throws DatabaseException;

}
