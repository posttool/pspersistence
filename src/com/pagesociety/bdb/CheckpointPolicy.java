package com.pagesociety.bdb;

import java.util.HashMap;


public interface CheckpointPolicy
{
	public void init(HashMap<Object,Object> config);
	public boolean isCheckpointNecessary();
}
