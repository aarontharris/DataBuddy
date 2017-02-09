package com.leap12.databuddy.data;


public class SimpleShardKey extends ShardKey {

	public SimpleShardKey( String context ) {
		super( toShard( context ) );
	}

	private static String toShard( String str ) {
		return str.substring( 0, 2 );
	}

}
