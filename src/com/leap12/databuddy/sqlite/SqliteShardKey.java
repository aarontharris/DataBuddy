package com.leap12.databuddy.sqlite;

import com.leap12.databuddy.data.ShardKey;

public class SqliteShardKey extends ShardKey {

    protected SqliteShardKey(String shard) {
        super(shard, SqliteDataStore.class);
    }

    protected SqliteShardKey(String shard, Class<? extends SqliteDataStore> clazz) {
        super(shard, clazz);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Class<? extends SqliteDataStore> getDataStoreClass() {
        return (Class<? extends SqliteDataStore>) super.getDataStoreClass();
    }

}
