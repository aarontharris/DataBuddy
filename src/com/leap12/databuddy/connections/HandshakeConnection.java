package com.leap12.databuddy.connections;

import java.util.Map;

import com.leap12.common.Log;

public class HandshakeConnection extends BaseConnection {

	public HandshakeConnection() {
		setInactivityTimeout(15000);
	}

	@Override
	protected void onReceivedMsg(String msg) throws Exception {
		try {
			UserConnection connection = handleAuthenticateUser(msg);
			getClientConnection().setDelegate(connection);
		} catch (Exception e) {
			Log.e(e);
			writeLnMsgSafe(e.getMessage());
			getClientConnection().stop();
		}
	}

	/**
	 * expects:
	 * 
	 * <pre>
	 * begin;request_auth=user&username=theUsername&password=thePassword;end
	 * or
	 * begin;request_auth=sysop&username=theUsername&password=thePassword;end
	 * </pre>
	 * @param msg
	 * @return Appropriate connection;
	 */
	private UserConnection handleAuthenticateUser(String msg) throws Exception {
		if (msg.contains("request_auth")) {
			try {
				Map<String, String> request = getCmdBeginMap(msg);
				for (String key : request.keySet()) {
					Log.d("'%s'=>'%s'", key, request.get(key));
				}

				String authType = request.get("request_auth");
				if ("user".equals(authType)) {
					// TODO validate
					return new UserConnection();
				} else if ("sysop".equals(authType)) {
					// TODO validate
					return new SysOpConnection();
				}
			} catch (Exception e) {
				writeLnMsgSafe("ERR: " + e.getMessage());
			}
		}
		throw new Exception("ungentlemanly introductions");
	}

}
