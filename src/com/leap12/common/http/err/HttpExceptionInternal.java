package com.leap12.common.http.err;

import com.leap12.common.HttpResponse.HttpException;
import com.leap12.common.HttpResponse.HttpStatusCode;

@SuppressWarnings( "serial" )
public class HttpExceptionInternal extends HttpException {
	public HttpExceptionInternal( String message, Throwable t ) {
		super( HttpStatusCode.ERR_INTERNAL, message, t );
	}

	public HttpExceptionInternal( String message ) {
		super( HttpStatusCode.ERR_INTERNAL, message );
	}
}
