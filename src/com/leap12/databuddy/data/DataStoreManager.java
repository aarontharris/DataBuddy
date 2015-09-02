package com.leap12.databuddy.data;


public interface DataStoreManager {
	public void startup() throws Exception;
	public void shutdown() throws Exception;
	public DataStore attainDataStore();
	public void releaseDataStore(DataStore dataStore);
}
