package com.pagesociety.bdb.binding;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
		to.writeInt(entity_index.getType());
		to.writeString(entity_index.getEntity());
		
		List<FieldDefinition> fields = entity_index.getFields();
		int s = fields.size();
		to.writeInt(s);
		for(int i = 0;i < s;i++ )
		{
			logger.debug("WRITING FIELD INTO INDEX INSTANCE "+entity_index.getName()+" FIELD NAME IS "+fields.get(i).getName());
			to.writeString(fields.get(i).getName());
			to.writeInt(fields.get(i).getType());
			to.writeString(fields.get(i).getReferenceType());
		}
		
        // Serialize to a byte array
        ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
        ObjectOutputStream out;
		try {
			out = new ObjectOutputStream(bos);
	        out.writeObject(entity_index.getAttributes());
	        out.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new DatabaseException("FAILED SERIALIZING "+entity_index.getName()+". BAD BAD.");
		}

		//this is here to be backwards compatible with before we were
		//dealing with attributes as serialized objects...num_attributes//
		int num_attributes = entity_index.getAttributes().keySet().size();
		System.out.println("NUM ATTRIBUTES FOR "+entity_index.getName()+" IS "+num_attributes);
		to.writeInt(num_attributes);
		if(num_attributes != 0)
		{
			// Get the bytes of the serialized object
			to.writeInt(bos.size());
			to.writeFast(bos.toByteArray());
		}
		return new DatabaseEntry(to.toByteArray());
	}

	//TODO:dont need def here anymore
	public Object entryToObject(EntityDefinition def, DatabaseEntry data) throws DatabaseException
	{
		TupleInput ti 			= new TupleInput(data.getData());
		String index_name 		= ti.readString();
		int index_type 			= ti.readInt();
		String entity_name 		= ti.readString();
		//String field_name = ti.readString();
	
		EntityIndex esi = new EntityIndex(index_name, index_type);
		esi.setEntity(entity_name);	

		/*decode fields*/
		int size = ti.readInt();
		for(int i=0;i<size;i++)
		{
			FieldDefinition f = new FieldDefinition(ti.readString(),ti.readInt(),ti.readString());
			esi.addField(f);//def.getField(ti.readString()));			
		}
		/*decode runtime attributes*/
		int num_attributes = ti.readInt();
		if(num_attributes != 0)
		{	
		    // Deserialize from a byte array
			int serialized_size = ti.readInt();
			byte[] buf = new byte[serialized_size];
			ti.readFast(buf,0,serialized_size);
			ObjectInputStream in;
			Map<String,Object> index_attributes; 
			try {
				in = new ObjectInputStream(new ByteArrayInputStream(buf));
				index_attributes = (Map<String,Object>) in.readObject();
				in.close();	
			} catch (Exception e) {
				e.printStackTrace();
				throw new DatabaseException("BARF IN DESERIALIZING INDEX ATTRIBUTES");
			}		
			esi.setAttributes(index_attributes);
		}
		return esi;
	}
}
