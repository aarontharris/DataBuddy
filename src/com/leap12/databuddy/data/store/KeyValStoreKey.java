package com.leap12.databuddy.data.store;

import com.leap12.databuddy.sqlite.SqliteDataStoreSimple;
import com.leap12.databuddy.sqlite.SqliteShardKey;

public class KeyValStoreKey extends SqliteShardKey {

    protected KeyValStoreKey(String shard) {
        super(shard, SqliteDataStoreSimple.class);
    }

}
