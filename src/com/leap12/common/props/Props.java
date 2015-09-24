package com.leap12.common.props;

import java.util.Properties;

public class Props implements PropsRead, PropsWrite {
	public static final Props EMPTY = new Props();

	@Override
	public void putAll( Properties sysProps ) {
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

	@Override
	public String getString( String key ) {
		return null;
	}

	@Override
	public Boolean getBoolean( String key ) {
		return null;
	}

	@Override
	public boolean getBoolean( String key, boolean defVal ) {
		return defVal;
	}

	@Override
	public int getInt( String key, int defVal ) {
		return defVal;
	}

	@Override
	public Integer getInteger( String key ) {
		return null;
	}

	@Override
	public long getLong( String key, long defVal ) {
		return defVal;
	}

	@Override
	public Long getLong( String key ) {
		return null;
	}

	@Override
	public float getFloat( String key, float defVal ) {
		return defVal;
	}

	@Override
	public Float getFloat( String key ) {
		return null;
	}

	@Override
	public double getDouble( String key, double defVal ) {
		return defVal;
	}

	@Override
	public Double getDouble( String key ) {
		return null;
	}

}
