package com.leap12.common;

public class ClientConnectionDelegate {
	private ClientConnection mConnection;
	private long mInactivityTimeoutMillis = 0L;

	protected void doAttached(ClientConnection connection) throws Exception {
		this.mConnection = connection;
		onAttached(connection);
	}

	protected void onAttached(ClientConnection connection) throws Exception {
		// Log.d(getClass().getName() + " onAttached");
	}

	protected void doConnectionOpened() throws Exception {
		onConnectionOpened();
	}

	protected void onConnectionOpened() throws Exception {
		// Log.d(getClass().getName() + " onConnectionOpened");
	}

	protected void doReceivedMsg(String msg) throws Exception {
		onReceivedMsg(msg);
	}

	protected void onReceivedMsg(String msg) throws Exception {
		// Log.d(getClass().getName() + " onReceivedMsg( %s )", msg);
	}

	protected void doReceivedQuit() throws Exception {
		onReceivedQuit();
	}

	protected void onReceivedQuit() throws Exception {
		// Log.d(getClass().getName() + " onReceivedQuit");
	}

	protected void doDetatched() throws Exception {
		onDetatched();
		this.mConnection = null;
	}

	protected void onDetatched() throws Exception {
		// Log.d(getClass().getName() + " onDetached");
	}

	/** Will return null if the Delegate has been detatched */
	protected final ClientConnection getClientConnection() {
		return mConnection;
	}

	protected final void setInactivityTimeout(long millis) {
		mInactivityTimeoutMillis = millis;
	}

	protected final long getInactivityTimeout() {
		return mInactivityTimeoutMillis;
	}

	/** No-op if detatched from the connection */
	public final void writeLnMsg(String msg) throws Exception {
		if (mConnection != null) {
			mConnection.writeLnMsg(msg);
		}
	}

	/** never throws - convenience for when you don't care about errors */
	public final void writeLnMsgSafe(String msg) {
		if (mConnection != null) {
			mConnection.writeLnMsgSafe(msg);
		}
	}

	public final void writeMsg(String msg) throws Exception {
		if (mConnection != null) {
			mConnection.writeMsg(msg);
		}
	}

	/** never throws - convenience for when you don't care about errors */
	public final void writeMsgSafe(String msg) {
		if (mConnection != null) {
			mConnection.writeMsgSafe(msg);
		}
	}

}
