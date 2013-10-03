package com.security.android.util;

public class SingletonDataExchange {
	private static SingletonDataExchange uniqInstance;

	private SingletonDataExchange() {
		banned = false;
	}

	public static synchronized SingletonDataExchange getInstance() {
		if (uniqInstance == null) {
			uniqInstance = new SingletonDataExchange();
		}
		return uniqInstance;
	}
	
	public boolean banned;
}
