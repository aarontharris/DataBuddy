package com.leap12.common.ref;


public class RefInt {

	private int val = 0;

	public RefInt( int val ) {
		set( val );
	}

	public int get() {
		return val;
	}

	public void set( int val ) {
		this.val = val;
	}

}
