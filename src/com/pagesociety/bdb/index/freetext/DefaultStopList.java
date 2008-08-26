package com.pagesociety.bdb.index.freetext;

import java.util.HashMap;
import java.util.Map;

public class DefaultStopList implements StopList
{
	
	private static String[] _stop_words = new String[]	                                 
	{
		     "I",
		     "a",
		     "about",
		     "an",
		     "are",
		     "as",
		     "at",
		     "be",
		     "by",
		     "com",
		     "de",
		     "en",
		     "for",
		     "from",
		     "how",
		     "in",
		     "is",
		     "it",
		     "la",
		     "of",
		     "on",
		     "or",
		     "that",
		     "the",
		     "this",
		     "to",
		     "was",
		     "what",
		     "when",
		     "where",
		     "who",
		     "will",
		     "with",
		     "und",
		     "the",
		     "www"};


	private Map<String,Object> _stop_map;
	private static final Object NOT_NULL = new Object();
	
	public DefaultStopList(Map<String,Object> params)
	{
		_stop_map = new HashMap<String,Object>();
		for( int i = 0;i < _stop_words.length;i++)
		{
			_stop_map.put(_stop_words[i],NOT_NULL);
		}
		
	}
	
	public boolean isStop(String word) 
	{
		return (_stop_map.get(word) == NOT_NULL);
	}

}
