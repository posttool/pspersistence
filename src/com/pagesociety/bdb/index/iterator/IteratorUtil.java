package com.pagesociety.bdb.index.iterator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.sleepycat.db.DatabaseEntry;

public class IteratorUtil {

	public static DatabaseEntry cloneDatabaseEntry(DatabaseEntry clonee)
	{
		int s = clonee.getSize();
		byte[] key_dup = new byte[s];
		if (s!=0)
			System.arraycopy(clonee.getData(),0,key_dup,0,s);	
		DatabaseEntry d = new DatabaseEntry(key_dup);
		return d;
	}
	
	public static int compareDatabaseEntries(DatabaseEntry d1,int o1,int l1,DatabaseEntry d2,int o2,int l2)
	{
		return compare(d1.getData(),o1,l1,d2.getData(),o2,l2);
	}
	
	
	public static final int compare (byte b1[], int o1, int l1, byte b2[], int o2, int l2) 
	{
			while ((o1 < l1) && (o2 < l2)) 
			{
				int cmp = (((int) b1[o1]) & 0xff) - (((int) b2[o2]) & 0xff) ;
				if ( cmp != 0 )
					return cmp;
				o1++;
				o2++;
			}
			return ((o1 == l1) && (o2 == l2)) ? 0 : l2-l1;
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
