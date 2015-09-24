package com.leap12.databuddy.connections.handler;

import com.leap12.common.HttpRequest;
import com.leap12.common.HttpResponse;
import com.leap12.databuddy.BaseConnectionDelegate;

public class HttpHandler {

	private final BaseConnectionDelegate mConnection;
	private final HttpRequest mRequest;

	public HttpHandler( BaseConnectionDelegate connection, HttpRequest request ) {
		this.mConnection = connection;
		this.mRequest = request;
	}

	public BaseConnectionDelegate getConnection() {
		return mConnection;
	}

	public HttpRequest getRequest() {
		return mRequest;
	}

	public void handleRequest() throws Exception {
		HttpResponse response = new HttpResponse();
		response.getBodyBuilder().append( "<html>" );
		response.getBodyBuilder().append( "<body>" );
		response.getBodyBuilder().append( "<b>Yaay! I'm a webserver!</b>" );
		response.getBodyBuilder().append( "</br>" );
		response.getBodyBuilder().append( "</br>" );
		response.getBodyBuilder().append( getRequest().describe( "<br/>\n" ) );
		response.getBodyBuilder().append( "</body>" );
		response.getBodyBuilder().append( "</html>" );
		getConnection().writeMsg( response.toString() );
	}


}
