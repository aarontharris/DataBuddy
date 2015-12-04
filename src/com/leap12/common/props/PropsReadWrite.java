package com.leap12.common.props;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.json.JSONObject;

public class PropsReadWrite implements PropsRead, PropsWrite {
	private final Map<String, String> properties = new HashMap<String, String>();

	@Override
	public void putAll( Properties sysProps ) {
		synchronized ( properties ) {
			if ( sysProps != null ) {
				for ( Object key : sysProps.keySet() ) {
					if ( key instanceof String ) {
						properties.put( (String) key, sysProps.getProperty( (String) key ) );
					}
				}
			}
		}
	}

	@Override
	public void putAll( JSONObject json ) {
		synchronized ( properties ) {
			for ( String key : json.keySet() ) {
				properties.put( key, String.valueOf( json.get( key ) ) );
			}
		}
	}

	@Override
	public void putAll( Map<String, String> map ) {
		synchronized ( properties ) {
			properties.putAll( map );
		}
	}

	@Override
	public void putString( String key, String val ) {
		properties.put( key, val );
	}

	@Override
	public boolean containsKey( String key ) {
		return properties.containsKey( key );
	}

	@Override
	public String getString( String key ) {
		return properties.get( key );
	}

	@Override
	public int size() {
		return properties.size();
	}

	@Override
	public Set<String> keySet() {
		return properties.keySet();
	}
}
