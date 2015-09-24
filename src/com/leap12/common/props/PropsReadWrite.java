package com.leap12.common.props;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.leap12.common.Log;
import com.leap12.common.StrUtl;

public class PropsReadWrite extends Props {
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
	public String getString( String key ) {
		return properties.get( key );
	}

	@Override
	public void putString( String key, String val ) {
		properties.put( key, val );
	}

	@Override
	public Boolean getBoolean( String key ) {
		try {
			String val = properties.get( key );
			if ( StrUtl.isNotEmpty( val ) ) {
				return Boolean.valueOf( val );
			}
		} catch ( Exception e ) {
			Log.e( e );
		}
		return null;
	}

	@Override
	public boolean getBoolean( String key, boolean defVal ) {
		try {
			String val = properties.get( key );
			if ( StrUtl.isNotEmpty( val ) ) {
				return Boolean.parseBoolean( val );
			}
		} catch ( Exception e ) {
			Log.e( e );
		}
		return defVal;
	}

	@Override
	public void putBoolean( String key, Boolean val ) {
		putString( key, String.valueOf( val ) );
	}

	@Override
	public int getInt( String key, int defVal ) {
		try {
			String val = properties.get( key );
			if ( StrUtl.isNotEmpty( val ) ) {
				return Integer.parseInt( val );
			}
		} catch ( Exception e ) {
			Log.e( e );
		}
		return defVal;
	}

	@Override
	public Integer getInteger( String key ) {
		try {
			String val = properties.get( key );
			if ( StrUtl.isNotEmpty( val ) ) {
				return Integer.valueOf( val );
			}
		} catch ( Exception e ) {
			Log.e( e );
		}
		return null;
	}

	@Override
	public void putInteger( String key, Integer val ) {
		putString( key, String.valueOf( val ) );
	}

	@Override
	public long getLong( String key, long defVal ) {
		try {
			String val = properties.get( key );
			if ( StrUtl.isNotEmpty( val ) ) {
				return Long.parseLong( val );
			}
		} catch ( Exception e ) {
			Log.e( e );
		}
		return defVal;
	}

	@Override
	public Long getLong( String key ) {
		try {
			String val = properties.get( key );
			if ( StrUtl.isNotEmpty( val ) ) {
				return Long.valueOf( val );
			}
		} catch ( Exception e ) {
			Log.e( e );
		}
		return null;
	}

	@Override
	public void putLong( String key, Long val ) {
		putString( key, String.valueOf( val ) );
	}

	@Override
	public float getFloat( String key, float defVal ) {
		try {
			String val = properties.get( key );
			if ( StrUtl.isNotEmpty( val ) ) {
				return Float.parseFloat( val );
			}
		} catch ( Exception e ) {
			Log.e( e );
		}
		return defVal;
	}

	@Override
	public Float getFloat( String key ) {
		try {
			String val = properties.get( key );
			if ( StrUtl.isNotEmpty( val ) ) {
				return Float.valueOf( val );
			}
		} catch ( Exception e ) {
			Log.e( e );
		}
		return null;
	}

	@Override
	public void putFloat( String key, Float val ) {
		putString( key, String.valueOf( val ) );
	}

	@Override
	public double getDouble( String key, double defVal ) {
		try {
			String val = properties.get( key );
			if ( StrUtl.isNotEmpty( val ) ) {
				return Double.parseDouble( val );
			}
		} catch ( Exception e ) {
			Log.e( e );
		}
		return defVal;
	}

	@Override
	public Double getDouble( String key ) {
		try {
			String val = properties.get( key );
			if ( StrUtl.isNotEmpty( val ) ) {
				return Double.valueOf( val );
			}
		} catch ( Exception e ) {
			Log.e( e );
		}
		return null;
	}

	@Override
	public void putDouble( String key, Double val ) {
		putString( key, String.valueOf( val ) );
	}
}
