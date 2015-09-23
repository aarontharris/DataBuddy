package com.leap12.databuddy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.leap12.common.ClientConnection;
import com.leap12.common.ConnectionDelegate;
import com.leap12.common.Log;
import com.leap12.common.Pair;
import com.leap12.common.StrUtl;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.RequestStatus;
import com.leap12.databuddy.data.Dao;
import com.leap12.databuddy.data.DataStore;

public class BaseConnectionDelegate extends ConnectionDelegate {

	private DataStore db;

	@Override
	protected void onAttached(ClientConnection connection) throws Exception {
		connection.setInactivityTimeout(10000);
		connection.setKeepAlive(false); // we don't know the client protocol yet, lets assume close when done unless told otherwise
	}

	/**
	 * Only available while this delegate is attached to the ClientConnection
	 */
	public DataStore getDb() {
		if (db == null) {
			db = Dao.getInstance(this);
		}
		return db;
	}

	public final void writeResponse(CmdResponse<?> response) {
		try {
			if (response != null) {
				RequestStatus status = response.getStatus();
				if (status == null) {
					throw new NullPointerException("Status is null");
				}

				String value;
				int statusCode = response.getStatus().getCode();

				if (response != null) {
					switch (response.getStatus()) {
					case SUCCESS:
						if (Void.class.equals(response.getType())) {
							writeLnMsgSafe("OK");
						} else {
							value = response.getValue() == null ? null : String.valueOf(response.getValue());
							writeResponseWithStatus(value, statusCode, response.getType().getSimpleName());
						}
						break;
					default:
						value = response.getStatusMessage();
						if (value == null) {
							value = response.getError().getMessage();
						}
						writeResponseWithStatus(value, statusCode, String.class.getSimpleName());
						break;
					}
				}
			} else {
				Log.e(new NullPointerException("Response was null"));
			}
		} catch (Exception e) {
			Log.e(e); // if a failure occurs mid-write, the client will know it didn't receive the full payload and re-request.
		}
	}
	public final void writeErrorResponse(String errMsg, Throwable err) {
		try {
			String msg = errMsg;
			if (msg == null) {
				msg = err.getMessage();
			}
			writeResponseWithStatus(msg, Commands.toRequestStatus(err).getCode(), String.class.getSimpleName());
		} catch (Exception e) {
			Log.e(e);
		}
	}

	protected Map<String, String> getCmdBeginMap(String msg) throws Exception {
		if (isCmdBegin(msg)) {
			String[] parts = msg.split(";");
			if (isCmdBegin(parts[0]) && isCmdEnd(parts[parts.length - 1])) {
				return StrUtl.toMap(parts[1], "=", "&");
			} else {
				throw new IllegalStateException("begin without end");
			}
		}
		return Collections.emptyMap();
	}

	protected List<Pair<String, String>> getCmdBeginPairs(String msg) throws Exception {
		if (isCmdBegin(msg)) {
			String[] parts = msg.split(";");
			if (isCmdBegin(parts[0]) && isCmdEnd(parts[parts.length - 1])) {
				return StrUtl.toPairs(parts[1], "=", "&");
			} else {
				throw new IllegalStateException("begin without end");
			}
		}
		return Collections.emptyList();
	}

	protected boolean isCmdBegin(String msg) {
		return StrUtl.startsWith(msg, "begin");
	}

	protected boolean isCmdEnd(String msg) {
		return StrUtl.startsWith(msg, "end");
	}

	protected boolean isCmdPut(String msg) {
		return StrUtl.startsWith(msg, "put: ");
	}

	protected boolean isCmdGet(String msg) {
		return StrUtl.startsWith(msg, "get: ");
	}

	@Override
	protected final void doAttached(ClientConnection connection) throws Exception {
		connection.setLineSeparator(Config.get().getLineSeparator());
		super.doAttached(connection);
	}

	@Override
	protected final void doConnectionOpened() throws Exception {
		super.doConnectionOpened();
	}

	@Override
	protected final void doReceivedMsg(String msg) throws Exception {
		super.doReceivedMsg(msg);
	}

	@Override
	protected final void doReceivedQuit() throws Exception {
		super.doReceivedQuit();
	}

	@Override
	protected final void doDetatched() throws Exception {
		super.doDetatched();
		if (db != null) {
			Dao.releaseInstance(this);
		}
	}

	@Override
	public void finalize() {
		Log.e("BaseConnection.finalize()");
		try {
			super.finalize();
		} catch (Throwable e) {
			Log.e(e);
		}
	}
}
