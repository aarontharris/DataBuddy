package com.leap12.dbexample;

import com.leap12.common.ClientConnection;
import com.leap12.databuddy.BaseConnectionDelegate;

public class MySpecialConnection extends BaseConnectionDelegate {

	/**
	 * This connection only gets called if this connection was active
	 */
	@Override
	protected void onConnectionOpened() throws Exception {
		super.onConnectionOpened();
	}

	@Override
	protected void onAttached(ClientConnection connection) throws Exception {
		connection.setInactivityTimeout(10000);
		connection.setKeepAlive(false); // we don't know the client protocol yet, could be HTTP or GAME
	}

	@Override
	protected void onReceivedMsg(String msg) throws Exception {
		logDebugMessageWithNewlineChars(msg);

		if (msg.contains("HTTP")) {
			getClientConnection().setKeepAlive(false);
			writeMsg(""
					+ "<html>"
					+ "<body>"
					+ "<b>Boo... I'm a webserver...</b>"
					+ "</body>"
					+ "</html>\r\n\r\n");
		}
	}

}
