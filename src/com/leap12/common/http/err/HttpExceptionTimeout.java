package com.leap12.common.http.err;

import com.leap12.common.http.HttpResponse.HttpException;
import com.leap12.common.http.HttpResponse.HttpStatusCode;

@SuppressWarnings( "serial" )
public class HttpExceptionTimeout extends HttpException {
	public HttpExceptionTimeout( String message, Throwable t ) {
		super( HttpStatusCode.ERR_TIMEOUT, message, t );
	}

	public HttpExceptionTimeout( String message ) {
		super( HttpStatusCode.ERR_TIMEOUT, message );
	}
}
