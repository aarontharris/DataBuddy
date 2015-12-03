package com.leap12.databuddy.data;

import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.leap12.common.Log;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.sqlite.SqliteDataStoreManager;

// Why wrap the SqliteDataStore ?
// 1. Abstraction, this allows us to easily change the database engine
// 2. Control, ensure one datastore per user connection
// 3. Encapsulation, added conveniences that are specific to the application but Sqlite doesn't care about.
// 4. Cleanliness, no need to clutter up the SqliteDataStore or its Manager with lockness mosters.
public final class Dao implements DataStore {

	// By extending it, we expose SqliteDataStoreManagers protected constructor, so only the Dao will secretely create an instance
	// As we don't really want the SqliteDataStoreManager exposed to the rest of the code base since it's supposed to be a swappable part.
	// The rest of the code shouldn't care about the DataStoreManager's true backing.
	private static final DataStoreManager dbFactory = new SqliteDataStoreManager( "dbBuddy.db" ) {};

	public static final Gson gson = new GsonBuilder().create();
	private static final String DEFAULT_SHARD_KEY = "default";
	private static final WeakHashMap<BaseConnectionDelegate, Map<String, Dao>> daos = new WeakHashMap<>();
	private static Lock lock = new ReentrantLock();

	/** Thread safe */
	public static final Dao getInstance( BaseConnectionDelegate connection, String shardKey ) throws Exception {
		lock.lock(); // this should be incredibly fast so lets not bother with the reentranceness
		long start = System.currentTimeMillis();
		try {
			if ( shardKey == null ) {
				shardKey = DEFAULT_SHARD_KEY;
			}

			Map<String, Dao> shards = daos.get( connection );
			if ( shards == null ) {
				shards = new HashMap<>();
				daos.put( connection, shards );
			}

			Dao dao = shards.get( shardKey );
			if ( dao == null ) {
				dao = new Dao( dbFactory.attainDataStore( shardKey ) );
				shards.put( shardKey, dao );
			}

			long now = System.currentTimeMillis();
			long delta = now - start;
			Log.d( "time: " + delta );
			return dao;
		} finally {
			lock.unlock();
		}
	}

	/** Thread safe */
	public static final void releaseInstance( BaseConnectionDelegate connection ) {
		lock.lock(); // this should be incredibly fast so lets not bother with the reentranceness
		try {
			daos.remove( connection );
		} finally {
			lock.unlock();
		}
	}

	private final DataStore mDataStore;

	private Dao( DataStore store ) {
		this.mDataStore = store;
	}

	@Override
	public void saveString( String topic, String subtopic, String key, String value ) throws Exception {
		mDataStore.saveString( topic, subtopic, key, value );
	}

	@Override
	public String loadString( String topic, String subtopic, String key ) throws Exception {
		return mDataStore.loadString( topic, subtopic, key );
	}

	@Override
	public void saveBlob( String topic, String subtopic, String key, byte[] value ) throws Exception {
	}

	@Override
	public byte[] loadBlob( String topic, String subtopic, String key ) throws Exception {
		return null;
	}

	@Override
	public void saveInt( String topic, String subtopic, String key, int value ) throws Exception {
	}

	@Override
	public int loadInt( String topic, String subtopic, String key ) throws Exception {
		return 0;
	}

	@Override
	public void saveBoolean( String topic, String subtopic, String key, boolean value ) throws Exception {
	}

	@Override
	public boolean loadBoolean( String topic, String subtopic, String key ) throws Exception {
		return false;
	}

	@Override
	public void saveFloat( String topic, String subtopic, String key, byte[] value ) throws Exception {
	}

	@Override
	public float loadFloat( String topic, String subtopic, String key ) throws Exception {
		return 0;
	}

	@Override
	public void saveJSONObject( String topic, String subtopic, String key, JSONObject value ) throws Exception {
	}

	@Override
	public JSONObject loadJSONObject( String topic, String subtopic, String key ) throws Exception {
		return null;
	}

}
