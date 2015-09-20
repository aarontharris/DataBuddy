package com.leap12.databuddy.commands;

import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.CmdResponse.CmdResponseMutable;
import com.leap12.databuddy.Commands.Command;
import com.leap12.databuddy.Commands.RequestStatus;

public class HelpCmd extends Command<String> {

	public HelpCmd() {
		super("help", "help", String.class);
	}

	@Override
	public CmdResponse<String> executeCommand(BaseConnectionDelegate connection, String msg) {
		// List<Command<?>> commands = new ArrayList<>(Commands.get().getCommands(connection.getRole()));
		// Collections.sort(commands, Commands.COMMAND_COMPARATOR);
		// for (Command<?> command : commands) {
		// connection.writeLnMsgSafe(String.format("%s"));
		// }
		CmdResponse<String> out = new CmdResponseMutable<String>(String.class,
				"Help me! Help me! ... Hahahaha! No one can hear you!",
				RequestStatus.SUCCESS);
		return out;
	}
}
