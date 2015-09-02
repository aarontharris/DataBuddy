package com.leap12.databuddy.commands;

import java.util.Map;
import com.leap12.common.StrUtl;
import com.leap12.databuddy.BaseConnection;
import com.leap12.databuddy.Commands;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.CmdResponse.CmdResponseMutable;
import com.leap12.databuddy.Commands.Command;
import com.leap12.databuddy.Commands.DBuddyArgsException;
import com.leap12.databuddy.Commands.RequestStatus;

public class PutCmd extends Command<Void> {
	private final int beginIndex;

	public PutCmd() {
		super("put",
				"put topic=[topic]&subtopic=[subtopic]&key=[key]&data=[data]",
				Void.class);
		beginIndex = (getName() + " ").length();
	}

	@Override
	public CmdResponse<Void> executeCommand(BaseConnection connection, String msg) {
		final CmdResponseMutable<Void> response = new CmdResponseMutable<>(Void.class);
		response.setStatus(RequestStatus.UNFULFILLED);
		try {
			msg = msg.substring(beginIndex, msg.length()); // from=zero-based-inclusive, to=zero-based-inclusive
			Map<String, String> fields = StrUtl.toMap(msg, "=", "&");
			String topic = fields.get("topic");
			String subtopic = fields.get("subtopic");
			String key = fields.get("key");
			String data = fields.get("data");
			if (StrUtl.isEmptyAny(topic, subtopic, key, data)) {
				throw new DBuddyArgsException("invalid put command", null);
			}
			connection.getDb().saveString(topic, subtopic, key, data);
			response.setValue(null, RequestStatus.SUCCESS);
		} catch (Exception e) {
			response.setError(e, Commands.toRequestStatus(e));
		}
		return response;
	}
}
