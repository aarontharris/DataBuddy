package com.leap12.common.http.err;

import com.leap12.common.http.HttpResponse.HttpException;
import com.leap12.common.http.HttpResponse.HttpStatusCode;

@SuppressWarnings( "serial" )
public class HttpExceptionUnauthorized extends HttpException {
	public HttpExceptionUnauthorized( String message, Throwable t ) {
		super( HttpStatusCode.ERR_UNAUTHORIZED, message, t );
	}

	public HttpExceptionUnauthorized( String message ) {
		super( HttpStatusCode.ERR_UNAUTHORIZED, message );
	}
}
