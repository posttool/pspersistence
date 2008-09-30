package com.pagesociety.bdb;


import java.util.Map;


public interface CheckpointPolicy
{
	public void init(Map<String,Object> config);
	public boolean isCheckpointNecessary();
}
