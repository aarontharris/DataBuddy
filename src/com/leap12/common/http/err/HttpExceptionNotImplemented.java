package com.leap12.common.http.err;

import com.leap12.common.http.HttpResponse.HttpException;
import com.leap12.common.http.HttpResponse.HttpStatusCode;

@SuppressWarnings( "serial" )
public class HttpExceptionNotImplemented extends HttpException {
	public HttpExceptionNotImplemented( String message, Throwable t ) {
		super( HttpStatusCode.ERR_NOT_IMPLEMENTED, message, t );
	}

	public HttpExceptionNotImplemented( String message ) {
		super( HttpStatusCode.ERR_NOT_IMPLEMENTED, message );
	}
}
