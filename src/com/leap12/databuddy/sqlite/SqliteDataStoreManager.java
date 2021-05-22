package com.leap12.databuddy.sqlite;

import com.leap12.databuddy.data.DataStore;
import com.leap12.databuddy.data.DataStoreManager;
import com.leap12.databuddy.data.ShardKey;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class SqliteDataStoreManager implements DataStoreManager<SqliteShardKey> {
    public static final String charEncoding = "UTF-8";
    public static final Charset CHARSET_UTF8 = Charset.forName(charEncoding);

    private static final String mDbName = "dataBuddy.db";

    public static final SqliteDataStoreManager SELF = new SqliteDataStoreManager();

    public static SqliteDataStoreManager get() {
        return SELF;
    }

    private final ReentrantLock lock = new ReentrantLock();

    private SqliteDataStoreManager() {
    }

    // Auto Tidy
    private final Map<SqliteShardKey, WeakReference<SqliteDataStore>> stores = new HashMap<>();
    private final LinkedList<SqliteShardKey> orderedKeys = new LinkedList<>();

    @Override
    public DataStore attainDataStore(SqliteShardKey shardKey, long millisTimeout) throws Exception {
        boolean ownLock = lock.tryLock(millisTimeout, TimeUnit.MILLISECONDS);
        try {
            SqliteDataStore store = null;
            WeakReference<SqliteDataStore> storeRef = stores.get(shardKey);
            if (storeRef != null) {
                store = storeRef.get();
                orderedKeys.remove(shardKey);
                orderedKeys.addFirst(shardKey);
            }

            if (store == null) {
                store = shardKey.getDataStoreClass().newInstance();
                String pathToDbStr = String.format("./db/%s/%s", shardKey, mDbName);
                Path pathToFile = Paths.get(pathToDbStr);
                if (!Files.exists(pathToFile)) {
                    Files.createDirectories(pathToFile.getParent());
                    Files.createFile(pathToFile);
                }
                store.openConnection(pathToDbStr);
                stores.put(shardKey, new WeakReference<>(store));
                orderedKeys.addFirst(shardKey);

                // when we acquire too many, lets tidy up
                cleanupDataStoresInsideLock();
            }
            return store;

        } finally {
            if (ownLock) {
                lock.unlock();
            }
        }
    }

    private void cleanupDataStoresInsideLock() {
        if (orderedKeys.size() > 100) {
            // First lets try to clean up old dead refs
            for (ShardKey key : stores.keySet()) {
                WeakReference<SqliteDataStore> storeRef = stores.get(key);
                if (storeRef.get() == null) {
                    stores.remove(key);
                    orderedKeys.remove(key);
                }
            }
            // If we are still over 100, then lets drop the oldest
            while (orderedKeys.size() > 100) {
                SqliteShardKey oldestKey = orderedKeys.removeLast();
                stores.remove(oldestKey);
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
    public void releaseDataStore(DataStore store) {
        // if ( store instanceof SqliteDataStore ) {
        // ( (SqliteDataStore) store ).closeConnection();
        // }
    }
}
