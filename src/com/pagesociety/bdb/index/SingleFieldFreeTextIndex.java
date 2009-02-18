package com.pagesociety.bdb.index;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import com.pagesociety.bdb.BDBSecondaryIndex;
import com.pagesociety.bdb.BDBStore;
import com.pagesociety.bdb.binding.FieldBinding;
import com.pagesociety.bdb.cache.ConcurrentLRUCache;
import com.pagesociety.bdb.index.freetext.DefaultStopList;
import com.pagesociety.bdb.index.freetext.FreetextStemmer;
import com.pagesociety.bdb.index.freetext.PorterStemmer;
import com.pagesociety.bdb.index.freetext.StopList;
import com.pagesociety.bdb.index.iterator.IteratorUtil;
import com.pagesociety.persistence.Entity;
import com.pagesociety.persistence.FieldDefinition;
import com.pagesociety.persistence.PersistenceException;
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

public class SingleFieldFreeTextIndex extends AbstractSingleFieldIndex
{

	public static final String NAME = SingleFieldFreeTextIndex.class.getSimpleName();
	private static  FreetextStemmer _stemmer = new PorterStemmer();
	private static  StopList _stoplist 	  = new DefaultStopList(null);
	
	public SingleFieldFreeTextIndex()
	{
		super(BDBSecondaryIndex.TYPE_FREETEXT_INDEX);
	}
	
	public void init(Map<String,String> attributes)
	{
		
	}

	public void validateField(FieldDefinition field) throws PersistenceException
	{
		if(field.getType() != Types.TYPE_STRING &&
			field.getType() != Types.TYPE_TEXT)
			throw new PersistenceException(getDefinition().getName()+" CAN ONLY INDEX FIELDS OF TYPE STRING OR TEXT.FIELD "+field.getName()+" IS NOT OF TYPE ARRAY");
	}
	
	
	
	@SuppressWarnings("unchecked")
	public void getInsertKeys(Entity e,Set<DatabaseEntry> result) throws DatabaseException
	{
		//just for abstract class since we are overloading insertIndexEntry
	}
	
	/*look where we trim these from the beginning!!!*/
	public static final DatabaseEntry FREETEXT_STOP_WORD = new DatabaseEntry();
	public List<DatabaseEntry> getQueryKeys(List<Object> values) throws DatabaseException
	{
		List<DatabaseEntry> ret;
		DatabaseEntry d; 
		if(values == null)
		{
			d = new DatabaseEntry();
			StringBinding.stringToEntry((String)null, d);
			ret = new ArrayList<DatabaseEntry>(1);
			ret.add(d);
			return ret;
		}
		else
		{
			//remove leading stop words first so first key is always
			//something that could have been inserted (see SETCONTAINSPHRASEIterator)
			String val;
			for(;;)
			{
				val = ((String)values.get(0)).toLowerCase();
				if(_stoplist.isStop(val))
				{
					values.remove(0);
					continue;
				}
				break;
			}
			
			int s = values.size();
			ret = new ArrayList<DatabaseEntry>(s);
			for(int i = 0; i < s;i++)
			{
				String lc_word = ((String)values.get(i)).toLowerCase();
				if(_stoplist.isStop(lc_word))
				{
					ret.add(FREETEXT_STOP_WORD);
				}
				else
				{
					lc_word = trim_word(lc_word);
					val = _stemmer.stem(lc_word);
					d = new DatabaseEntry();
					StringBinding.stringToEntry(val,d);
					ret.add(d);
				}
			}
		}
		return ret;
	}	

