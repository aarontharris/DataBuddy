package com.leap12.databuddy;

import java.util.Properties;

import com.leap12.common.Props;

public final class Config {
	private static final String KEY_port = "port";
	private static final int DEF_port = 5309;

	private static final Config self = new Config();

	public static final Config get() {
		return self;
	}

	private Props props;

	private Config() {
		initDefaults();
	}

	private void initDefaults() {
		props = new Props();
		setPort(DEF_port);
	}

	public void clear() {
		initDefaults();
	}

	/** not validated */
	public void setSystemProperties(Properties systemProperties) {
		props.putAll(systemProperties);
	}

	public void setPort(int port) {
		props.putInteger(KEY_port, port);
	}

	public int getPort() {
		return props.getInt(KEY_port, DEF_port);
	}
}
