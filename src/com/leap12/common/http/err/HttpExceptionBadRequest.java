package com.leap12.common.http.err;

import com.leap12.common.HttpResponse.HttpException;
import com.leap12.common.HttpResponse.HttpStatusCode;

@SuppressWarnings( "serial" )
public class HttpExceptionBadRequest extends HttpException {
	public HttpExceptionBadRequest( String message, Throwable t ) {
		super( HttpStatusCode.ERR_BAD_REQ, message, t );
	}

	public HttpExceptionBadRequest( String message ) {
		super( HttpStatusCode.ERR_BAD_REQ, message );
	}
}
