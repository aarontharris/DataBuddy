package com.leap12.databuddy.ex;

import com.leap12.databuddy.Commands.ResponseStatus;

@SuppressWarnings( "serial" )
public class DBCmdAuthException extends DBCmdException {

	public DBCmdAuthException( String statusMessage ) {
		this( statusMessage, null );
	}

	public DBCmdAuthException( String statusMessage, Throwable throwable ) {
		super( statusMessage, throwable, ResponseStatus.FAIL_NOT_AUTHORIZED );
	}

}
