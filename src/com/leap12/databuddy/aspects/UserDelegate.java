package com.leap12.databuddy.aspects;

import com.leap12.common.ClientConnection;
import com.leap12.common.Log;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.Commands;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.Role;
import com.leap12.databuddy.Commands.StrCommand;

/**
 * This is a connection tailored for a user in need of a persistent connection. Ideal for pushing an pulling data without having to reconnect each
 * time as the connection remains open until explicitly closed by client or server.
 */
public class UserDelegate extends BaseConnectionDelegate {

	private static final StrCommand<?>[] commands = new StrCommand[] {
			Commands.CMD_HELP,
			Commands.CMD_PUT,
			Commands.CMD_GET,
			Commands.CMD_RELAY,
	};

	public Role getRole() {
		return Role.user;
	}

	@Override
	protected void onAttached(ClientConnection connection) throws Exception {
		connection.setInactivityTimeout(120000); // 2 minute -- TODO: client will need to detect disconnect and reconnect.
		connection.setKeepAlive(true);
	}

	@Override
	protected void onReceivedMsg(String msg) throws Exception {
		Log.debugNewlineChars(msg);

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
