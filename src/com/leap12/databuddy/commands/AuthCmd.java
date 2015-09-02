package com.leap12.databuddy.commands;

import java.util.Map;
import com.leap12.common.Log;
import com.leap12.common.Preconditions;
import com.leap12.common.StrUtl;
import com.leap12.databuddy.BaseConnection;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.CmdResponse.CmdResponseMutable;
import com.leap12.databuddy.Commands.Command;
import com.leap12.databuddy.Commands.RequestStatus;
import com.leap12.databuddy.Commands.Role;

public class AuthCmd extends Command<Role> {
	private final int beginIndex;

	public AuthCmd() {
		super("auth",
				"auth request_auth=[ROLE]&username=[USERNAME]&password=[PASSWORD]",
				Role.class);
		beginIndex = (getName() + " ").length();
	}

	@Override
	public CmdResponse<Role> executeCommand(BaseConnection connection, String msg) {
		final CmdResponseMutable<Role> response = new CmdResponseMutable<>(Role.class);
		try {
			msg = msg.substring(beginIndex, msg.length()); // from=zero-based-inclusive, to=zero-based-inclusive
			Map<String, String> fields = StrUtl.toMap(msg, "=", "&");
			String roleStr = fields.get("request_auth");

			try {
				Role role = Role.fromValue(roleStr);
				response.setValue(role);
			} catch (NullPointerException | IllegalArgumentException e) {
				response.setError(e, RequestStatus.FAIL_INVALID_CMD_ARGUMENTS, "Invalid Role " + roleStr);
			} catch (Exception e) {
				response.setError(e, RequestStatus.FAIL_UNKNOWN, "Something went wrong when parsing the role " + roleStr);
			}

			if (response.getValue() != null) {
				String userStr = fields.get("username");
				String passStr = fields.get("password");
				Log.d("Received Request of Role %s for '%s' '%s'", roleStr, userStr, passStr);

				Preconditions.isValidArg(StrUtl.isNotEmptyAll(roleStr, userStr, passStr)); // throws if empty

				response.setArgs().putString("username", userStr);
				response.setArgs().putString("password", userStr);
				response.setStatus(RequestStatus.SUCCESS);
			}
		} catch (IllegalArgumentException e) {
			response.setError(e, RequestStatus.FAIL_INVALID_CMD_ARGUMENTS, e.getMessage());
		} catch (Exception e) {
			response.setError(e, RequestStatus.FAIL_INVALID_CMD_FORMAT);
		}
		return response;
	}

	@Override
	public Class<?> getDisplayType() {
		return Void.class;
	}
}
