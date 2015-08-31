package com.leap12.common;

import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class Props {
	private final ConcurrentHashMap<String, String> properties = new ConcurrentHashMap<>();

	public Props() {
	}

	public void putAll(Properties sysProps) {
		synchronized (properties) {
			if (sysProps != null) {
				for (Object key : sysProps.keySet()) {
					if (key instanceof String) {
						properties.put((String) key, sysProps.getProperty((String) key));
					}
				}
			}
		}
	}

	public String getString(String key) {
		return properties.get(key);
	}

	public void putString(String key, String val) {
		properties.put(key, val);
	}

	public Boolean getBoolean(String key) {
		try {
			String val = properties.get(key);
			if (StrUtl.isNotEmpty(val)) {
				return Boolean.valueOf(val);
			}
		} catch (Exception e) {
			Log.e(e);
		}
		return null;
	}

	public boolean getBoolean(String key, boolean defVal) {
		try {
			String val = properties.get(key);
			if (StrUtl.isNotEmpty(val)) {
				return Boolean.parseBoolean(val);
			}
		} catch (Exception e) {
			Log.e(e);
		}
		return defVal;
	}

	public void putBoolean(String key, Boolean val) {
		putString(key, String.valueOf(val));
	}

	public int getInt(String key, int defVal) {
		try {
			String val = properties.get(key);
			if (StrUtl.isNotEmpty(val)) {
				return Integer.parseInt(val);
			}
		} catch (Exception e) {
			Log.e(e);
		}
		return defVal;
	}

	public Integer getInteger(String key) {
		try {
			String val = properties.get(key);
			if (StrUtl.isNotEmpty(val)) {
				return Integer.valueOf(val);
			}
		} catch (Exception e) {
			Log.e(e);
		}
		return null;
	}

	public void putInteger(String key, Integer val) {
		putString(key, String.valueOf(val));
	}

	public long getLong(String key, long defVal) {
		try {
			String val = properties.get(key);
			if (StrUtl.isNotEmpty(val)) {
				return Long.parseLong(val);
			}
		} catch (Exception e) {
			Log.e(e);
		}
		return defVal;
	}

	public Long getLong(String key) {
		try {
			String val = properties.get(key);
			if (StrUtl.isNotEmpty(val)) {
				return Long.valueOf(val);
			}
		} catch (Exception e) {
			Log.e(e);
		}
		return null;
	}

	public void putLong(String key, Long val) {
		putString(key, String.valueOf(val));
	}

	public float getFloat(String key, float defVal) {
		try {
			String val = properties.get(key);
			if (StrUtl.isNotEmpty(val)) {
				return Float.parseFloat(val);
			}
		} catch (Exception e) {
			Log.e(e);
		}
		return defVal;
	}

	public Float getFloat(String key) {
		try {
			String val = properties.get(key);
			if (StrUtl.isNotEmpty(val)) {
				return Float.valueOf(val);
			}
		} catch (Exception e) {
			Log.e(e);
		}
		return null;
	}

	public void putFloat(String key, Float val) {
		putString(key, String.valueOf(val));
	}

	public double getDouble(String key, double defVal) {
		try {
			String val = properties.get(key);
			if (StrUtl.isNotEmpty(val)) {
				return Double.parseDouble(val);
			}
		} catch (Exception e) {
			Log.e(e);
		}
		return defVal;
	}

	public Double getDouble(String key) {
		try {
			String val = properties.get(key);
			if (StrUtl.isNotEmpty(val)) {
				return Double.valueOf(val);
			}
		} catch (Exception e) {
			Log.e(e);
		}
		return null;
	}

	public void putDouble(String key, Double val) {
		putString(key, String.valueOf(val));
	}
}
