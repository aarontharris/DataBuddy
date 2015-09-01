package com.leap12.databuddy.connections;

import com.leap12.databuddy.BaseConnection;
import com.leap12.databuddy.Commands;
import com.leap12.databuddy.Commands.CmdRequest;
import com.leap12.databuddy.Commands.CmdRequest.RequestStatus;
import com.leap12.databuddy.Commands.Role;

public class UserConnection extends BaseConnection {

	public Role getRole() {
		return Role.user;
	}

	@Override
	protected void onConnectionOpened() throws Exception {
	}

	@Override
	protected void onReceivedMsg(String msg) throws Exception {
		if (Commands.CMD_HELP.isCommand(msg)) {
			CmdRequest<String> request = Commands.CMD_HELP.parseCommand(this, msg);
			if (RequestStatus.SUCCESS == request.getStatus()) {
				writeResponse(request.getValue());
			} else {
				writeFailResponse(msg, request);
			}
		}
	}
	@Override
	protected void onReceivedQuit() throws Exception {
		writeLnMsgSafe("quitting");
	}

}
