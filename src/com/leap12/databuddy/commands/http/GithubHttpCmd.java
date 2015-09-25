package com.leap12.databuddy.commands.http;

import com.leap12.common.HttpRequest;
import com.leap12.common.HttpResponse;
import com.leap12.common.Log;
import com.leap12.common.StrUtl;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.CmdResponse.CmdResponseMutable;

public class GithubHttpCmd extends HttpCmd {

	@Override
	public float isCommand( HttpRequest in ) {
		try {
			Log.d( "USER-AGENT: '%s'", in.getUserAgent() );
			return StrUtl.startsWith( in.getUserAgent(), "GitHub-Hookshot" ) ? 1f : 0;
		} catch ( Exception e ) {
			return 0f;
		}
	}

	@Override
	public CmdResponse<HttpResponse> executeCommand( BaseConnectionDelegate connection, HttpRequest input ) {
		try {
			HttpResponse response = new HttpResponse();
			connection.writeMsg( response.toString() );
			return new CmdResponseMutable<HttpResponse>( HttpResponse.class, response );
		} catch ( Exception e ) {
			return new CmdResponseMutable<HttpResponse>( HttpResponse.class, e );
		}
	}

	// POST / HTTP/1.1\r\n
	// Host: proxy.aarontharris.com:25566\r\n
	// Accept: */*\r\n
	// User-Agent: GitHub-Hookshot/9f2c1a3\r\n
	// X-GitHub-Event: ping\r\n
	// X-GitHub-Delivery: ebbdf200-6283-11e5-8270-49f5cd8cff1c\r\n
	// content-type: application/json\r\n
	// Content-Length: 6457\r\n
	// \r\n

}
