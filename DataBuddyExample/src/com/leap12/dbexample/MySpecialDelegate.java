package com.leap12.dbexample;

import com.leap12.common.Log;
import com.leap12.common.http.HttpRequest;
import com.leap12.common.http.HttpResponse;
import com.leap12.common.http.annot.HttpGet;
import com.leap12.common.http.annot.HttpPost;
import com.leap12.common.props.PropsRead;
import com.leap12.databuddy.aspects.DefaultHandshakeDelegate;

public class MySpecialDelegate extends DefaultHandshakeDelegate {

	private final TopicShardKey statesKey = new TopicShardKey( "states" ); // FIXME: should probably put these in a Dao
	private final TopicShardKey animalsKey = new TopicShardKey( "animals" );

	@HttpGet( "/test/{str$name}/and/{Str$attr}/search?query={Str$query}" )
	private void onTestGet( HttpRequest request, HttpResponse response, PropsRead params ) {
		Log.d( "Received Request matching onTestGet %s, %s, %s",
		        params.getString( "name" ),
		        params.getString( "attr" ),
		        params.getString( "query" )
		        );
		response.appendBody( "WOOT" );
	}

	@HttpPost( "/test/{str$name}/and/{Str$attr}/search?query={Str$query}" )
	private void onTestPost( HttpRequest request, HttpResponse response, PropsRead params ) {
		try {
			Log.d( "Received Request matching onTestPost %s, %s, %s",
			        params.getString( "name" ),
			        params.getString( "attr" ),
			        params.getString( "query" )
			        );

			// DataStore db = getDb( animalsKey );

			response.appendBody( "WOOT" );
		} catch ( Exception e ) {
			response.setStatusCode( e );
		}
	}
}
