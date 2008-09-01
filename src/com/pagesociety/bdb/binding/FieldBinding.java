package com.pagesociety.bdb.binding;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.pagesociety.bdb.BDBPrimaryIndex;

import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.EntityDefinition;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.pagesociety.persistence.Types;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;

public class FieldBinding
{
	private static final Logger logger = Logger.getLogger(FieldBinding.class);
	
	public static final int NULL_FLAG_VAL_NULL 		= (int)'!';
	public static final int NULL_FLAG_VAL_NOT_NULL  = (int)'#';
	public static void writeValueToTuple(FieldDefinition attribute, Object value, TupleOutput to) throws DatabaseException
	{
			if (attribute.isArray())
			{
				List<?> values = (List<?>) value;
				if (values == null)
				{
					to.writeFast(NULL_FLAG_VAL_NULL);//array is null
					return;
				}
				to.writeFast(NULL_FLAG_VAL_NOT_NULL);//array is not null
				
				int s = values.size();
				to.writeInt(s);
				for (int i = 0;i < s;i++)
					FieldBinding.doWriteValueToTuple(attribute.getBaseType(), values.get(i), to);

			}
			else
			{
				//null and non null handling is in writevaluetotuple in this case
				FieldBinding.doWriteValueToTuple(attribute.getBaseType(), value, to);
			}
	}
	
	public static Object readValueFromTuple(TupleInput ti, FieldDefinition field)
	{
		if (field.isArray())
		{
			int null_flag = ti.readFast();
			if(null_flag == NULL_FLAG_VAL_NULL )
				return null;
			
			int len = ti.readInt();
			List<Object> vals = new ArrayList<Object>(len);
			for (int j = 0; j < len; j++)
				vals.add(doReadValueFromTuple(ti,field.getBaseType()));

			return vals;
		}
		else
		{
			return doReadValueFromTuple(ti,field.getType());
		}
	}



	public static void doWriteValueToTuple(int type, Object val, TupleOutput to) throws DatabaseException
	{

		if(val == null)
		{
			to.writeFast(NULL_FLAG_VAL_NULL);
			return;
		}

		to.writeFast(NULL_FLAG_VAL_NOT_NULL);
		switch (type)
		{
			//case Types.TYPE_UNDEFINED:
			//	break;
			case Types.TYPE_BOOLEAN:
				to.writeBoolean((Boolean) val);
				break;
			case Types.TYPE_INT:
				to.writeInt((Integer) val);
				break;
			case Types.TYPE_LONG:
				to.writeLong((Long) val);
				break;
			case Types.TYPE_DOUBLE:
				to.writeSortedDouble((Double) val);
				break;
			case Types.TYPE_FLOAT:
				to.writeSortedFloat((Float) val);
				break;
			case Types.TYPE_STRING:
			case Types.TYPE_TEXT:
				to.writeString((String) val);
				break;
			case Types.TYPE_DATE:
				Date d = (Date) val;
				to.writeLong(d.getTime());
				break;
			case Types.TYPE_BLOB:
				byte[] b = (byte[]) val;
				to.writeInt(b.length);
				to.writeFast(b);
				break;
			case Types.TYPE_REFERENCE:
				long id;
				Entity e = (Entity) val;
				if((id = e.getId()) == Entity.UNDEFINED)
					throw new DatabaseException("CANT SERIALIZE UNDEFINED. REF SAVE REFERENCE " + e);
				//TODO: give entity definitions ids and make this a reference to that and keep
				//a map of entity_def_ids to entity_definitions
				to.writeString(e.getEntityDefinition().getName());
				to.writeLong(id);
				break;
			default:
				try{
					throw new Exception();
				}catch(Exception ee)
				{
					ee.printStackTrace();
				}
				System.err.println("UNKNOWN type " + type);
				break;
		}
	}