	public void insertIndexEntry(Transaction parent_txn,Entity e,DatabaseEntry data) throws DatabaseException
	{	
		if(field.isArray())
		{
			List<String> ss 			= (List<String>)e.getAttribute(field.getName());
			DatabaseEntry key 	= new DatabaseEntry();
			if(ss == null)
			{
				/*key*/
				StringBinding.stringToEntry(null, key);
				/*data*/
				TupleOutput to = new TupleOutput();				
				to.writeFast(data.getData(), 0, data.getSize());
				to.writeInt(0);/* we put in a position of zero here so all our index rows are the same */
				DatabaseEntry ddata = new DatabaseEntry(to.toByteArray());
				put_row(parent_txn,e,key,ddata);
				return;
			}
			else
			{
				for(int i = 0;i < ss.size();i++)
				{
					String word = ss.get(i);
					String lc_word = word.toLowerCase();
					if(_stoplist.isStop(lc_word))
						continue;
	
					lc_word = trim_word(lc_word);
					word = _stemmer.stem(lc_word);
					/*CHECK IGNORE LIST IGNORE THAN STEM HERE*/
					/*key*/
				
					StringBinding.stringToEntry(word, key);
					/*data*/
					TupleOutput to = new TupleOutput();				
					to.writeFast(data.getData(), 0, data.getSize());
					to.writeInt(i);
					
					DatabaseEntry ddata = new DatabaseEntry(to.toByteArray());
					/*key is now word and ddata is {entity_id,pos in field}*/
					put_row(parent_txn,e,key,ddata);	
				}
			}
			/*probably check to see if anything was inserted...everything could be culled because of ignore list*/
			/*insert null in this case??? */	
		}
		else
		{
			String s 			= (String)e.getAttribute(field.getName());
			DatabaseEntry key 	= new DatabaseEntry();
			if(s == null)
			{
				/*key*/
				StringBinding.stringToEntry(null, key);
				/*data*/
				TupleOutput to = new TupleOutput();				
				to.writeFast(data.getData(), 0, data.getSize());
				to.writeInt(0);/* we put in a position of zero here so all our index rows are the same */
				DatabaseEntry ddata = new DatabaseEntry(to.toByteArray());
				put_row(parent_txn,e,key,ddata);
				return;
			}
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
	
					lc_word = trim_word(lc_word);
					word = _stemmer.stem(lc_word);
					/*CHECK IGNORE LIST IGNORE THAN STEM HERE*/
					/*key*/
				
					StringBinding.stringToEntry(word, key);
					/*data*/
					TupleOutput to = new TupleOutput();				
					to.writeFast(data.getData(), 0, data.getSize());
					to.writeInt(c);
					
					DatabaseEntry ddata = new DatabaseEntry(to.toByteArray());
					/*key is now word and ddata is {entity_id,pos in field}*/
					put_row(parent_txn,e,key,ddata);	
				}
			}
			/*probably check to see if anything was inserted...everything could be culled because of ignore list*/
			/*insert null in this case??? */
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
	
	public static String trim_word(String s)
	{
		int i;
		for(i = s.length()-1;i > -1;i--)
		{
			char c = s.charAt(i);
			if(Character.isJavaIdentifierPart(c))
				break;
		}
		
		return s.substring(0,i+1);
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
		System.out.println("FREE TEXT INDEX PUTTING ROW "+K+" | "+ID+":"+C );
		
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

	
	/* any index must!!! implement this method for now. this is how we get the meta information about what
	 * attributes it takes without having to construct an instance. See EntityDefinition*/
	public static EntityIndexDefinition getDefinition()
	{
		EntityIndexDefinition definition = new EntityIndexDefinition();
		definition.setName(NAME);
		definition.setIsMultiField(false);
		definition.setDescription("Creates an index on the members of an array field which you can query "+
									" by subset.i.e.  SET_CONTAINS_ALL {1,3,9}, SET_CONTAINS_ANY {1,3,9} etc.");
		
		//NOTE there are no attributes here
		//but something like this: 
		//FieldDef use_lower_case = new FieldDef();
		//use_lower_case.setName("use lower case");
		//use_lower_case.setType(Types.TYPE_BOOLEAN);
		//definition.addAttribute(use_lower_case);

		return definition;
	}



}
