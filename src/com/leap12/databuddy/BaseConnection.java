package com.leap12.databuddy;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import com.leap12.common.ClientConnection;
import com.leap12.common.ConnectionDelegate;
import com.leap12.common.Log;
import com.leap12.common.Pair;
import com.leap12.common.StrUtl;
import com.leap12.databuddy.Commands.CmdRequest;

public class BaseConnection extends ConnectionDelegate {

	public final void writeFailResponse(String msg, CmdRequest<?> request) {
		try {
			writeResponse(String.format("ERR %s : %s", request.getStatus(), request.getStatusMessage()));
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
