package com.leap12.databuddy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.json.JSONObject;

import com.leap12.common.Log;
import com.leap12.common.StrUtl;
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
	 * @throws Exception
	 */
	public void initialize( Properties systemProperties ) throws Exception {
		props = new PropsReadWrite();

		// Defaults -- low precedence
		props.putInteger( KEY_port, DEF_port );

		// System properties -- high precedence - overrides defaults, careful of namespacing conflicts with standard system properties.
		props.putAll( systemProperties );

		// Config file -- highest precedence - overrides all else when configFile is set via -DconfigFile=./databuddy.config
		String configFile = props.getString( "configFile" );
		Log.d( "configFile: " + configFile );
		if ( StrUtl.isNotEmpty( configFile ) ) {
			props.putAll( readConfigFile( configFile ) );
		}

		// it may seem redundant to put all the properties into a map and then cache them into properties
		// but we're doing it to allow property precedence
		port = props.getInteger( KEY_port, DEF_port );
		lineSeparator = props.getString( KEY_lineSeparator );
	}

	private JSONObject readConfigFile( String filename ) throws Exception {
		File file = new File( filename );

		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader( new FileReader( file ) )) {
			String line = null;
			while ( ( line = br.readLine() ) != null ) {
				Log.d( "Line: '%s'", line.trim() );
				sb.append( line );
			}
		} catch ( Exception e ) {
			Log.e( e );
		}

		JSONObject json = new JSONObject( sb.toString() );
		Log.d( "JSON: '%s'", json.toString() );
		return json;
	}

	public String getCharPalette() {
		return props.getString( "crypt.palette" );
	}

	public int getPort() {
		return port;
	}

	public String getLineSeparator() {
		return lineSeparator;
	}
}
