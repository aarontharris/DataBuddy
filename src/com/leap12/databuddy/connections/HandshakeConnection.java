package com.leap12.databuddy.connections;

import com.leap12.common.Log;
import com.leap12.databuddy.BaseConnection;
import com.leap12.databuddy.Commands;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.RequestStatus;
import com.leap12.databuddy.Commands.Role;

public class HandshakeConnection extends BaseConnection {

	public HandshakeConnection() {
		// setInactivityTimeout(15000);
	}

	@Override
	protected void onReceivedMsg(String msg) throws Exception {
		try {
			UserConnection connection = handleAuthenticateUser(msg);
			getClientConnection().setDelegate(connection);
		} catch (Exception e) {
			Log.e(e);
			// writeLnMsgSafe(e.getMessage());
			writeResponse(e.getMessage());
			getClientConnection().stop();
		}
	}

	/**
	 * expects:
	 * 
	 * <pre>
	 * auth request_auth=user&username=theUsername&password=thePassword
	 * or
	 * auth request_auth=sysop&username=theUsername&password=thePassword
	 * </pre>
	 * 
	 * @param msg
	 * @return Appropriate connection;
	 */
	private UserConnection handleAuthenticateUser(String msg) throws Exception {
		if (Commands.CMD_AUTH.isCommand(msg)) {
			CmdResponse<Role> request = Commands.CMD_AUTH.executeCommand(this, msg);
			if (RequestStatus.SUCCESS == request.getStatus()) {

				// TODO validate user -- maybe send them to the appropriate connection and let that connection do the validation? This would better support an anonymous type

				return toConnection(request);
			}
		}
		throw new Exception("invalid command");
	}

	private UserConnection toConnection(CmdResponse<Role> request) {
		Role role = request.getValue();
		switch (role) {
		case sysop:
			return new SysOpConnection();
		case user:
			return new UserConnection();
		}
		throw new IllegalStateException("Unknown Role " + role);
	}

}
