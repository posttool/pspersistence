package com.pagesociety.bdb.binding;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.pagesociety.bdb.BDBStore;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.Types;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.db.DatabaseEntry;

public class EntityDefinitionBinding extends TupleBinding
{
	private static final Logger logger = Logger.getLogger(EntityDefinitionBinding.class);

	
	public void objectToEntry(Object object, TupleOutput to)
	{
		EntityDefinition entity_def = (EntityDefinition) object;
		to.writeString(entity_def.getName());
		ArrayList<FieldDefinition> fields = entity_def.getFields();
		to.writeInt(fields.size());
		for (int i = 0; i < fields.size(); i++)
		{
			FieldDefinition f = fields.get(i);
			to.writeString(f.getName());
			to.writeInt(f.getType());
			if (f.getBaseType() == Types.TYPE_REFERENCE)
				to.writeString(f.getReferenceType());
		}

	}

	/* TODO:  figure out how to get rid of BDBStore here and revert back to old version*/
	public Object entryToObject(BDBStore store,DatabaseEntry data)
	{
	    TupleInput ti =  new TupleInput(data.getData(), data.getOffset(),data.getSize());
		EntityDefinition entity_def = new EntityDefinition(ti.readString());
		int size = ti.readInt();
		for (int i = 0; i < size; i++)
		{
			FieldDefinition f = new FieldDefinition(ti.readString(), ti.readInt(),null);
			if (f.getBaseType() == Types.TYPE_REFERENCE)
				f.setReferenceType(ti.readString());
			entity_def.addField(f);
		}

		return entity_def;
	}
	
	/*TODO: we dont eve use this part of the tuple interface */
	public Object entryToObject(TupleInput ti)
	{
		System.err.println("EntityDefBinding: WRONG ENTRY TO OBJECT!!!");
		return null;
	}
	
}