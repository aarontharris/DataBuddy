package com.leap12.databuddy.connections;

import com.leap12.databuddy.DataBuddy;

public class SysOpConnection extends UserConnection {

	@Override
	protected void onReceivedMsg(String msg) throws Exception {
		if ("help".equals(msg)) {
			writeLnMsgSafe("'help' as String - display this menu");
			writeLnMsgSafe("'get connection count' as int - number of connections");
			writeLnMsgSafe("'gc' as void - garbage collect");
		} else if ("get connection count".equals(msg)) {
			int count = DataBuddy.get().getConnectionCount();
			writeLnMsgSafe(String.valueOf(count));
		} else if ("gc".equals(msg)) {
			System.gc();
		}
	}

}
