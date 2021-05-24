package com.leap12.databuddy.data.store;

import com.leap12.common.NonNull;
import com.leap12.databuddy.sqlite.SqliteDataStoreManager;
import com.leap12.databuddy.sqlite.SqliteDataStoreSimple;
import com.leap12.databuddy.sqlite.SqliteShardKey;
import org.json.JSONArray;

/**
 * Construct for every request, but *usually* each request will get the same instance (not guaranteed)
 */
public class KeyValStore {

    private static final SqliteShardKey shardKey = new KeyValStoreKey("generic.keyval");
    private final SqliteDataStoreSimple dataStore;

    public KeyValStore() {
        try {
            dataStore = (SqliteDataStoreSimple) SqliteDataStoreManager.get().attainDataStore(shardKey, 1000);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void saveString(String topic, String subtopic, String key, String value) throws Exception {
        dataStore.beginWrites();
        dataStore.saveString(topic, subtopic, key, value);
        dataStore.endWrites();
    }

    public String loadString(String topic, String subtopic, String key) throws Exception {
        dataStore.beginReads();
        String result = dataStore.loadString(topic, subtopic, key);
        dataStore.endReads();
        return result;
    }

    @NonNull
    public JSONArray loadAll(String topic, String subtopic) throws Exception {
        dataStore.beginReads();
        JSONArray result = dataStore.loadArrayOfKeyVals(topic, subtopic, null, null);
        dataStore.endReads();
        return result;
    }
}
