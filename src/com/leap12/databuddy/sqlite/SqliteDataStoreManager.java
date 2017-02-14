package com.leap12.databuddy.sqlite;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.leap12.databuddy.data.DataStore;
import com.leap12.databuddy.data.DataStoreManager;

public class SqliteDataStoreManager implements DataStoreManager<SqliteShardKey> {
	public static final String charEncoding = "UTF-8";
	public static final Charset CHARSET_UTF8 = Charset.forName( charEncoding );

	private static final String mDbName = "dataBuddy.db";

	public static final SqliteDataStoreManager SELF = new SqliteDataStoreManager();

	public static SqliteDataStoreManager get() {
		return SELF;
	}

	private final ReentrantLock lock = new ReentrantLock();

	private SqliteDataStoreManager() {
	}

	// FIXME: This can eventually run out of memory
	// FIXME: drop old DataStores with no references
	private final Map<SqliteShardKey, WeakReference<SqliteDataStore>> stores = new HashMap<>();

	@Override
	public DataStore attainDataStore( SqliteShardKey shardKey, long millisTimeout ) throws Exception {
		boolean ownLock = lock.tryLock( millisTimeout, TimeUnit.MILLISECONDS );
		try {

			SqliteDataStore store = null;
			WeakReference<SqliteDataStore> storeRef = stores.get( shardKey );
			if ( storeRef != null ) {
				store = storeRef.get();
			}

			if ( store == null ) {
				store = shardKey.getDataStoreClass().newInstance();
				String pathToDbStr = String.format( "./db/%s/%s", shardKey, mDbName );
				Path pathToFile = Paths.get( pathToDbStr );
				if ( !Files.exists( pathToFile ) ) {
					Files.createDirectories( pathToFile.getParent() );
					Files.createFile( pathToFile );
				}
				store.openConnection( pathToDbStr );
				stores.put( shardKey, new WeakReference<>( store ) );
			}
			return store;

		} finally {
			if ( ownLock ) {
				lock.unlock();
			}
		}
	}


	@Override
	public void startup() throws Exception {
	}

	@Override
	public void shutdown() throws Exception {
	}

	@Override
	public void releaseDataStore( DataStore store ) {
		// if ( store instanceof SqliteDataStore ) {
		// ( (SqliteDataStore) store ).closeConnection();
		// }
	}
}
