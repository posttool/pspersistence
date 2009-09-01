package com.pagesociety.bdb.index;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.pagesociety.bdb.BDBSecondaryIndex;
import com.pagesociety.bdb.BDBStore;
import com.pagesociety.bdb.binding.FieldBinding;
import com.pagesociety.bdb.index.freetext.DefaultStopList;
import com.pagesociety.bdb.index.freetext.FreetextStemmer;
import com.pagesociety.bdb.index.freetext.PorterStemmer;
import com.pagesociety.bdb.index.freetext.StopList;
import com.pagesociety.bdb.index.iterator.IteratorUtil;
import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.PersistenceException;
import com.pagesociety.persistence.Query;
import com.pagesociety.persistence.Types;
import com.sleepycat.bind.tuple.IntegerBinding;
import com.sleepycat.bind.tuple.LongBinding;
import com.sleepycat.bind.tuple.StringBinding;
import com.sleepycat.bind.tuple.TupleOutput;
import com.sleepycat.db.Cursor;
import com.sleepycat.db.DatabaseEntry;
import com.sleepycat.db.DatabaseException;
import com.sleepycat.db.DeadlockException;
import com.sleepycat.db.LockMode;
import com.sleepycat.db.OperationStatus;
import com.sleepycat.db.Transaction;
import com.sleepycat.util.FastOutputStream;

public class MultiFieldFreeTextIndex extends AbstractMultiFieldIndex
{

	public static final String	NAME = MultiFieldFreeTextIndex.class.getSimpleName();
	private static  FreetextStemmer _stemmer  = new PorterStemmer();
	private static  StopList 		_stoplist = new DefaultStopList(null);
	
	private List<FieldDefinition> _string_fields;
	private List<FieldDefinition> _equality_fields;
	
	public MultiFieldFreeTextIndex()
	{
		super(BDBSecondaryIndex.TYPE_FREETEXT_INDEX);

	}
	

	
	
	public void init(Map<String,Object> attributes)
	{
		
		/* this is how we deal with default parameters */
		_string_fields   = new ArrayList<FieldDefinition>();
		_equality_fields = new ArrayList<FieldDefinition>();
		for(int i = 0;i < fields.size();i++)
		{
			FieldDefinition f = fields.get(i);
			if(f.getBaseType() == Types.TYPE_STRING)
				_string_fields.add(f);
			else
				_equality_fields.add(f);
		}

	}
	

	public void getInsertKeys(Entity e,Set<DatabaseEntry> result) throws DatabaseException
	{

	}


