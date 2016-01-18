package com.leap12.databuddy.ex;

import com.leap12.databuddy.Commands.ResponseStatus;

@SuppressWarnings( "serial" )
public class DBCmdException extends DBException {
	private ResponseStatus mStatus = ResponseStatus.FAIL_INVALID_CMD_STATE;

	public DBCmdException( String statusMessage ) {
		super( statusMessage );
	}

	public DBCmdException( String statusMessage, ResponseStatus status ) {
		super( statusMessage );
		mStatus = status;
	}

	public DBCmdException( String statusMessage, Throwable throwable ) {
		super( statusMessage, throwable );
	}

	public DBCmdException( String statusMessage, Throwable throwable, ResponseStatus status ) {
		super( statusMessage, throwable );
		mStatus = status;
	}

	public ResponseStatus getStatus() {
		return mStatus;
	}
}
