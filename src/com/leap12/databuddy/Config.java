package com.leap12.databuddy;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.json.JSONObject;

import com.leap12.common.Log;
import com.leap12.common.props.PropsRead;
import com.leap12.common.props.PropsReadWrite;

public final class Config {

	private static final String KEY_lineSeparator = "line.separator"; // no need for default, java comes with one
	private static final String KEY_cryptCharPalette = "crypt.lossy_palette"; // has its own default

	private static final String KEY_port = "port";
	private static final int DEF_port = 25564;

	private static final String KEY_cryptSalt = "crypt.salt";
	private static final String DEF_cryptSalt = "undefined";

	private static final String DEF_configFile = "./databuddy.json";

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
		if ( configFile == null || configFile.isEmpty() ) {
			configFile = DEF_configFile;
		}
		props.putAll( readConfigFile( configFile ) );

		// it may seem redundant to put all the properties into a map and then cache them into properties
		// but we're doing it to allow property precedence
		port = props.getInteger( KEY_port, DEF_port );
		lineSeparator = props.getString( KEY_lineSeparator );
	}

	private JSONObject readConfigFile( String filename ) throws Exception {
		// File file = new File( filename );

		Log.d( "Reading Config File: %s", filename );
		Path pathToFile = Paths.get( filename );
		if ( !Files.exists( pathToFile ) ) {
			Log.e( "Config file: Missing " + pathToFile.toString() );
			return null;
		}

		String fileContents = new String( Files.readAllBytes( pathToFile ) );

		JSONObject json = new JSONObject( fileContents );
		Log.d( "JSON: '%s'", json.toString() );
		return json;
	}

	public String getEncryptionSalt() {
		return props.getString( KEY_cryptSalt, DEF_cryptSalt );
	}

	public String getCharPalette( String defVal ) {
		return props.getString( KEY_cryptCharPalette, defVal );
	}

	public int getPort() {
		return port;
	}

	public String getLineSeparator() {
		return lineSeparator;
	}

	public PropsRead getProps() {
		return props;
	}
}