	//Query q = new Query("Book");
	//q.idx("BookFreeText");
	//q.containsAny(q.list(q.list("title","summary"),q.list("Adventures","Huckleberry")));
	//q.containsAll(q.list(q.list("title","summary"),q.list("Adventures","Huckleberry")));
	//q.containsPhrase(q.list(q.list("title","summary"),q.list("Adventures","of","Huckleberry"),PUBLISHED));
	//q.containsPhrase(q.list(q.VAL_GLOB,q.list("Adventures","of","Huckleberry"),PUBLISHED));
	public List<List<DatabaseEntry>> getQueryKeys(List<Object> vals) throws DatabaseException
	{
		
		List<String> fieldnames = null;
		try{
			fieldnames 		= (List<String>)vals.get(0);
		}catch(Exception e)
		{
			/* this happens when someone passes in Query.VAL_GLOB for field specification*/
			Object o = (Object)vals.get(0);
			if(o == Query.VAL_GLOB)
			{
				fieldnames = new ArrayList<String>();
				int s = _string_fields.size();
				for(int i = 0;i < s;i++)
					fieldnames.add(_string_fields.get(i).getName());
			}
			else
			{
				throw new DatabaseException("QUERY FIELDNAMES SHOULD BE A LIST PARAMETER OR Query.VAL_GLOB FOR ALL FIELDS.");
			}
		}

		List<List<DatabaseEntry>> ll = new ArrayList<List<DatabaseEntry>>(); 
		/* gen the equality part of the key */
		TupleOutput eq_output = new TupleOutput();
		int vs = vals.size();
		for(int i = 2;i < vs;i++)
		{
			FieldBinding.writeValueToTuple(_equality_fields.get(i-2), vals.get(i), eq_output);
		}
	
		
		//The idea here is that someone may make a multi freetext index and want to 
		//ignore the freetext part of the index when querying. this is accomplised because
		//on insert every entity is indexed as   KEY<null,null,equality_parts>:DATA<id,0>
		//query.setContainsAny(Query.VAL_GLOB,Query.VAL_GLOB,PUBLISHED);
		List<String> words	= null;
		try{
			words = (List<String>)vals.get(1);
		}catch(Exception e)
		{
			Object o = (Object)vals.get(0);
			if(o == Query.VAL_GLOB)
			{
				List<DatabaseEntry> l = new ArrayList<DatabaseEntry>(1);
				ll.add(l);
				TupleOutput tto = new TupleOutput();
				tto.writeString((String)null);
				tto.writeString((String)null);
				tto.writeFast(eq_output.toByteArray());
				l.add(new DatabaseEntry(tto.toByteArray()));
				return ll;
			}
		}
		

		
		/*make a set for each word combined with fieldname */
		int sf = fieldnames.size();
		int sw = words.size();

		for(int i = 0;i < sf;i++)
		{
			String fieldname = fieldnames.get(i);
			List<DatabaseEntry> l = new ArrayList<DatabaseEntry>(sw);
			ll.add(l);
			 /*this is for some bonehead passing null in as a param
			 * q.setContainsAny(q.list(q.list("title","body"),q.list(null),PUBLISHED) */
			if(words == null) 
			{
				TupleOutput tto = new TupleOutput();
				tto.writeString(fieldname);
				tto.writeString((String)null);
				tto.writeFast(eq_output.toByteArray());
				l.add(new DatabaseEntry(tto.toByteArray()));
			}
			else
			{
				String val;
				for(;;)
				{
					val = ((String)words.get(0)).toLowerCase();
					if(_stoplist.isStop(val))
					{
						words.remove(0);
						continue;
					}
					break;
				}
				sw = words.size();
				for(int ii = 0; ii < sw;ii++)
				{
					String word    = words.get(ii);
					String lc_word = word.toLowerCase();
					
					if(_stoplist.isStop(lc_word))
					{				
						l.add(SingleFieldFreeTextIndex.FREETEXT_STOP_WORD);	
					}
					else
					{
						lc_word = SingleFieldFreeTextIndex.trim_word(lc_word);
						word = _stemmer.stem(lc_word);

						TupleOutput tto = new TupleOutput();
						tto.writeString(fieldname);
						tto.writeString(word);
						tto.writeFast(eq_output.toByteArray());
						l.add(new DatabaseEntry(tto.toByteArray()));	
					}
				}
			}
		}
	
		return ll;
	}


	
	public void insertIndexEntry(Transaction parent_txn,Entity e,DatabaseEntry data) throws DatabaseException
	{	
		/* index entries look like   KEY<fieldname,word,equality_parts>:DATA<id,pos in field>*/
		TupleOutput eq_tuple_output = new TupleOutput();
		for(int i = 0;i < _equality_fields.size();i++)
		{
			FieldDefinition f = _equality_fields.get(i);
			FieldBinding.writeValueToTuple(f, e.getAttribute(f.getName()), eq_tuple_output);
		}
		
		//every record gets a row that ignores the free text indexing
		//so that someone could also use the multi index without a freetext
		//query where all records are considered
		//q.setContainsAny(Query.VAL_GLOB,Query.VAL_GLOB,PUBLISHED);
		write_index_row(parent_txn, e, null,null, eq_tuple_output, data,0 );
		for(int i = 0;i < _string_fields.size();i++)
		{
			FieldDefinition f   = _string_fields.get(i);
			String fieldname    = f.getName();
			//TODO: at some point we should support string arrays
			if(f.isArray())
			{
				List<String> ss = (List<String>)e.getAttribute(fieldname);
				if(ss == null)
					write_index_row(parent_txn, e, fieldname.toLowerCase(),null, eq_tuple_output, data,0 );
				else
				{
					
					for(int j = 0;j < ss.size();j++)
					{
						String word = ss.get(j);
						String lc_word = word.toLowerCase();
						if(_stoplist.isStop(lc_word))
						continue;

						lc_word = SingleFieldFreeTextIndex.trim_word(lc_word);
						word = _stemmer.stem(lc_word);
						write_index_row(parent_txn, e, fieldname,word, eq_tuple_output, data,j);
					}
				}
			}
			else
			{
				String s 			= (String)e.getAttribute(fieldname);
				if(s == null)
					write_index_row(parent_txn, e, fieldname.toLowerCase(),null, eq_tuple_output, data,0 );
				else
				{
					StringTokenizer st = new StringTokenizer(s);
					int c = -1;
					while(st.hasMoreTokens())
					{					
						c++;
						//System.out.println(e.getId()+":C IS "+c);
						String word = st.nextToken();
						String lc_word = word.toLowerCase();
						if(_stoplist.isStop(lc_word))
							continue;
	
						lc_word = SingleFieldFreeTextIndex.trim_word(lc_word);
						word = _stemmer.stem(lc_word);
						write_index_row(parent_txn, e, fieldname,word, eq_tuple_output, data,c );
					
					}
				}
			}
		}
		
		/*probably check to see if anything was inserted...everything could be culled because of ignore list*/
		/*insert null in this case??? */
		
	}