	private static Object doReadValueFromTuple(TupleInput ti,int type)
	{
		int null_flag = ti.readFast();
		if(null_flag == NULL_FLAG_VAL_NULL )
			return null;

		switch (type)
		{
			case Types.TYPE_UNDEFINED:
				return null;
			case Types.TYPE_BOOLEAN:
				return ti.readBoolean();
			case Types.TYPE_INT:
				return ti.readInt();
			case Types.TYPE_LONG:
				return ti.readLong();
			case Types.TYPE_DOUBLE:
				return ti.readSortedDouble();
			case Types.TYPE_FLOAT:
				return ti.readSortedFloat();
			case Types.TYPE_STRING:
			case Types.TYPE_TEXT:
				return ti.readString();
			case Types.TYPE_DATE:
				long l = ti.readLong();
				return new Date(l);
			case Types.TYPE_BLOB:
				int len = ti.readInt();
				byte[] b = new byte[len];
				ti.readFast(b);
				return b;
			case Types.TYPE_REFERENCE:
				String entity_type = ti.readString();//this should be some sort of class id at some point...not a string//
				long id = ti.readLong();
				Entity e = Entity.createInstance();
				//TODO: primary index map should become static instance and be like a
				//directory service
				e.setEntityDefinition(_primary_index_map.get(entity_type).getEntityDefinition());
				e.setType(entity_type);
				e.setId(id);
				return e;
			default:
				try{
					throw new Exception();
				}catch(Exception ee)
				{
					ee.printStackTrace();
				}
				System.err.println("UNKNOWN type " +type);
				break;
			}
		return null;

	}
	
	private static final int BOOLEAN_SIZE 	= 1;
	private static final int INTEGER_SIZE 	= 4;
	private static final int FLOAT_SIZE   	= 4;
	private static final int LONG_SIZE 	  	= 8;
	private static final int DOUBLE_SIZE  	= 8;
	private static final int DATE_SIZE	  	= 8;
	private static final int REFERENCE_SIZE	= 8;
	
	private static int get_tuple_size(int type)
	{
		switch (type)
		{
			case Types.TYPE_BOOLEAN:
				return BOOLEAN_SIZE;
			case Types.TYPE_INT:
				return INTEGER_SIZE;
			case Types.TYPE_LONG:
				return LONG_SIZE;
			case Types.TYPE_DOUBLE:
				return DOUBLE_SIZE;
			case Types.TYPE_FLOAT:
				return FLOAT_SIZE;
			case Types.TYPE_STRING:
				return 128;//this is the default initial size
			case Types.TYPE_TEXT:
				return 2048;//this is the default initial size
			case Types.TYPE_DATE:
				return DATE_SIZE;
			case Types.TYPE_BLOB:
				return 512;
			case Types.TYPE_REFERENCE:
				return REFERENCE_SIZE;
			default:
				try{
					throw new Exception();
				}catch(Exception ee)
				{
					ee.printStackTrace();
				}
				System.err.println("UNKNOWN type " + type);
				return -1;
		}
	}
	
	public static boolean valueToEntry(int type, Object val, DatabaseEntry data) throws DatabaseException
	{
		TupleOutput to = new TupleOutput(new byte[get_tuple_size(type)]);
		doWriteValueToTuple(type, val, to);
		data.setData(to.getBufferBytes(), to.getBufferOffset(), to.getBufferLength());
		return true;
	}
		
	public static Object entryToValue(int type, DatabaseEntry data)
	{
		TupleInput ti = new TupleInput(new byte[get_tuple_size(type)]);
		return doReadValueFromTuple(ti, type);
	}

	public static DatabaseEntry minValAsEntry(int type) throws DatabaseException
	{
		DatabaseEntry d = new DatabaseEntry();
		switch(type)
		{
			case Types.TYPE_BOOLEAN:
			case Types.TYPE_INT:
			case Types.TYPE_LONG:
			case Types.TYPE_DOUBLE:
			case Types.TYPE_FLOAT:
			case Types.TYPE_STRING:	
			case Types.TYPE_TEXT:	
			case Types.TYPE_DATE:
			case Types.TYPE_REFERENCE:
				valueToEntry(type, null, d);
				break;
			default:
			{
				throw new DatabaseException("UNKNOWN TYPE FOR MIN SHOULD NOT BE HERE YO");
			}
		}
		return d;
	}

	
	public static DatabaseEntry maxValAsEntry(int type)throws DatabaseException
	{
		DatabaseEntry d = new DatabaseEntry();
		switch(type)
		{
			case Types.TYPE_BOOLEAN:
				 valueToEntry(type,true,d);
				break;
			case Types.TYPE_INT:
				valueToEntry(type,Integer.MAX_VALUE,d);
				break;
			case Types.TYPE_DATE:
			case Types.TYPE_LONG:
				valueToEntry(Types.TYPE_LONG,Long.MAX_VALUE,d);
				break;
			case Types.TYPE_DOUBLE:
				valueToEntry(type,Double.MAX_VALUE,d);
				break;
			case Types.TYPE_FLOAT:
				valueToEntry(type,Float.MAX_VALUE,d);
				break;
			case Types.TYPE_STRING:	
			case Types.TYPE_TEXT:	
				TupleOutput to = new TupleOutput(new byte[8]);
				writeMaxVal(type, to);
				d.setData(to.getBufferBytes(), to.getBufferOffset(), to.getBufferLength());
				break;
			case Types.TYPE_REFERENCE:
				valueToEntry(type,MAX_ENTITY,d);
				break;
			default:
			{
				throw new DatabaseException("UNKNOWN TYPE FOR MIN SHOULD NOT BE HERE YO");
			}
		}
		return d;
	}
	
