package com.pagesociety.bdb;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.pagesociety.bdb.binding.FieldBinding;
import com.sleepycat.db.DatabaseEntry;


public class BDBQueryToken
{
	protected String 		_entity_type;
	protected String 		_index_name;
	protected int 			_query_op;
	protected int 	 		_page_size;
	protected String 		_index_key_as_hex_string;
	protected DatabaseEntry _index_key_as_database_entry;
	/* for between query iterators */
	protected String 		_index_top_key_as_hex_string;
	protected DatabaseEntry _index_top_key_as_database_entry;
	
	/* for set contains any iterator */
	protected List<DatabaseEntry> _set_keys_as_database_entries;
	protected String        	  _set_keys_as_hex_string;
	protected int 				  _current_set_key_index;
	protected List<DatabaseEntry> _seen_records_as_database_entries;
	protected String        	  _seen_records_as_hex_string;
	
	/* for set contains all iterator */
	protected DatabaseEntry       _smallest_key_as_database_entry;
	protected String        	  _smallest_key_as_hex_string;
	protected int 				  _otherkeys_length;
	protected DatabaseEntry       _last_pkey_as_database_entry;
	protected String        	  _last_pkey_as_hex_string;
	
	protected int 			_offset;

	public BDBQueryToken()
	{
		
	}
	
	//////////*BEGIN SET CONTAINS ANY*////////////////
	
	public void setSmallestKey(DatabaseEntry smallest_key)
	{
		_smallest_key_as_hex_string     = databaseEntryToHexString(smallest_key);
		_smallest_key_as_database_entry = smallest_key;
	}
	
	public String getSmallestKeyAsHexString()
	{
		return _smallest_key_as_hex_string;
	}
		
	public void setSmallestKey(String hexstring)
	{
		_smallest_key_as_database_entry = hexStringToDatabaseEntry(hexstring);
		_smallest_key_as_hex_string = hexstring;
	}

	public DatabaseEntry getSmallestKeyAsDatabaseEntry()
	{
		return _smallest_key_as_database_entry;
	}

	public void setLastPKey(DatabaseEntry last_pkey)
	{
		_last_pkey_as_hex_string     = databaseEntryToHexString(last_pkey);
		_last_pkey_as_database_entry = last_pkey;
	}
	
	public String getLastPKeyAsHexString()
	{
		return _last_pkey_as_hex_string;
	}
		
	public void setLastPKey(String hexstring)
	{
		_last_pkey_as_database_entry = hexStringToDatabaseEntry(hexstring);
		_last_pkey_as_hex_string = hexstring;
	}

	public DatabaseEntry getLastPKeyAsDatabaseEntry()
	{
		return _last_pkey_as_database_entry;
	}

	public void setOtherkeysLength(int length)
	{
		_otherkeys_length = length;
	}
	
	public int getOtherkeysLength()
	{
		return _otherkeys_length;
	}
	
	////////////*END SET CONTAINS ANY*////////////////
	
	public void setEntityType(String entity_type)
	{
		_entity_type = entity_type;
	}
	
	public String getEntityType()
	{
		return _entity_type;
	}

	public void setIndexName(String index_name)
	{
		_index_name = index_name;
	}
	
	public String getIndexName()
	{
		return _index_name;
	}

	public int getPageSize()
	{
		return _page_size;
	}
	
	public void setPageSize(int page_size)
	{
		_page_size = page_size;
	}
	
	public void setQueryOp(int op)
	{
		_query_op = op;
	}
	
	public int getQueryOp()
	{
		return _query_op;
	}
	
	public void setIndexKey(DatabaseEntry index_key)
	{
		_index_key_as_hex_string = databaseEntryToHexString(index_key);
		_index_key_as_database_entry = index_key;
	}
	
	public String getIndexKeyAsHexString()
	{
		return _index_key_as_hex_string;
	}
		
	public void setIndexKey(String hexstring)
	{
		_index_key_as_database_entry = hexStringToDatabaseEntry(hexstring);
		_index_key_as_hex_string = hexstring;
	}

	public DatabaseEntry getIndexKeyAsDatabaseEntry()
	{
		return _index_key_as_database_entry;
	}
	
	public void setIndexTopKey(DatabaseEntry index_key)
	{
		_index_top_key_as_hex_string = databaseEntryToHexString(index_key);
		_index_top_key_as_database_entry = index_key;
	}
	
	public String getIndexTopKeyAsHexString()
	{
		return _index_top_key_as_hex_string;
	}
		
	public void setIndexTopKey(String hexstring)
	{
		_index_top_key_as_database_entry = hexStringToDatabaseEntry(hexstring);
		_index_top_key_as_hex_string = hexstring;
	}