	private void write_index_row(Transaction parent_txn,Entity e,String fieldname,String word,TupleOutput eq_tuple_output,DatabaseEntry pkey,int pos) throws DatabaseException
	{
		DatabaseEntry key;
		DatabaseEntry ddata;
		/*key*/
		TupleOutput to = new TupleOutput();
		to.writeString(fieldname);
		to.writeString(word);
		to.writeFast(eq_tuple_output.toByteArray());
		key = new DatabaseEntry(to.toByteArray());
		
		/*data*/
		TupleOutput too = new TupleOutput();				
		too.writeFast(pkey.getData(), 0, pkey.getSize());
		too.writeInt(pos);/* we put in a position of zero here so all our index rows are the same */
		ddata = new DatabaseEntry(too.toByteArray());
		put_row(parent_txn,e,key,ddata);
	}

	public boolean key_matched(DatabaseEntry original_key,DatabaseEntry found_key)
	{
		return (IteratorUtil.compareDatabaseEntries(original_key, 0, original_key.getSize(), found_key, 0,original_key.getSize()) == 0);
	}
	
	
	private void put_row(Transaction parent_txn,Entity e,DatabaseEntry key,DatabaseEntry row) throws DatabaseException
	{
		String K = new String(key.getData());
		long ID  = LongBinding.entryToLong(new DatabaseEntry(row.getData(),0,8));
		int C 	 = IntegerBinding.entryToInt(new DatabaseEntry(row.getData(),8,12));
		//System.out.println("FREE TEXT INDEX PUTTING ROW "+K+" | "+ID+":"+C );
		
		Transaction txn 	= null;
		int retry_count = 0;				
		//fail fast here for now....for some reason our access pattern causes this retry to be useless
		//without starting the whole transaction over again//
		//while (retry_count < BDBStore.MAX_DEADLOCK_RETRIES) 
		//{
		    try {
				txn = environment.beginTransaction(parent_txn, null);	
				db_handle.put(txn, key, row);
				delete_handle.put(txn,row,key);	
				txn.commitNoSync();
				/* dont delete. we break out upon success */
				//break;
		    } catch (DeadlockException de) {
		    	try{
		    		txn.abort();
		    	}catch(DatabaseException dbe)
		    	{
		    		dbe.printStackTrace();
		    		System.err.println("FAILED ABORTING TXN.");
		    	}
		    		retry_count++;
		    	System.err.println(Thread.currentThread().getId()+" SEC INDEX WRITE DEADLOCK OCCURRED retry_count "+retry_count+" FOR "+e);
		    	//if (retry_count >= BDBStore.MAX_DEADLOCK_RETRIES) 
	            //{
		    		throw new DatabaseException(e.getId()+":108 SAVE FAILED FOR SEC INDEX.RETRY WAS GREATER THAN MAX_NUMBER_RETRYS.");
	            //}
		    }	
	}

	
	public void deleteIndexEntry(Transaction parent_txn,DatabaseEntry pkey) throws DatabaseException
	{	
//		System.out.println(getName()+" DELETEING INDEX ENTRYS FOR PKEY "+FieldBinding.entryToValue(Types.TYPE_LONG,pkey));
		OperationStatus op_stat;
		DatabaseEntry data  		= new DatabaseEntry();
		DatabaseEntry original_key  = new DatabaseEntry();
		Cursor del_cursor 	= null;
		Cursor idx_cursor 	= null;
		Transaction txn 	= null; 
		
		int retry_count = 0;
		while (retry_count < BDBStore.MAX_DEADLOCK_RETRIES) 
		{
		    try {
				txn = environment.beginTransaction(parent_txn, null);	
				del_cursor 			    = delete_handle.openCursor(txn, null);
				original_key = IteratorUtil.cloneDatabaseEntry(pkey);
				op_stat 				= del_cursor.getSearchKeyRange(pkey, data, LockMode.DEFAULT);
				if(op_stat == OperationStatus.NOTFOUND || !key_matched(original_key,pkey))
				{
					/* this can also happen if when a record was initially inserted the indexed field was not dirty.*/ 
					 /* having default field values ON INSERT should solve this problem */
					/* default field values should only set non dirty fields !!! */
					System.err.println(getName()+" UNABLE TO DELETE!!!! pkey not found.PROBABLY RECORD WAS NEVER INSERTED. ARE YOU NOT DEALING WITH NULLS? "+LongBinding.entryToLong(pkey));
					del_cursor.close();
					txn.commitNoSync();
					try{
						System.err.println("SHOULD NOT BE HERE!!!");
						throw new Exception();
					}catch(Exception e)
					{
						e.printStackTrace();
					}
					return;
				}
				
				idx_cursor 			    = db_handle.openCursor(txn, null);				
				do{
					op_stat = idx_cursor.getSearchBoth(data, pkey, LockMode.DEFAULT);
					if(op_stat == OperationStatus.NOTFOUND)
					{
						idx_cursor.close();
						del_cursor.close();
						txn.commitNoSync();
						throw new DatabaseException("INDEX SEEMS CORRUPTED NO KEY/PKEY ENTRY FOR DEL INDEX");
					}
//					System.out.println(">>>>>>>>>"+getName()+" LOW LEVEL DELETEING OF "+FieldBinding.entryToValue(Types.TYPE_LONG,pkey));
					op_stat = idx_cursor.delete();
					op_stat = del_cursor.delete();					
				}while(del_cursor.getNext(pkey, data, LockMode.DEFAULT) == OperationStatus.SUCCESS && key_matched(original_key,pkey));
			

				idx_cursor.close();
				del_cursor.close();
				txn.commitNoSync();
				/* ok...we need to do this because a delete will leave the key in a funky state
				 * since the reverse look up is ID:POS. We need to restore the passed in pkey when we are done
				 * so that when someone does an update the pkey is still the same length and id after
				 * the delete instead of some random comound ID:POS
				 */
				pkey.setData(original_key.getData(),0,original_key.getSize());
				/* we break on succes since we are in deadlock loop */
				break;
		    } catch (DeadlockException de) {
				idx_cursor.close();
				del_cursor.close();
		    	txn.abort();
		    	retry_count++;
	            System.err.println(Thread.currentThread().getId()+" SEC INDEX DELETE DEADLOCK OCCURRED retry count "+retry_count);
	            if (retry_count >= BDBStore.MAX_DEADLOCK_RETRIES) 
	            {
	            	throw new DatabaseException("108 DELETE FAILED FOR SEC INDEX .RETRY WAS GREATER THAN MAX_NUMBER_RETRYS.");
	            }
			}

		}//end while loop	
	}
	
	
	public static EntityIndexDefinition getDefinition()
	{
		EntityIndexDefinition definition = new EntityIndexDefinition();
		definition.setName(NAME);
		definition.setIsMultiField(true);
		definition.setDescription(" Creates an index on multiple fields in an entity"+
								  " ordered by the first field. The entities can "+
								  " be queried for equality accross index member fields."+
								  " i.e. First Name=\"Dave\" Last Name=\"Rogers\"");

		//we dont do this anymore but this is how you add settable paramters to an index definition
		//FieldDefinition array_index_strategy = new FieldDefinition();
		//array_index_strategy.setName(KEY_PARAM_ARRAY_INDEXING_STRATEGY);
		//array_index_strategy.setType(Types.TYPE_INT);
		//array_index_strategy.setDescription("0=Index Arrays For Equality," +
		//									"1=Index Arrays for Containment");
		//definition.addAttribute(array_index_strategy);
		return definition;
	}

	public void validateFields() throws PersistenceException
	{
		for(int i = 0;i < fields.size();i++)
		{
			if(fields.get(i).getType() == Types.TYPE_STRING)
				return;
		}
		throw new PersistenceException("MULTIFIELD FREE TEXT INDEX MUST CONTAIN AT LEAST ONE STRING FIELD. THE FIELD TO BE FREE TEXT INDEXED.");
	}
}
