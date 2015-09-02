package com.leap12.databuddy.connections;

import com.leap12.databuddy.Commands.Role;
import com.leap12.databuddy.DataBuddy;

public class SysOpConnection extends UserConnection {

	@Override
	public Role getRole() {
		return Role.sysop;
	}

	@Override
	protected void onReceivedMsg(String msg) throws Exception {
		if ("get connection count".equals(msg)) {
			int count = DataBuddy.get().getConnectionCount();
			writeLnMsgSafe(String.valueOf(count));
		} else if ("gc".equals(msg)) {
			System.gc();
		} else {
			super.onReceivedMsg(msg);
		}
	}

}
