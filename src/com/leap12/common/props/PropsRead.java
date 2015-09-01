package com.leap12.common.props;

public interface PropsRead {

	public String getString(String key);

	public Boolean getBoolean(String key);

	public boolean getBoolean(String key, boolean defVal);

	public int getInt(String key, int defVal);

	public Integer getInteger(String key);

	public long getLong(String key, long defVal);

	public Long getLong(String key);

	public float getFloat(String key, float defVal);

	public Float getFloat(String key);

	public double getDouble(String key, double defVal);

	public Double getDouble(String key);

}