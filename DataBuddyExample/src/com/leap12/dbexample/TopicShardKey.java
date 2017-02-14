package com.leap12.dbexample;

import com.leap12.databuddy.sqlite.SqliteShardKey;


public class TopicShardKey extends SqliteShardKey {

	public TopicShardKey( String topic ) {
		super( topic );
	}

}
