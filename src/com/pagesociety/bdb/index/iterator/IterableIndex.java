package com.pagesociety.bdb.index.iterator;

import com.sleepycat.db.Database;

public interface IterableIndex
{
	public Database getDbh();
	public Database getReverseIndexDbh();
	public boolean 	isMultiFieldIndex();
	public int 		getNumIndexedFields();
	public boolean  isNormalIndex();
	public boolean  isSetIndex();
	public String   getName();
}
