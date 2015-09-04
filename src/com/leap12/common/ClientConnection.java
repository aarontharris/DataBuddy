package com.leap12.common;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientConnection {
	private static ConnectionDelegate DEFAULT_DELEGATE = new ConnectionDelegate();

	private final Socket socket;
	private ConnectionDelegate delegate;
	private CopyOnWriteArrayList<WeakReference<ConnectionEventListener>> listeners;
	private boolean running = false;
	private String mLineSeparator;
	private boolean keepAlive = false;
	private int mInactivityTimeoutMillis = 0;

	public ClientConnection(Socket socket) {
		this.socket = socket;
		setDelegate(DEFAULT_DELEGATE);
		setLineSeparator(System.getProperty("line.separator"));
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

	public void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public boolean getKeepAlive() {
		return keepAlive;
	}

	public int getInactivityTimeout() {
		return mInactivityTimeoutMillis;
	}

	public void setInactivityTimeout(int millis) {
		this.mInactivityTimeoutMillis = millis;
	}

	public void setDelegate(ConnectionDelegate delegate) {
		try {
			if (this.delegate != null) {
				this.delegate.doDetatched();
			}
		} catch (Exception e) {
			Log.e(e);
		}
		this.delegate = delegate == null ? DEFAULT_DELEGATE : delegate; // singular transaction, no period of nullness (thread safety)
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

	/**
	 * Auto-Flushes every line see {@link #writeLnMsgSafe(String, boolean)} if you want to batch never throws - convenience for when you don't care
	 * about errors
	 * */
	public final void writeLnMsgSafe(String msg) {
		try {
			writeLnMsg(msg);
		} catch (Exception e) {
			Log.e(e);
		}
	}

	public final void writeLnMsg(String msg) throws Exception {
		writeMsg(msg, 1);
	}

	public final void writeMsg(String msg) throws Exception {
		writeMsg(msg, 0);
	}

	/**
	 * Write a message to the client.
	 * @param msg - the message
	 * @param appendNewLines - number of newline chars to be appended to the message
	 * @param flush - flush the stream after write
	 * @throws Exception - if something went wrong during the write
	 */
	public final void writeMsg(String msg, int appendNewLines) throws Exception {
		if (isSocketOpen()) {
			OutputStream out = socket.getOutputStream();
			BufferedOutputStream bOut = new BufferedOutputStream(out);
			bOut.write(StrUtl.toBytes(msg));
			if (appendNewLines == 1) {
				bOut.write('\n');
			} else if (appendNewLines > 1) {
				for (int i = 0; i < appendNewLines; i++) {
					bOut.write('\n');
				}
			}
			bOut.flush();
		}
	}

	public void setLineSeparator(String separator) {
		mLineSeparator = separator;
	}

	public String getLineSeparator() {
		return mLineSeparator;
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

	private static final int BUF_SIZE = 32;
	public final void run() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Log.d("New Connection on Port %s", socket.getPort());
				try {
					if (isConnected()) {
						doConnectionOpened();

						running = true;
						socket.setKeepAlive(false);

						InputStream in = socket.getInputStream();
						BufferedInputStream bIn = new BufferedInputStream(in);
						byte[] inputBuffer = new byte[BUF_SIZE];
						//						boolean running = true;

						// keepAlive causes the messaging loop to only run once when false
						// otherwise, it loops to stay alive
						// browsers or general HTTP GET or PUT should keepAlive=false
						// gameClient should keepAlive=true
						while (running) { // keepAlive messaging loop
							try {
								if (!isConnected()) {
									Log.d("NOT CONNECTED");
									break;
								}

								Log.d("main loop");
								int totalBytesRead = 0;

								handleInactivity(bIn);

								boolean more = true;
								int bytesRead = 0;
								while (more) { // individual message loop
									Log.d(" - read loop");
									bytesRead = bIn.read(inputBuffer, totalBytesRead, BUF_SIZE);
									if (bytesRead == -1) {
										running = false;
										break;
									}
									Log.d(" - - read loop got %s", bytesRead);
									totalBytesRead += bytesRead;
									more = bIn.available() > 0;
									// msg = StrUtl.toString(inputBuffer, 0, totalBytesRead);


									// we've exceeded the expected common-case optimized limit, lets bulk up, should be a rare case
									// if not a rare case, then you may want to increase the the inputBuffer size, however this will cost you
									// more overhead as number of concurrent users increase, so its a trade off
									// a StringBuiler for this case would have been nice, but there is no way to scale down and I don't
									// want to construct a new StringBuilder every time.
									if (more) {
										if (totalBytesRead + BUF_SIZE > inputBuffer.length) {
											Log.d("expanding from %s to %s", inputBuffer.length, inputBuffer.length * 2);
											inputBuffer = Arrays.copyOf(inputBuffer, inputBuffer.length * 2);
										} else {
											Log.d("no need to expand read %s of %s", totalBytesRead, inputBuffer.length);
										}
									}
								}

								if (totalBytesRead > 0) {
									String msg = StrUtl.toString(inputBuffer, 0, totalBytesRead);
									if (inputBuffer.length > BUF_SIZE) {
										Log.d("trim down from %s to %s", inputBuffer.length, BUF_SIZE);
										inputBuffer = Arrays.copyOf(inputBuffer, BUF_SIZE);
									}
									Log.d("running before processMessage = %s", running);
									processMessage(msg);
									Log.d("running after  processMessage = %s", running);
								}
							} finally {
								Log.d("running before finally = %s", running);
								running = running && keepAlive;
								Log.d("running after  finally = %s", running);
							}
						}
					}
				} catch (IOException e) {
					if (!running) {
						Log.e("IOException while not running likely connection timeout");
					} else {
						Log.e("IOException while running not sure why, client terminated?");
						Log.e(e);
					}
				} catch (Exception e) {
					Log.e(e);
				} finally {
					try {
						socket.close();
					} catch (IOException e) {
						Log.e("IOException while closing socket, running = " + running);
						Log.e(e);
					}
					stop();
				}
				Log.d("Thread died naturally");
			}
		}).start();
	}
	private void handleInactivity(InputStream in) throws Exception {
		long timeout = getInactivityTimeout();
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
			if (msg.startsWith("quit")) {
				doReceivedQuit();
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

}
