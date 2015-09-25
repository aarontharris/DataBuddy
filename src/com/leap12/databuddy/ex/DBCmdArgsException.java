package com.leap12.databuddy.ex;

import com.leap12.databuddy.Commands.ResponseStatus;

@SuppressWarnings( "serial" )
public class DBCmdArgsException extends DBCmdException {

	public DBCmdArgsException( String statusMessage ) {
		this( statusMessage, null );
	}

	public DBCmdArgsException( String statusMessage, Throwable throwable ) {
		super( statusMessage, throwable, ResponseStatus.FAIL_INVALID_CMD_ARGUMENTS );
	}

}
