package com.leap12.common.props;

import java.util.Properties;

public interface PropsWrite {

	public void putAll( Properties sysProps );

	public void putString( String key, String val );

	public void putBoolean( String key, Boolean val );

	public void putInteger( String key, Integer val );

	public void putLong( String key, Long val );

	public void putFloat( String key, Float val );

	public void putDouble( String key, Double val );

}
