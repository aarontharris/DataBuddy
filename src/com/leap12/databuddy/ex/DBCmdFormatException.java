package com.leap12.databuddy.ex;

import com.leap12.databuddy.Commands.ResponseStatus;

@SuppressWarnings( "serial" )
public class DBCmdFormatException extends DBCmdException {

	public DBCmdFormatException( String statusMessage ) {
		this( statusMessage, null );
	}

	public DBCmdFormatException( String statusMessage, Throwable throwable ) {
		super( statusMessage, throwable, ResponseStatus.FAIL_INVALID_CMD_FORMAT );
	}

}
