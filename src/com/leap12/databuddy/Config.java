package com.leap12.databuddy;

import java.util.Properties;
import com.leap12.common.props.PropsReadWrite;

public final class Config {

	private static final String KEY_port = "port";
	private static final int DEF_port = 25564;
	private static final String KEY_lineSeparator = "line.separator"; // no need for default, java comes with one

	private static final Config self = new Config();

	public static final Config get() {
		return self;
	}

	private PropsReadWrite props;

	// cache
	private int port;
	private String lineSeparator;

	private Config() {
	}

	/**
	 * Initialize or reset the config - best to do this at startup
	 * 
	 * @param systemProperties - optional - not validated
	 */
	public void initialize( Properties systemProperties ) {
		props = new PropsReadWrite();

		// Defaults -- lowest precedence
		props.putInteger( KEY_port, DEF_port );

		// System properties -- low precedence - overrides defaults, careful of namespacing conflicts with standard system properties.
		props.putAll( systemProperties );

		// Config file -- high precedence
		// props.putAll(configFile); // TODO config file

		// command line args -- highest precedence
		// props.putAll(args[]); // TODO command line args

		// it may seem redundant to put all the properties into a map and then cache them into properties
		// but we're doing it to allow property precedence

		port = props.getInt( KEY_port, DEF_port );
		lineSeparator = props.getString( KEY_lineSeparator );
	}

	public int getPort() {
		return port;
	}

	public String getLineSeparator() {
		return lineSeparator;
	}
}
