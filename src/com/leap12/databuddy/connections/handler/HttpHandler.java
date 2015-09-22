package com.leap12.databuddy.connections.handler;

import com.leap12.common.HttpRequest;
import com.leap12.databuddy.BaseConnection;

public class HttpHandler {

	private BaseConnection mConnection;
	private HttpRequest mRequest;

	public HttpHandler( BaseConnection connection, HttpRequest request ) {
		this.mConnection = connection;
		this.mRequest = request;
	}

	public BaseConnection getConnection() {
		return mConnection;
	}

	public HttpRequest getRequest() {
		return mRequest;
	}

	public void handleRequest() throws Exception {
		getConnection().writeMsg( ""
				+ "Content-type: text/html\n\n"
				+ "<html>"
				+ "<body>"
				+ "<b>Boo... I'm a webserver...</b>"
				+ "</body>"
				+ "</html>\r\n\r\n" );
	}

}
