package com.pagesociety.bdb.binding;


import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;


import com.pagesociety.bdb.BDBEntityDefinitionProvider;
import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.FieldDefinition;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;

public class EntityBinding
{

	private static final Logger logger = Logger.getLogger(EntityBinding.class);
	private BDBEntityDefinitionProvider _def_provider;
	
	public EntityBinding()
	{
	}
	
	public void setEntityDefinitionProvider(BDBEntityDefinitionProvider provider)
	{
		_def_provider = provider;
	}

	public void entityToEntry(Entity entity, DatabaseEntry entry) throws DatabaseException
	{
		TupleOutput to = new TupleOutput();
		if(entity == null)
			to.writeFast(FieldBinding.NULL_FLAG_VAL_NULL);
		else
		{
			to.writeFast(FieldBinding.NULL_FLAG_VAL_NOT_NULL);
//			EntityDefinition ed = entity.getEntityDefinition();
			EntityDefinition ed = _def_provider.provideEntityDefinition(entity.getType());
			List<FieldDefinition> fields = ed.getFields();
			for (int i = 0; i < fields.size(); i++)
			{
				FieldDefinition field = fields.get(i);
				FieldBinding.writeValueToTuple(field, entity.getAttribute(field.getName()), to);
			}
		}	
		entry.setData(to.toByteArray());
	}

	public void entityToEntry(EntityDefinition ed,Entity entity, DatabaseEntry entry) throws DatabaseException
	{
		TupleOutput to = new TupleOutput();
		if(entity == null)
			to.writeFast(FieldBinding.NULL_FLAG_VAL_NULL);
		else
		{
			to.writeFast(FieldBinding.NULL_FLAG_VAL_NOT_NULL);
//			EntityDefinition ed = entity.getEntityDefinition();
			List<FieldDefinition> fields = ed.getFields();
			for (int i = 0; i < fields.size(); i++)
			{
				FieldDefinition field = fields.get(i);
				FieldBinding.writeValueToTuple(field, entity.getAttribute(field.getName()), to);
			}
		}	
		entry.setData(to.toByteArray());
	}




	public Entity entryToEntity(EntityDefinition ed, DatabaseEntry entry)
	{
		
		TupleInput ti = new TupleInput(entry.getData(), entry.getOffset(), entry.getSize());
		int null_flag = ti.readFast();
		if(null_flag == FieldBinding.NULL_FLAG_VAL_NULL)
			return null;
		
		Entity entity = ed.createInstance();
		List<FieldDefinition> fields = ed.getFields();
		Map<String,Object> attr = entity.getAttributes();
		
		FieldDefinition field;
		String fieldname;
		Object val;

		for (int i = 0; i < fields.size(); i++)
		{
			field 	  = fields.get(i);
			fieldname = field.getName();
			val = FieldBinding.readValueFromTuple(ti, field);
			
			/* we set the attribute directly instead of through setAttribute so the object
			 * isn't dirtied*/
			attr.put(fieldname, val);
			
		}
		return entity;
	}

	public Entity getEntitySetId(EntityDefinition def, DatabaseEntry id, DatabaseEntry entry)
	{
		Entity entity = entryToEntity(def, entry);
		if(entity == null)
			return null;
		
		entity.setId((Long)LongBinding.entryToLong(id));
		return entity;
	}
}
