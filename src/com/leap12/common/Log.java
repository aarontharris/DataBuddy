package com.leap12.common;

public class Log {
	private static final long startTime = System.currentTimeMillis();

	private static String prefix() {
		long now = System.currentTimeMillis();
		return String.format( "[%04d][%012d] ", Thread.currentThread().getId(), ( now - startTime ) );
	}

	public static void d( String format, Object... args ) {
		System.out.println( String.format( prefix() + format, args ) );
	}

	public static void e( Throwable error, String format, Object... args ) {
		e( format, args );
		e( error );
	}

	public static void e( String format, Object... args ) {
		System.err.println( prefix() + String.format( format, args ) );
	}

	public static void e( Throwable error ) {
		error.printStackTrace();
	}

	/**
	 * Shows the message with newLine characters \r and \n exposed for debugging
	 */
	public static void debugNewlineChars( String msg ) {
		String output = msg.replace( "\r\n", "\\r\\n_DB_BREAK_" );
		output = output.replace( "\r", "\\r_DB_BREAK_" );
		output = output.replace( "\n", "\\n_DB_BREAK_" );
		output = output.replace( "_DB_BREAK_", "\n" );
		Log.d( output );
	}
}
