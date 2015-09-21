package com.leap12.databuddy.aspects;

import com.leap12.common.ClientConnection;
import com.leap12.common.Log;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.Commands;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.Role;
import com.leap12.databuddy.Commands.StrCommand;

/**
 * Like A UserConnection in that it remains open, but this connection is meant to be able to run parallel to the standard UserConnection as a client
 * may want a UserConnection for the user to issue commands and a separate RelayConnection to listen for data being broadcast from the server or other
 * users.
 */
public class RelayDelegate extends BaseConnectionDelegate {

	private static final StrCommand<?>[] commands = new StrCommand[] {
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
		for (StrCommand<?> cmd : commands) {
			if (1.0f == cmd.isCommand(msg)) {
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
