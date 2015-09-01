package com.leap12.databuddy.commands;

import java.util.Map;
import com.leap12.common.Log;
import com.leap12.common.Preconditions;
import com.leap12.common.StrUtl;
import com.leap12.databuddy.BaseConnection;
import com.leap12.databuddy.Commands.CmdRequest;
import com.leap12.databuddy.Commands.CmdRequest.CmdRequestMutable;
import com.leap12.databuddy.Commands.CmdRequest.RequestStatus;
import com.leap12.databuddy.Commands.Command;
import com.leap12.databuddy.Commands.Role;

public class AuthCmd extends Command<Role> {
	private int beginIndex = 0;

	public AuthCmd() {
		super("auth",
				"auth request_auth=[ROLE]&username=[USERNAME]&password=[PASSWORD]",
				Role.class);
		beginIndex = (getName() + " ").length();
	}

	@Override
	public CmdRequest<Role> parseCommand(BaseConnection connection, String msg) {
		CmdRequestMutable<Role> request = new CmdRequestMutable<>();
		try {
			msg = msg.substring(beginIndex, msg.length() - 1); // from=zero-based-inclusive, to=zero-based-inclusive
			Map<String, String> fields = StrUtl.toMap(msg, "=", "&");
			String roleStr = fields.get("request_auth");

			try {
				Role role = Role.fromValue(roleStr);
				request.setValue(role);
			} catch (NullPointerException | IllegalArgumentException e) {
				request.setError(e, RequestStatus.FAIL_INVALID_CMD_ARGUMENTS, "Invalid Role " + roleStr);
			} catch (Exception e) {
				request.setError(e, RequestStatus.FAIL_UNKNOWN, "Something went wrong when parsing the role " + roleStr);
			}

			if (request.getValue() != null) {
				String userStr = fields.get("username");
				String passStr = fields.get("password");
				Log.d("Received Request of Role %s for '%s' '%s'", roleStr, userStr, passStr);

				Preconditions.isValidArg(StrUtl.isNotEmptyAll(roleStr, userStr, passStr)); // throws if empty

				request.setArgs().putString("username", userStr);
				request.setArgs().putString("password", userStr);
				request.setStatus(RequestStatus.SUCCESS);
			}
		} catch (IllegalArgumentException e) {
			request.setError(e, RequestStatus.FAIL_INVALID_CMD_ARGUMENTS, e.getMessage());
		} catch (Exception e) {
			request.setError(e, RequestStatus.FAIL_INVALID_CMD_FORMAT);
		}
		return request;
	}

	@Override
	public Class<?> getDisplayType() {
		return Void.class;
	}
}