	public DatabaseEntry getIndexTopKeyAsDatabaseEntry()
	{
		return _index_top_key_as_database_entry;
	}

	public void setOffset(int offset)
	{
		_offset = offset;
	}

	public int getOffset()
	{
		return _offset;
	}
	
	public void setSetQueryKeys(List<DatabaseEntry> set_keys)
	{
		_set_keys_as_database_entries = set_keys;
		StringBuffer encoded_set_query_keys = new StringBuffer();
		int s = _set_keys_as_database_entries.size();
		for(int i = 0;i < s;i++)
		{
			//System.out.println(">>>SETTING QUERY KEY TO "+new String(_set_keys_as_database_entries.get(i).getData()));
			encoded_set_query_keys.append(databaseEntryToHexString(_set_keys_as_database_entries.get(i)));
			encoded_set_query_keys.append(':');//delimiter doesnt matter because of hex string
		}
		encoded_set_query_keys.setLength(encoded_set_query_keys.length()-1);
		_set_keys_as_hex_string = encoded_set_query_keys.toString();
	}
	
	public void setSetQueryKeys(String encoded_set_keys)
	{
		_set_keys_as_hex_string 		= encoded_set_keys;
		_set_keys_as_database_entries 	= new ArrayList<DatabaseEntry>();
		StringTokenizer st = new StringTokenizer(_set_keys_as_hex_string,":",false);
		while(st.hasMoreTokens())
			_set_keys_as_database_entries.add(hexStringToDatabaseEntry(st.nextToken()));

	}

	public String getSetQueryKeysAsHexString()
	{
		return _set_keys_as_hex_string;
	}

	public List<DatabaseEntry> getSetQueryKeysAsDatabaseEntries()
	{
		return _set_keys_as_database_entries;
	}
	
	public void setCurrentSetKeyIndex(int idx)
	{
		_current_set_key_index = idx;
	}
	
	public int getCurrentSetKeyIndex()
	{
		return _current_set_key_index;
	}

	public void setSetSeenRecords(List<DatabaseEntry> seen_records)
	{
		
		StringBuffer encoded_seen_records = new StringBuffer();
		int s = seen_records.size();
		for(int i = 0;i < s;i++)
		{
			encoded_seen_records.append(databaseEntryToHexString(seen_records.get(i)));
			encoded_seen_records.append(':');
		}
		encoded_seen_records.setLength(encoded_seen_records.length()-1);
		_seen_records_as_hex_string = encoded_seen_records.toString();
	}
	
	public void setSetSeenRecords(String encoded_seen_records)
	{
		_seen_records_as_hex_string 		= encoded_seen_records;
		_seen_records_as_database_entries 	= new ArrayList<DatabaseEntry>();
		StringTokenizer st = new StringTokenizer(_seen_records_as_hex_string,":",false);
		while(st.hasMoreTokens())
			_seen_records_as_database_entries.add(hexStringToDatabaseEntry(st.nextToken()));

	}

	public String getSeenRecordsAsHexString()
	{
		return _seen_records_as_hex_string;
	}

	public List<DatabaseEntry> getSeenRecordsAsDatabaseEntries()
	{
		return _seen_records_as_database_entries;
	}

	static char split_char = '!';
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(_entity_type);
		sb.append(split_char);
		sb.append(_index_name);
		sb.append(split_char);
		sb.append(_page_size);
		sb.append(split_char);
		sb.append(_query_op);
		sb.append(split_char);
		sb.append(_offset);
		sb.append(split_char);
		sb.append(_index_key_as_hex_string);
		sb.append(split_char);
		sb.append(_index_top_key_as_hex_string);
		sb.append(split_char);
		sb.append(_set_keys_as_hex_string);
		sb.append(split_char);
		sb.append(_current_set_key_index);
		sb.append(split_char);
		sb.append(_seen_records_as_hex_string);
		sb.append(split_char);
		sb.append(_smallest_key_as_hex_string);
		sb.append(split_char);
		sb.append(_otherkeys_length);
		sb.append(split_char);
		sb.append(_last_pkey_as_hex_string);
		return sb.toString();
	}

	public static BDBQueryToken fromString(String token)
	{
		BDBQueryToken t 	= new BDBQueryToken();	
		StringTokenizer st 	= new StringTokenizer(token,String.valueOf(split_char),false);
		t.setEntityType(st.nextToken());
		t.setIndexName(st.nextToken());
		t.setPageSize(Integer.parseInt(st.nextToken()));
		t.setQueryOp(Integer.parseInt(st.nextToken()));
		t.setOffset(Integer.parseInt(st.nextToken()));
		t.setIndexKey(st.nextToken()); 
		t.setIndexTopKey(st.nextToken()); 
		t.setSetQueryKeys(st.nextToken()); 
		t.setCurrentSetKeyIndex(Integer.parseInt(st.nextToken()));
		t.setSetSeenRecords(st.nextToken());
		t.setSmallestKey(st.nextToken());
		t.setOtherkeysLength(Integer.parseInt(st.nextToken()));
		t.setLastPKey(st.nextToken());
		return t;
	}
	
	
