package com.pagesociety.bdb.binding;

import org.apache.log4j.Logger;

import com.pagesociety.persistence.EntityRelationshipDefinition;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.db.DatabaseEntry;

public class EntityRelationshipBinding 
{
	private static final Logger logger = Logger.getLogger(EntityRelationshipBinding.class);

	public void objectToEntry(Object object, DatabaseEntry d)
	{

		TupleOutput to = new TupleOutput(d.getData());
		EntityRelationshipDefinition entity_relationship = (EntityRelationshipDefinition) object;
		to.writeString(entity_relationship.getOriginatingEntity());
		to.writeString(entity_relationship.getOriginatingEntityField());
		to.writeInt(entity_relationship.getType());
		to.writeString(entity_relationship.getTargetEntity());
		to.writeString(entity_relationship.getTargetEntityField());
	}

	public Object entryToObject(DatabaseEntry data)
	{
		 TupleInput ti =  new TupleInput(data.getData(), data.getOffset(),data.getSize());
		 EntityRelationshipDefinition entity_relationship = new EntityRelationshipDefinition(ti.readString(),
				  														ti.readString(),
				  														ti.readInt(),
				  														ti.readString(),
				  														ti.readString());
		return entity_relationship;
	}
}