package com.leap12.databuddy.commands;

import com.leap12.databuddy.BaseConnection;
import com.leap12.databuddy.Commands.CmdRequest;
import com.leap12.databuddy.Commands.CmdRequest.CmdRequestMutable;
import com.leap12.databuddy.Commands.CmdRequest.RequestStatus;
import com.leap12.databuddy.Commands.Command;

public class HelpCmd extends Command<String> {

	public HelpCmd() {
		super("help", "help", String.class);
	}

	@Override
	public CmdRequest<String> parseCommand(BaseConnection connection, String msg) {
		// List<Command<?>> commands = new ArrayList<>(Commands.get().getCommands(connection.getRole()));
		// Collections.sort(commands, Commands.COMMAND_COMPARATOR);
		// for (Command<?> command : commands) {
		// connection.writeLnMsgSafe(String.format("%s"));
		// }
		CmdRequest<String> out = new CmdRequestMutable<String>("Help me! Help me! ... No one can hear you hahahaha", RequestStatus.SUCCESS);
		return out;
	}
}
