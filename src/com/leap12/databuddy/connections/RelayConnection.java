package com.leap12.databuddy.connections;

import com.leap12.common.ClientConnection;
import com.leap12.common.Log;
import com.leap12.databuddy.BaseConnection;
import com.leap12.databuddy.Commands;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.Command;
import com.leap12.databuddy.Commands.Role;

public class RelayConnection extends BaseConnection {

	private static final Command<?>[] commands = new Command[] {
			Commands.CMD_RELAY
	};

	public Role getRole() {
		return Role.user;
	}

	@Override
	protected void onAttached(ClientConnection connection) throws Exception {
		connection.setInactivityTimeout(0);
		connection.setKeepAlive(true);
	}

	@Override
	protected void onReceivedMsg(String msg) throws Exception {
		for (Command<?> cmd : commands) {
			if (cmd.isCommand(msg)) {
				CmdResponse<?> response = cmd.executeCommand(this, msg);
				writeResponse(response);
				if (response.getError() != null) {
					Log.e(response.getError());
				}
			}
		}
	}
	@Override
	protected void onReceivedQuit() throws Exception {
		writeLnMsgSafe("quitting");
	}

}
