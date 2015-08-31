package com.leap12.databuddy.connections;

import com.leap12.common.ClientConnectionDelegate;

public class UserConnection extends ClientConnectionDelegate {

	@Override
	protected void onConnectionOpened() throws Exception {
	}

	@Override
	protected void onReceivedMsg(String msg) throws Exception {
	}

	@Override
	protected void onReceivedQuit() throws Exception {
		writeLnMsgSafe("quitting");
	}

}