	public static void writeMinVal(int type,TupleOutput to) throws DatabaseException
	{
		switch(type)
		{
			case Types.TYPE_BOOLEAN:
			case Types.TYPE_INT:
			case Types.TYPE_LONG:
			case Types.TYPE_DOUBLE:
			case Types.TYPE_FLOAT:
			case Types.TYPE_STRING:
			case Types.TYPE_TEXT:
			case Types.TYPE_DATE:
			case Types.TYPE_REFERENCE:
				doWriteValueToTuple(type, null, to);
				break;
			default:
			{
				try{
					throw new Exception("SHOULD NOT BE HERE YO");
				}catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void writeMaxVal(int type,TupleOutput to) throws DatabaseException
	{
		switch(type)
		{
		case Types.TYPE_BOOLEAN:
			doWriteValueToTuple(type, true, to);
			break;
		case Types.TYPE_INT:
			doWriteValueToTuple(type, Integer.MAX_VALUE, to);
			break;
		case Types.TYPE_DATE:
		case Types.TYPE_LONG:
			doWriteValueToTuple(Types.TYPE_LONG, Long.MAX_VALUE, to);
			break;
		case Types.TYPE_DOUBLE:
			doWriteValueToTuple(type, Double.MAX_VALUE, to);
			break;
		case Types.TYPE_FLOAT:
			doWriteValueToTuple(type, Float.MAX_VALUE, to);
			break;
		case Types.TYPE_STRING:
		case Types.TYPE_TEXT:
			to.writeFast(NULL_FLAG_VAL_NOT_NULL);
			to.writeString((String)null);
	//		doWriteValueToTuple(type, MAX_STRING, to);
			break;
		case Types.TYPE_REFERENCE:
			doWriteValueToTuple(type, MAX_ENTITY, to);
			break;
		default:
		{
			try{
				throw new Exception("SHOULD NOT BE HERE YO");
			}catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		}
	}
	
	//TODO
	//we really need to think about this
	//multiple stores currently use the same static methods in the same vm
	//should the be an instance of field binding per store?
	//or is the a reference registry that the store & field binding both interact with?
	private static Map<String,BDBPrimaryIndex> _primary_index_map;
	public static void initWithPrimaryIndexMap(Map<String,BDBPrimaryIndex> map) throws PersistenceException
	{
		if (_primary_index_map == null)
		{
			_primary_index_map = new HashMap<String,BDBPrimaryIndex>();
		}
		_primary_index_map.putAll(map);
	
	}
	
	public static void addToPrimaryIndexMap(String name, BDBPrimaryIndex index)
	{
		if (_primary_index_map == null)
		{
			_primary_index_map = new HashMap<String,BDBPrimaryIndex>();
		}
		_primary_index_map.put(name, index);
	}
		



	
	private static final String MIN_STRING = new String(new byte[]{});
	private static final String MAX_STRING = null;//new String(new byte[]{(byte)0xFF,(byte)0x00});
	static final int NULL_STRING_UTF_VALUE = ((byte) 0xFF);
	private static final EntityDefinition MAX_ENTITY_DEF = new EntityDefinition(MAX_STRING); 
	private static Entity MAX_ENTITY;
	static{
		MAX_ENTITY = MAX_ENTITY_DEF.createInstance();
		MAX_ENTITY.setId(Long.MAX_VALUE);
	}

	
}
