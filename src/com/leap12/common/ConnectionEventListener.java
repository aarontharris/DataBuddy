package com.leap12.common;

public interface ConnectionEventListener {
	void onConnectionOpened();

	void onReceivedMsg( String msg );

	void onReceivedQuit();
}
