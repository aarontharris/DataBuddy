package com.leap12.databuddy.commands;

import com.leap12.databuddy.BaseConnection;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.Command;

public class RelayCmd extends Command<Void> {

	public RelayCmd(String cmdName, String cmdFormat, Class<Void> type) {
		super(cmdName, cmdFormat, type);
	}

	@Override
	public CmdResponse<Void> executeCommand(BaseConnection connection, String msg) {
		// FIXME: relay to other connected users without storing to the database
		// FIXME: relay needs to support groups, to only relay to people in that group
		return null;
	}

}
