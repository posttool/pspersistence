package com.pagesociety.bdb;

public class BDBStoreConfigKeyValues
{
	/* keys must always be unique */
	// TODO convert the other keys to strings... environment-root-directory will be
	// passed by an xml config file. the others only have the default impl for
	// now, so the params are not exposed to the xml config file yet.
	public static final String KEY_STORE_ROOT_DIRECTORY 		= "store-root-directory";
	public static final String KEY_STORE_BACKUP_DIRECTORY 		= "store-backup-directory";
	public static final String KEY_DEADLOCK_RESOLUTION_SCHEME 	= "deadlock-resolution-scheme";
	public static final String KEY_DEADLOCK_RESOLUTION_SCHEME_MONITOR_DEADLOCKS_INTERVAL = "deadlock-resolution-scheme-interval";
	public static final String KEY_STORE_LOCKER_CLASS = "store-locker-class";
	public static final String KEY_STORE_CHECKPOINT_POLICY_CLASS = "check-point-policy-class";
	public static final int VALUE_DEADLOCK_RESOLUTION_SCHEME_ALWAYS_CRAWL_LOCKTABLE = 0x01;
	public static final int VALUE_DEADLOCK_RESOLUTION_SCHEME_MONITOR_DEADLOCKS 		= 0x02;
}
