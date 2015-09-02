package com.leap12.databuddy.data;

import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.leap12.databuddy.BaseConnection;
import com.leap12.databuddy.sqlite.SqliteDataStoreManager;

// Why wrap the SqliteDataStore ?
// 1. Abstraction, this allows us to easily change the database engine
// 2. Control, ensure one datastore per user connection
// 3. Encapsulation, added conveniences that are specific to the application but Sqlite doesn't care about.
// 4. Cleanliness, no need to clutter up the SqliteDataStore or its Manager with lockness mosters.
public final class Dao implements DataStore {

	private static final Gson gson = new GsonBuilder().create();
	private static final WeakHashMap<BaseConnection, Dao> daos = new WeakHashMap<>();
	private static Lock lock = new ReentrantLock();

	/** Thread safe */
	public static final Dao getInstance(BaseConnection connection) {
		lock.lock(); // this should be incredibly fast so lets not bother with the reentranceness
		try {
			Dao dao = daos.get(connection);
			if (dao == null) {
				dao = new Dao(SqliteDataStoreManager.getInstance().attainDataStore());
				daos.put(connection, dao);
			}
			return dao;
		} finally {
			lock.unlock();
		}
	}

	/** Thread safe */
	public static final void releaseInstance(BaseConnection connection) {
		lock.lock(); // this should be incredibly fast so lets not bother with the reentranceness
		try {
			daos.remove(connection);
		} finally {
			lock.unlock();
		}
	}

	private final DataStore mDataStore;

	private Dao(DataStore store) {
		this.mDataStore = store;
	}

	@Override
	public void saveString(String topic, String subtopic, String key, String value) throws Exception {
		mDataStore.saveString(topic, subtopic, key, value);
	}

	@Override
	public String loadString(String topic, String subtopic, String key) throws Exception {
		return mDataStore.loadString(topic, subtopic, key);
	}

}
