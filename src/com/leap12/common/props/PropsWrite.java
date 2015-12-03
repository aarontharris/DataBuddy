package com.leap12.common.props;

import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;

public interface PropsWrite {

	public void putAll( Properties sysProps );

	public void putAll( JSONObject json );

	public void putAll( Map<String, String> map );

	public void putString( String key, String val );

	default void putBoolean( String key, Boolean val ) {
		putString( key, String.valueOf( val ) );
	}

	default void putInteger( String key, Integer val ) {
		putString( key, String.valueOf( val ) );
	}

	default void putLong( String key, Long val ) {
		putString( key, String.valueOf( val ) );
	}

	default void putFloat( String key, Float val ) {
		putString( key, String.valueOf( val ) );
	}

	default void putDouble( String key, Double val ) {
		putString( key, String.valueOf( val ) );
	}

}
