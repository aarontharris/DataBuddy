package com.leap12.common;

import java.util.List;

public class ConnectionDelegate {
	private ClientConnection mConnection;

	protected void doAttached(ClientConnection connection) throws Exception {
		this.mConnection = connection;
		onAttached(connection);
	}

	/**
	 * Delegate is always attached before all else
	 * @param connection The connection the delegate is attached to
	 * @throws Exception
	 */
	protected void onAttached(ClientConnection connection) throws Exception {
		// Log.d(getClass().getName() + " onAttached");
	}

	protected void doConnectionOpened() throws Exception {
		onConnectionOpened();
	}

	/**
	 * Only called if this is the delegate that opened the socket
	 */
	protected void onConnectionOpened() throws Exception {
		// Log.d(getClass().getName() + " onConnectionOpened");
	}

	protected void doReceivedMsg(String msg) throws Exception {
		onReceivedMsg(msg);
	}

	/**
	 * Always called when a non-emptpy non-quit msg is received from the client
	 */
	protected void onReceivedMsg(String msg) throws Exception {
		// Log.d(getClass().getName() + " onReceivedMsg( %s )", msg);
	}

	protected void doReceivedQuit() throws Exception {
		onReceivedQuit();
	}

	/**
	 * Only called if this is the delegate that received the quit msg
	 */
	protected void onReceivedQuit() throws Exception {
		// Log.d(getClass().getName() + " onReceivedQuit");
	}

	protected void doDetatched() throws Exception {
		onDetatched();
		this.mConnection = null;
	}

	/**
	 * Always called before the delegate is fully detached and destroyed
	 */
	protected void onDetatched() throws Exception {
		// Log.d(getClass().getName() + " onDetached");
	}

	/** Will return null if the Delegate has been detatched */
	protected final ClientConnection getClientConnection() {
		return mConnection;
	}

	/**
	 * Auto-Flushes every line see {@link #writeLnMsgSafe(String, boolean)} if you want to batch never throws - convenience for when you don't care
	 * about errors
	 * */
	public final void writeLnMsgSafe(String msg) {
		if (mConnection != null) {
			mConnection.writeLnMsgSafe(msg);
		}
	}

	public final void writeLnMsg(String msg) throws Exception {
		if (mConnection != null) {
			mConnection.writeLnMsg(msg);
		}
	}

	public final void writeMsg(String msg) throws Exception {
		if (mConnection != null) {
			mConnection.writeMsg(msg);
		}
	}

	/**
	 * Write a message to the client.
	 * @param msg - the message
	 * @param appendNewLines - number of newline chars to be appended to the message
	 * @param flush - flush the stream after write
	 * @throws Exception - if something went wrong during the write
	 */
	public final void writeMsg(String msg, int appendNewLines) throws Exception {
		if (mConnection != null) {
			mConnection.writeMsg(msg, appendNewLines);
		}
	}

	public final void writeResponse(String msg) throws Exception {
		writeResponse(msg, "String");
	}

	public final void writeResponse(String msg, String dataFormat) throws Exception {
		int length = 0;
		if (msg != null) {
			length = msg.length();
		}
		writeLnMsg("BEGIN format=" + dataFormat + "&length=" + length);
		if (msg != null) {
			writeLnMsg(msg);
		}
		writeLnMsg("END");
	}

	public final void writeResponseWithStatus(String msg, int status, String dataFormat) throws Exception {
		int length = 0;
		if (msg != null) {
			length = msg.length();
		}
		writeLnMsg("BEGIN status=" + status + "&format=" + dataFormat + "&length=" + length);
		if (msg != null) {
			writeLnMsg(msg);
		}
		writeLnMsg("END");
	}

	/**
	 * Write a batch list of messages. Each line will be transmitted as you've provided it, however a terminating newLine character will be appended
	 * to the batch regardless of if whether each line has a newline charater or not.
	 * @param msgs
	 * @param dataFormat
	 * @param appendNewLineToEachListItem
	 * @throws Exception
	 */
	public final void writeBatchResponse(List<String> msgs, String dataFormat, boolean appendNewLineToEachListItem) throws Exception {
		if (mConnection != null) {
			StringBuilder sb = new StringBuilder();
			for (String msg : msgs) {
				sb.append(msg);
				if (appendNewLineToEachListItem) {
					sb.append(mConnection.getLineSeparator());
				}
			}
			String msg = sb.toString();

			writeLnMsg("BEGIN format=" + dataFormat + "&length=" + msg.length());
			writeLnMsg(msg);
			writeLnMsg("END");
		}
	}
}
