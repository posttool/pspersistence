package com.pagesociety.bdb;

public class BDBStoreConfigKeyValues
{
	/* keys must always be unique */
	// TODO convert the other keys to strings... environment-root-directory will be
	// passed by an xml config file. the others only have the default impl for
	// now, so the params are not exposed to the xml config file yet.
	public static final String KEY_STORE_ROOT_DIRECTORY = "store-root-directory";
	public static final int KEY_DEADLOCK_RESOLUTION_SCHEME = 0x02;
	public static final int KEY_DEADLOCK_RESOLUTION_SCHEME_MONITOR_DEADLOCKS_INTERVAL = 0x03;
	public static final int KEY_STORE_LOCKER_CLASS = 0x04;
	public static final int KEY_STORE_CHECKPOINT_POLICY_CLASS = 0x08;
	public static final int VALUE_DEADLOCK_RESOLUTION_SCHEME_ALWAYS_CRAWL_LOCKTABLE = 0x01;
	public static final int VALUE_DEADLOCK_RESOLUTION_SCHEME_MONITOR_DEADLOCKS = 0x02;
}
