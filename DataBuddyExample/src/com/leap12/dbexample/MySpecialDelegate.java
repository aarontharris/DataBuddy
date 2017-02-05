package com.leap12.dbexample;

import com.leap12.common.HttpRequest;
import com.leap12.common.HttpResponse;
import com.leap12.common.Log;
import com.leap12.common.props.PropsRead;
import com.leap12.databuddy.aspects.DefaultHandshakeDelegate;
import com.leap12.databuddy.commands.http.annotation.HttpGet;

public class MySpecialDelegate extends DefaultHandshakeDelegate {

	@HttpGet( "/test/{str$name}/and/{str$attr}/search?query={Str$query}" )
	private void onTestGet( HttpRequest request, HttpResponse response, PropsRead params ) {
		Log.d( "Received Request matching onTestGet %s, %s, %s",
		        params.getString( "name" ),
		        params.getString( "attr" ),
		        params.getString( "query" )
		        );
		// Log.d( request.describe() );
	}

}
