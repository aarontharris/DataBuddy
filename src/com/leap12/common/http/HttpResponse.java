package com.leap12.common.http;

import java.util.HashSet;
import java.util.Set;

import com.leap12.common.Log;
import com.leap12.common.Nullable;
import com.leap12.common.Pair;
import com.leap12.common.http.err.HttpExceptionBadRequest;
import com.leap12.common.http.err.HttpExceptionForbidden;
import com.leap12.common.http.err.HttpExceptionInternal;
import com.leap12.common.http.err.HttpExceptionNotImplemented;
import com.leap12.common.http.err.HttpExceptionTimeout;
import com.leap12.common.http.err.HttpExceptionUnauthorized;
import com.leap12.common.http.err.HttpExceptionUnavailable;
import com.leap12.databuddy.DataBuddy;

public class HttpResponse {

	@SuppressWarnings( "serial" )
	public static class HttpException extends Exception {
		private final HttpStatusCode code;

		public HttpException( HttpStatusCode code, String message, Throwable t ) {
			super( Thread.currentThread().getId() + "." + DataBuddy.get().getPort() + ": " + message, t );
			this.code = code;
		}

		public HttpException( HttpStatusCode code, String message ) {
			super( Thread.currentThread().getId() + "." + DataBuddy.get().getPort() + ": " + message );
			this.code = code;
		}

		public HttpStatusCode getStatusCode() {
			return code;
		}
	}



	public static enum HttpStatusCode {
		OK( HttpStatus.SC_OK, "OK" ), // 200 - All Good
		ERR_BAD_REQ( HttpStatus.SC_BAD_REQUEST, "Bad Request" ), // 400 - The requested service exists but the request was invalid or missing data
		ERR_UNAUTHORIZED( HttpStatus.SC_UNAUTHORIZED, "Authentication Error" ), // 401 - There was a problem verifying the login
		ERR_FORBIDDEN( HttpStatus.SC_FORBIDDEN, "Forbidden" ), // 403 - You are logged in but you are not permitted
		ERR_NOT_FOUND( HttpStatus.SC_BAD_REQUEST, "Bad Request" ), // 404 - The server is available but the requested resource was not found
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

		public HttpException toException( String message ) {
			switch ( this ) {
			case OK:
				return null;
			case ERR_BAD_REQ:
				return new HttpExceptionBadRequest( message );
			case ERR_FORBIDDEN:
				return new HttpExceptionForbidden( message );
			case ERR_INTERNAL:
				return new HttpExceptionInternal( message );
			case ERR_NOT_IMPLEMENTED:
				return new HttpExceptionNotImplemented( message );
			case ERR_TIMEOUT:
				return new HttpExceptionTimeout( message );
			case ERR_UNAUTHORIZED:
				return new HttpExceptionUnauthorized( message );
			case ERR_UNAVAILABLE:
				return new HttpExceptionUnavailable( message );
			default:
				return new HttpExceptionInternal( "UNKNOWN: " + message );
			}
		}

		public boolean isOK() {
			return HttpStatusCode.OK.equals( this );
		}
	}

	private final Set<Pair<String, String>> defaultHeaders;
	private HttpStatusCode mCode = HttpStatusCode.OK;
	private Set<Pair<String, String>> mHeaders;
	private StringBuilder mBodyBuilder;
	private Exception error;
	private String statusIdentifier;

	public HttpResponse() {
		defaultHeaders = new HashSet<>();
		defaultHeaders.add( new Pair<String, String>( "Content-Type", "text/html" ) );
		defaultHeaders.add( new Pair<String, String>( "Server", "DataBuddy/1.1" ) );
	}

	public Exception getError() {
		return error;
	}

	public void addHeader( String key, String value ) {
		if ( mHeaders == null ) {
			mHeaders = new HashSet<>();
		}
		mHeaders.add( new Pair<String, String>( key, value ) );
	}

	/**
	 * Set the response status code<br>
	 * 
	 * @param code
	 * @param e
	 * @return a status identifier composed of [TID].[CurrentTimeMillis]
	 */
	public String setStatusCode( HttpStatusCode code, @Nullable Exception e ) {
		this.mCode = code;
		this.statusIdentifier = Thread.currentThread().getId() + "." + System.currentTimeMillis();
		this.error = new HttpException( code, this.statusIdentifier, e );
		return this.statusIdentifier;
	}

	public String setStatusCode( HttpException e ) {
		return setStatusCode( e.getStatusCode(), e );
	}

	public String setStatusCode( Exception e ) {
		return setStatusCode( HttpStatusCode.ERR_INTERNAL, e );
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
		StringBuilder out = new StringBuilder();

		if ( !mCode.isOK() ) {
			getBodyBuilder().append( "ERRCODE: " + Thread.currentThread().getId() + "." + System.currentTimeMillis() );
			out.append( "\n" );
		}
		String body = getBodyBuilder().toString();

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
		resp.setStatusCode( HttpStatusCode.OK, null );
		resp.addHeader( "Content-Type", "application/json;charset=ISO-8859-1" );
		resp.setBody( output );
		Log.debugNewlineChars( resp.toString() );
	}
}
