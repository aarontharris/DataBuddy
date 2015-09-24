package com.leap12.common;

public class Preconditions {

	/**
	 * @param trueStatement
	 * @throws IllegalStateException when trueStatement is not true
	 */
	public static void isTrueState( boolean trueStatement ) throws IllegalStateException {
		if ( !trueStatement ) {
			throw new IllegalStateException();
		}
	}

	/**
	 * @param trueStatement
	 * @param reason
	 * @throws IllegalStateException when trueStatement is not true
	 */
	public static void isTrueState( boolean trueStatement, String reason ) throws IllegalStateException {
		if ( !trueStatement ) {
			throw new IllegalStateException( reason );
		}
	}

	/**
	 * @param trueStatement
	 * @throws IllegalStateException when trueStatement is not true
	 */
	public static void isValidArg( boolean trueStatement ) throws IllegalStateException {
		if ( !trueStatement ) {
			throw new IllegalArgumentException();
		}
	}

	/**
	 * @param trueStatement
	 * @param reason
	 * @throws IllegalStateException when trueStatement is not true
	 */
	public static void isValidArg( boolean trueStatement, String reason ) throws IllegalStateException {
		if ( !trueStatement ) {
			throw new IllegalArgumentException( reason );
		}
	}

}
