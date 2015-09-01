package com.leap12.databuddy.commands;

import com.leap12.databuddy.BaseConnection;
import com.leap12.databuddy.Commands.CmdRequest;
import com.leap12.databuddy.Commands.Command;

public class GetCmd extends Command<String> {

	public GetCmd() {
		super("get",
				"get [topic] [subtopic] [key]",
				String.class);
	}

	@Override
	public CmdRequest<String> parseCommand(BaseConnection connection, String msg) {
		return null;
	}

}
