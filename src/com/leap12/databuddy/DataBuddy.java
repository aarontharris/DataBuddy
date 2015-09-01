package com.leap12.databuddy;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.leap12.common.ClientConnection;
import com.leap12.common.Log;
import com.leap12.databuddy.connections.HandshakeConnection;

public final class DataBuddy {
	private static final DataBuddy self = new DataBuddy();

	public static DataBuddy get() {
		return self;
	}

	private boolean running = false;
	private WeakHashMap<ClientConnection, Long> connections = null;
	private CountDownLatch latch = null;

	private DataBuddy() {
		Config.get().initialize(System.getProperties());
	}

	public synchronized void startup() throws Exception {
		if (!running) {
			running = true;
			ServerSocket serverSocket = null;
			int port = 0;

			try {
				connections = new WeakHashMap<>();
				port = Config.get().getPort();
				serverSocket = new ServerSocket(port);
				latch = new CountDownLatch(1);

				try {
					Log.d("Listening on Port %s", port);
					Socket socket = null;
					while (running && (socket = serverSocket.accept()) != null) {
						ClientConnection connection = new ClientConnection(socket);
						connection.setDelegate(new HandshakeConnection());
						connection.run();
						connections.put(connection, System.currentTimeMillis());
					}

					Log.d("Waiting on latch");
					if (latch.await(1000, TimeUnit.MILLISECONDS)) {
						Log.d("done with latch");
					} else {
						Log.d("cant wait anymore for latch");
					}
				} catch (Exception e) {
					Log.e(e, "Trouble while listening on port %s", port);
				}
			} catch (Exception e) {
				Log.e(e, "Trouble opening port %s", port);
			} finally {
				if (serverSocket != null) {
					Log.d("Disconnecting Port %s", port);
					serverSocket.close();
				}
			}
		}
	}

	public void shutdown() {
		Log.d("Shutting Down");
		try {
			if (running) {
				running = false;
				WeakHashMap<ClientConnection, Long> connections = this.connections;
				this.connections = null;
				for (ClientConnection connection : connections.keySet()) {
					try {
						Log.d("Shutting Down Connection");
						if (connection != null) {
							connection.stop();
						}
					} catch (Exception e) {
						Log.e(e);
					}
				}
			}
		} finally {
			if (latch != null) {
				latch.countDown();
			}
		}
	}

	public int getConnectionCount() {
		int count = 0;
		for (ClientConnection connection : connections.keySet()) {
			if (connection != null) {
				count++;
			}
		}
		return count;
	}

}
