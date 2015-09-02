package com.leap12.databuddy.connections;

import com.leap12.common.Log;
import com.leap12.databuddy.BaseConnection;
import com.leap12.databuddy.Commands;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.Command;
import com.leap12.databuddy.Commands.Role;

public class UserConnection extends BaseConnection {

	private static final Command<?>[] commands = new Command[] {
			Commands.CMD_HELP,
			Commands.CMD_PUT,
			Commands.CMD_GET,
	};

	public Role getRole() {
		return Role.user;
	}

	@Override
	protected void onConnectionOpened() throws Exception {
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
