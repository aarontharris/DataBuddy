package com.leap12.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientConnection {
	private static ClientConnectionDelegate DEFAULT_DELEGATE = new ClientConnectionDelegate();

	private final Socket socket;
	private ClientConnectionDelegate delegate;
	private CopyOnWriteArrayList<WeakReference<ConnectionEventListener>> listeners;
	private boolean running = false;

	public ClientConnection(Socket socket) {
		this.socket = socket;
		setDelegate(DEFAULT_DELEGATE);
	}

	@Override
	public void finalize() {
		Log.e("ClientConnection.finalize()");
		try {
			super.finalize();
		} catch (Throwable e) {
			Log.e(e);
		}
	}

	public Socket getSocket() {
		return socket;
	}

	public void setDelegate(ClientConnectionDelegate delegate) {
		try {
			if (this.delegate != null) {
				this.delegate.doDetatched();
			}
		} catch (Exception e) {
			Log.e(e);
		}
		this.delegate = delegate == null ? DEFAULT_DELEGATE : delegate;
		try {
			if (this.delegate != null) {
				this.delegate.doAttached(this);
			}
		} catch (Exception e) {
			Log.e(e);
		}
	}

	public synchronized void registerListener(ConnectionEventListener listener) {
		if (listener != null) {
			if (listeners == null) {
				listeners = new CopyOnWriteArrayList<>();
			}
			listeners.add(new WeakReference<>(listener));
		}
	}

	public synchronized void unregisterListener(ConnectionEventListener listener) {
		if (listener != null) {
			if (listeners != null) {
				for (WeakReference<ConnectionEventListener> weakListener : listeners) {
					try {
						ConnectionEventListener aListener = weakListener.get();
						if (listener == aListener || aListener == null) {
							listeners.remove(weakListener);
						}
					} catch (Exception e) {
						Log.e(e);
					}
				}
			}
		}
	}

	public final void writeLnMsg(String msg) throws Exception {
		writeMsg(msg, true);
	}

	/** never throws - convenience for when you don't care about errors */
	public final void writeLnMsgSafe(String msg) {
		try {
			writeLnMsg(msg);
		} catch (Exception e) {
			Log.e(e);
		}
	}

	public final void writeMsg(String msg) throws Exception {
		writeMsg(msg, false);
	}

	/** never throws - convenience for when you don't care about errors */
	public final void writeMsgSafe(String msg) {
		try {
			writeMsg(msg);
		} catch (Exception e) {
			Log.e(e);
		}
	}

	public final boolean isSocketOpen() {
		return isConnected() && running;
	}

	public final void stop() {
		try {
			doReceivedQuit();
		} catch (Exception e) {
			Log.e(e);
		}
	}

	public final void run() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (isConnected()) {
						doConnectionOpened();

						running = true;
						socket.setKeepAlive(true);

						InputStream in = socket.getInputStream();
						BufferedInputStream bIn = new BufferedInputStream(in);
						byte[] bufferedData = new byte[1024];
						boolean running = true;
						while (running) {
							int bytesRead = 0;
							int data = 10;

							handleInactivity(bIn);

							while ((data = bIn.read()) != -1 && data != 10 && data != 13) {
								bytesRead++;
								bufferedData[bytesRead - 1] = (byte) data;
							}
							if (bytesRead > 0) {
								String msg = new String(bufferedData, 0, bytesRead);
								processMessage(msg);
							}
						}
					}
					socket.close();
				} catch (IOException e) {
					if (running) {
						Log.e(e);
					}
				} catch (Exception e) {
					Log.e(e);
				}
			}
		}).start();
	}

	private void handleInactivity(InputStream in) throws Exception {
		long timeout = delegate.getInactivityTimeout();
		if (timeout > 0) {
			long start = System.currentTimeMillis();
			while (in.available() == 0) {
				long now = System.currentTimeMillis();
				long delta = now - start;
				if (delta > timeout) {
					ClientConnection.this.stop();
				}
				Thread.sleep(20);
			}
		}
	}

	private void processMessage(String msg) {
		try {
			if ("quit".equals(msg)) {
				doReceivedQuit();
			} else if ("ping".equals(msg)) {
				writeLnMsg("pong");
			} else {
				doReceivedMsg(msg);
			}
		} catch (Exception e) {
			Log.e(e);
		}
	}

	private void doConnectionOpened() throws Exception {
		delegate.doConnectionOpened(); // fail here bounce out to skip listeners
		if (listeners != null) {
			for (WeakReference<ConnectionEventListener> weakListener : listeners) {
				try {
					ConnectionEventListener listener = weakListener.get();
					if (listener != null) {
						listener.onConnectionOpened();
					}
				} catch (Exception e) {
					Log.e(e);
				}
			}
		}
	}

	private void doReceivedMsg(final String msg) throws Exception {
		delegate.doReceivedMsg(msg); // fails here should bounce out to skip listeners
		if (listeners != null) {
			for (WeakReference<ConnectionEventListener> weakListener : listeners) {
				try {
					ConnectionEventListener listener = weakListener.get();
					if (listener != null) {
						listener.onReceivedMsg(msg);
					}
				} catch (Exception e) {
					Log.e(e);
				}
			}
		}
	}

	private void doReceivedQuit() throws Exception {
		try {
			if (listeners != null) {
				for (WeakReference<ConnectionEventListener> weakListener : listeners) {
					try {
						ConnectionEventListener listener = weakListener.get();
						if (listener != null) {
							listener.onReceivedQuit();
						}
					} catch (Exception e) {
						Log.e(e);
					}
				}
			}
		} catch (Exception e) { // a little overcautious but we can't have any listeners mucking up our delegate
			Log.e(e);
		}
		try {
			delegate.doReceivedQuit();
		} catch (Exception e) { // also overcautious but we want to ensure that we set the delegate to null for proper cycle
			Log.e(e);
		}
		try {
			setDelegate(null);
		} catch (Exception e) { // cant not close the socket and a finally doesnt make sense because this is all progressive
			Log.e(e);
		}
		running = false;
		socket.close();
	}

	private boolean isConnected() {
		if (socket != null && socket.isBound() && socket.isConnected() && !socket.isClosed() && !socket.isOutputShutdown()
				&& !socket.isInputShutdown()) {
			return true;
		}
		return false;
	}

	private final void writeMsg(String msg, boolean includeNewline) throws Exception {
		if (isSocketOpen()) {
			OutputStream out = socket.getOutputStream();
			BufferedOutputStream bOut = new BufferedOutputStream(out);
			bOut.write(msg.getBytes());
			if (includeNewline) {
				bOut.write('\n');
			}
			bOut.flush();
		}
	}
}
