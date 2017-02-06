package com.leap12.dbexample;

import com.leap12.common.Log;
import com.leap12.common.http.HttpRequest;
import com.leap12.common.http.HttpResponse;
import com.leap12.common.http.annot.HttpGet;
import com.leap12.common.http.annot.HttpPost;
import com.leap12.common.props.PropsRead;
import com.leap12.databuddy.aspects.DefaultHandshakeDelegate;

public class MySpecialDelegate extends DefaultHandshakeDelegate {

	@HttpGet( "/test/{str$name}/and/{Str$attr}/search?query={Str$query}" )
	private void onTestGet( HttpRequest request, HttpResponse response, PropsRead params ) {
		Log.d( "Received Request matching onTestGet %s, %s, %s",
		        params.getString( "name" ),
		        params.getString( "attr" ),
		        params.getString( "query" )
		        );
		// Log.d( request.describe() );
	}

	@HttpPost( "/test/{str$name}/and/{Str$attr}/search?query={Str$query}" )
	private void onTestPost( HttpRequest request, HttpResponse response, PropsRead params ) {
		Log.d( "Received Request matching onTestPost %s, %s, %s",
		        params.getString( "name" ),
		        params.getString( "attr" ),
		        params.getString( "query" )
		        );
		// Log.d( request.describe() );
	}
}
