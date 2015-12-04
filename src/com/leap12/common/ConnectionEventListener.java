package com.leap12.common;

public interface ConnectionEventListener {
	void onConnectionOpened();

	void onReceivedMsg( final String msg );

	void onReceivedQuit();
}
