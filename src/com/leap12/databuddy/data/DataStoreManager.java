package com.leap12.databuddy.data;


public interface DataStoreManager<T extends ShardKey> {

	public void startup() throws Exception;

	public void shutdown() throws Exception;

	public DataStore attainDataStore( T shardKey, long millisTimeout ) throws Exception;

	public void releaseDataStore( DataStore dataStore );

}
