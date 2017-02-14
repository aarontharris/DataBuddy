package com.leap12.common.http.err;

import com.leap12.common.http.HttpResponse.HttpException;
import com.leap12.common.http.HttpResponse.HttpStatusCode;

@SuppressWarnings( "serial" )
public class HttpExceptionForbidden extends HttpException {
	public HttpExceptionForbidden( String message, Throwable t ) {
		super( HttpStatusCode.ERR_FORBIDDEN, message, t );
	}

	public HttpExceptionForbidden( String message ) {
		super( HttpStatusCode.ERR_FORBIDDEN, message );
	}
}
