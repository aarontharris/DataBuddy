package com.leap12.common;

import java.util.HashSet;
import java.util.Set;

public class HttpResponse {
	public static enum HttpStatusCode {
		OK( HttpStatus.SC_OK, "OK" ), // 200 - All Good
		ERR_BAD_REQ( HttpStatus.SC_BAD_REQUEST, "Bad Request" ), // 400 - The requested service exists but the request was invalid or missing data
		ERR_UNAUTHORIZED( HttpStatus.SC_UNAUTHORIZED, "Authentication Error" ), // 401 - There was a problem verifying the login
		ERR_FORBIDDEN( HttpStatus.SC_FORBIDDEN, "Forbidden" ), // 403 - You are logged in but you are not permitted
		ERR_INTERNAL( HttpStatus.SC_INTERNAL_SERVER_ERROR, "Internal Server Error" ), // 500 - Something bad happened internally
		ERR_NOT_IMPLEMENTED( HttpStatus.SC_NOT_IMPLEMENTED, "Service Not Implemented" ), // 501 - Service does not exist
		ERR_UNAVAILABLE( HttpStatus.SC_SERVICE_UNAVAILABLE, "Service Temporarily Unavailable" ), // 503 - Service is temporarily unavailable (resources?) try again later
		ERR_TIMEOUT( HttpStatus.SC_GATEWAY_TIMEOUT, "Service Timeout" ), // 504 - Service took too long to respond
		;


		private final int code;
		private final String msg;

		HttpStatusCode( int code, String msg ) {
			this.code = code;
			this.msg = msg;
		}
	}

	private final Set<Pair<String, String>> defaultHeaders;
	private HttpStatusCode mCode = HttpStatusCode.OK;
	private Set<Pair<String, String>> mHeaders;
	private StringBuilder mBodyBuilder;

	public HttpResponse() {
		defaultHeaders = new HashSet<>();
		defaultHeaders.add( new Pair<String, String>( "Content-Type", "text/html" ) );
		defaultHeaders.add( new Pair<String, String>( "Server", "DataBuddy/1.1" ) );
	}

	public void addHeader( String key, String value ) {
		if ( mHeaders == null ) {
			mHeaders = new HashSet<>();
		}
		mHeaders.add( new Pair<String, String>( key, value ) );
	}

	public void setStatusCode( HttpStatusCode code ) {
		this.mCode = code;
	}

	public HttpStatusCode getStatusCode() {
		return this.mCode;
	}

	public StringBuilder getBodyBuilder() {
		if ( mBodyBuilder == null ) {
			setBody( null ); // initialize the builder
		}
		return mBodyBuilder;
	}

	public void setBody( String body ) {
		mBodyBuilder = new StringBuilder();
		if ( body != null ) {
			mBodyBuilder.append( body );
		}
	}

	public void appendBody( String string ) {
		getBodyBuilder().append( string );
	}

	@Override
	public String toString() {
		String body = getBodyBuilder().toString();
		StringBuilder out = new StringBuilder();

		Set<Pair<String, String>> headers = new HashSet<>();
		if ( mHeaders != null ) {
			headers.addAll( mHeaders ); // precedence over defaults
		}
		headers.addAll( defaultHeaders ); // does not overwrite

		out.append( "HTTP/1.1 " + mCode.code + " " + mCode.msg + "\r\n" );
		for ( Pair<String, String> pair : headers ) {
			if ( "Content-Length".equals( pair.a ) ) {
				// skip
			} else {
				out.append( pair.a + ": " + pair.b + "\r\n" );
			}
		}
		out.append( "Content-Length: " + body.length() + "\r\n" );
		out.append( "\r\n" );
		out.append( body );
		return out.toString();
	}

	public static void test() {
		String output = "{\"color\": \"green\",\"message\": \"Hello!\", \"message_format\": \"text\", \"notify\": false }";

		HttpResponse resp = new HttpResponse();
		resp.setStatusCode( HttpStatusCode.OK );
		resp.addHeader( "Content-Type", "application/json;charset=ISO-8859-1" );
		resp.setBody( output );
		Log.debugNewlineChars( resp.toString() );
	}
}
