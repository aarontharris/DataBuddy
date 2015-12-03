package com.leap12.common.props;

import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;

public class Props {

	public static final PropsReadWrite EMPTY = new PropsReadWrite() {

		@Override
		public void putAll( Properties sysProps ) {
		}

		@Override
		public void putAll( JSONObject json ) {
		}

		@Override
		public void putAll( Map<String, String> map ) {
		}

		@Override
		public void putString( String key, String val ) {
		}

		@Override
		public void putBoolean( String key, Boolean val ) {
		}

		@Override
		public void putInteger( String key, Integer val ) {
		}

		@Override
		public void putLong( String key, Long val ) {
		}

		@Override
		public void putFloat( String key, Float val ) {
		}

		@Override
		public void putDouble( String key, Double val ) {
		}

	};
}