/* HEX ENCODING DECODING FOR BYTE ARRAYS */
	public static final byte NULL_BYTE = (byte)0;
	public static String databaseEntryToHexString(DatabaseEntry d)
	{
		if(d == null || d.getData() == null)
			return "00";

		byte[] db_entry_bytes = d.getData();
		int l = db_entry_bytes.length;
		if(l > 0 && db_entry_bytes[l-1] == NULL_BYTE)
		{
			while(l > 1 && db_entry_bytes[l-2] == NULL_BYTE)
				l--;
		}
		
		ByteArrayOutputStream string = new ByteArrayOutputStream();
		try{
			hex_encode(db_entry_bytes, 0, l, string);
		}catch(IOException e)
		{
			e.printStackTrace();
		}
		return string.toString();
	}
	
	public static DatabaseEntry hexStringToDatabaseEntry(String hexstring)
	{
		//kill trailing double null terminated bytes that may be in the key
		//due to bdb optimized reuse of skeys
		
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		try{
			hex_decode(hexstring,data);
		}catch(IOException e)
		{
			e.printStackTrace();
		}
		return new DatabaseEntry(data.toByteArray());
	}
	
	protected static final byte[] hex_encoding_table =
	{
        (byte)'0', (byte)'1', (byte)'2', (byte)'3', (byte)'4', (byte)'5', (byte)'6', (byte)'7',
        (byte)'8', (byte)'9', (byte)'a', (byte)'b', (byte)'c', (byte)'d', (byte)'e', (byte)'f'
	};
	    

    protected static byte[] hex_decoding_table = new byte[128];
    static
    {
        for (int i = 0; i < hex_encoding_table.length; i++)
            hex_decoding_table[hex_encoding_table[i]] = (byte)i;
        hex_decoding_table['A'] = hex_decoding_table['a'];
        hex_decoding_table['B'] = hex_decoding_table['b'];
        hex_decoding_table['C'] = hex_decoding_table['c'];
        hex_decoding_table['D'] = hex_decoding_table['d'];
        hex_decoding_table['E'] = hex_decoding_table['e'];
        hex_decoding_table['F'] = hex_decoding_table['f'];
    }
    
    public static int hex_encode(byte[] data,int off,int length,OutputStream out) throws IOException
    {        
        for (int i = off; i < (off + length); i++)
        {
            int    v = data[i] & 0xff;
            out.write(hex_encoding_table[(v >>> 4)]);
            out.write(hex_encoding_table[v & 0xf]);
        }
        return length * 2;
    }

    private static boolean hex_ignore(char c)
    {
        return (c == '\n' || c =='\r' || c == '\t' || c == ' ');
    }

    public static int hex_decode(byte[] data,int off,int length,OutputStream out) throws IOException
    {
        byte    b1, b2;
        int     outLen = 0;
        int     end = off + length;
        
        while (end > off)
        {
            if (!hex_ignore((char)data[end - 1]))
                break;
            end--;
        }
        
        int i = off;
        while (i < end)
        {
        	while (i < end && hex_ignore((char)data[i]))
                i++;
            
            b1 = hex_decoding_table[data[i++]];
            while (i < end && hex_ignore((char)data[i]))
                i++;
            b2 = hex_decoding_table[data[i++]];
            out.write((b1 << 4) | b2);
            outLen++;
        }
        return outLen;
    }
    
    public static int hex_decode(String data,OutputStream out) throws IOException
    {
        byte    b1, b2;
        int     length = 0;
        int     end = data.length();
        
        while (end > 0)
        {
            if (!hex_ignore(data.charAt(end - 1)))
                break;
            end--;
        }
        
        int i = 0;
        while (i < end)
        {
            while (i < end && hex_ignore(data.charAt(i)))
                i++;
            
            b1 = hex_decoding_table[data.charAt(i++)];
            while (i < end && hex_ignore(data.charAt(i)))
                i++;
            
            b2 = hex_decoding_table[data.charAt(i++)];
            out.write((b1 << 4) | b2);
            length++;
        }
        return length;
    }


}
