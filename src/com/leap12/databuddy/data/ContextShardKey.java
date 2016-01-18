package com.leap12.databuddy.data;

import com.leap12.common.Log;

public class ContextShardKey extends ShardKey {

	public ContextShardKey( String context ) {
		super( toShard( context ) );
	}

	private static String toShard( String str ) {
		return str.substring( 0, 2 );
	}

	public static void main( String args[] ) {
		ShardKey key;
		String msg;

		msg = "asdfa";
		key = new ContextShardKey( msg );
		Log.d( "%s -> %s", msg.hashCode(), key );

		msg = "asdfasdf2428_asdf";
		key = new ContextShardKey( msg );
		Log.d( "%s -> %s", msg.hashCode(), key );

		msg = "your mom";
		key = new ContextShardKey( msg );
		Log.d( "%s -> %s", msg.hashCode(), key );

		msg = "hello world";
		key = new ContextShardKey( msg );
		Log.d( "%s -> %s", msg.hashCode(), key );

	}
}
