package com.pagesociety.bdb.binding;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.pagesociety.bdb.BDBStore;
import com.pagesociety.bdb.index.EntityIndexDefinition;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.EntityIndex;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;

public class EntitySecondaryIndexBinding
{
	private static final Logger logger = Logger.getLogger(EntitySecondaryIndexBinding.class);
	public DatabaseEntry objectToEntry(Object object) throws DatabaseException
	{

		TupleOutput to = new TupleOutput();
		EntityIndex entity_index = (EntityIndex) object;
		to.writeString(entity_index.getName());
		to.writeInt(entity_index.getEntityIndexType());
		to.writeString(entity_index.getEntity());
		
		List<FieldDefinition> fields = entity_index.getFields();
		int s = fields.size();
		to.writeInt(s);
		for(int i = 0;i < s;i++ )
		{
			logger.debug("WRITING FIELD INTO INDEX INSTANCE "+entity_index.getName()+" FIELD NAME IS "+fields.get(i).getName());
			to.writeString(fields.get(i).getName());
		}
		//
		Set<String> keys = entity_index.getAttributes().keySet();
		to.writeInt(keys.size());
		Iterator<String> keyiterator = keys.iterator();
		String attribute;
		String key;
		while (keyiterator.hasNext())
		{
			key = keyiterator.next();
			attribute = (String)entity_index.getAttributes().get(key);
			/* attribute was not set by user so just skip it and let the index
			 * decide how to handle null attributes. this gets into the required vs non
			 * required stuff.*/
			if(attribute == null)
				continue;
			to.writeString(key);
			to.writeString(attribute);
			//FieldBinding.writeValueToTuple(attribute, entity_index.getAttributes().get(key), to);
		}
		return new DatabaseEntry(to.toByteArray());
	}

	public Object entryToObject(BDBStore store, DatabaseEntry data) throws DatabaseException
	{
		TupleInput ti 			= new TupleInput(data.getData());
		String index_name 		= ti.readString();
		int index_type 			= ti.readInt();
		String entity_name 		= ti.readString();
		//String field_name = ti.readString();
		EntityDefinition def;

			def = store.getEntityDefinition(entity_name);
			if(def == null)
				throw new DatabaseException("NO ENTITY DEF FOR "+entity_name);

		EntityIndex esi = new EntityIndex(index_name, index_type);
		esi.setEntity(entity_name);	
		
		/*decode fields*/
		int size = ti.readInt();
		for(int i=0;i<size;i++)
			esi.addField(def.getField(ti.readString()));			

		/*decode runtime attributes*/
		size = ti.readInt();
		for (int i = 0; i < size; i++)
		{
			String att_name = ti.readString();
			String att_val  = ti.readString();
			esi.setAttribute(att_name,att_val);
		}
		return esi;
	}
}
