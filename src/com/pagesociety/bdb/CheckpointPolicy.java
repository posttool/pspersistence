package com.pagesociety.bdb;


import java.util.Map;

import com.sleepycat.db.DatabaseException;


public interface CheckpointPolicy
{
	public void init(BDBStore context,Map<String,Object> config);
	public void handleCheckpoint() throws DatabaseException;
	public void destroy();
}
