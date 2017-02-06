package com.leap12.common.http.err;

import com.leap12.common.http.HttpResponse.HttpException;
import com.leap12.common.http.HttpResponse.HttpStatusCode;

@SuppressWarnings( "serial" )
public class HttpExceptionUnavailable extends HttpException {
	public HttpExceptionUnavailable( String message, Throwable t ) {
		super( HttpStatusCode.ERR_UNAVAILABLE, message, t );
	}

	public HttpExceptionUnavailable( String message ) {
		super( HttpStatusCode.ERR_UNAVAILABLE, message );
	}
}
