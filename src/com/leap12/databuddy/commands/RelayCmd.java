package com.leap12.databuddy.commands;

import com.leap12.common.ClientConnection;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.Command;
import com.leap12.databuddy.Commands.RequestStatus;
import com.leap12.databuddy.DataBuddy;

public class RelayCmd extends Command<Void> {

	public RelayCmd() {
		super("relay", "relay [msg]", Void.class);
	}

	@Override
	public CmdResponse<Void> executeCommand(BaseConnectionDelegate connection, String msg) {
		// FIXME: relay to other connected users without storing to the database
		// FIXME: relay needs to support groups, to only relay to people in that group

		ClientConnection clientConnection = connection.getClientConnection();
		if (clientConnection != null) {
			DataBuddy.get().relayMessage(msg, clientConnection);
		}
		return new CmdResponse<Void>(Void.class, null, RequestStatus.SUCCESS);
	}

}
