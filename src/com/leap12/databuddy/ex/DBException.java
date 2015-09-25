package com.leap12.databuddy.ex;

@SuppressWarnings( "serial" )
public class DBException extends Exception {

	public DBException( String msg ) {
		super( msg );
	}

	public DBException( String msg, Throwable throwable ) {
		super( msg, throwable );
	}

}
