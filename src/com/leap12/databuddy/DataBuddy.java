package com.leap12.databuddy;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.leap12.common.ClientConnection;
import com.leap12.common.Crypto;
import com.leap12.common.Log;
import com.leap12.databuddy.aspects.DefaultHandshakeDelegate;

public final class DataBuddy {
	private static final DataBuddy self = new DataBuddy();

	public static DataBuddy get() {
		return self;
	}

	private boolean running = false;
	private WeakHashMap<ClientConnection, Long> connections = null;
	private CountDownLatch latch = null;
	private final Map<Long, Integer> threadPort = new HashMap<>();
	private final Map<Integer, Long> portThread = new HashMap<>();

	// private final Map<Long, Integer> threadPort = new HashMap<>();

	private DataBuddy() {
		try {
			Config.get().initialize( System.getProperties() );
			Crypto.season( Config.get().getCharPalette( null ), Config.get().getProps().getString( "crypt.salt" ) );
		} catch ( Exception e ) {
			throw new IllegalStateException( e );
		}
	}

	public synchronized void startup() throws Exception {
		startup( DefaultHandshakeDelegate.class );
	}

	/**
	 * @param connectionDelegate Who will be handling the connection messages. The class must have an empty constructor.
	 */
	public synchronized void startup( Class<? extends BaseConnectionDelegate> connectionDelegateClass ) throws Exception {
		if ( !running ) {
			running = true;
			ServerSocket serverSocket = null;
			int port = 0;

			try {
				connections = new WeakHashMap<>();
				port = Config.get().getPort();
				serverSocket = new ServerSocket( port );
				latch = new CountDownLatch( 1 );

				try {
					Log.d( "Listening on Port %s", port );
					Socket socket = null;
					while ( running && ( socket = serverSocket.accept() ) != null ) { // FIXME: block failed connections for 10 seconds
						ClientConnection connection = new ClientConnection( socket );
						BaseConnectionDelegate delegate = connectionDelegateClass.newInstance();
						connection.setDelegate( delegate );
						connection.run();
						connections.put( connection, System.currentTimeMillis() );
					}

					Log.d( "Waiting on latch" );
					if ( latch.await( 1000, TimeUnit.MILLISECONDS ) ) {
						Log.d( "done with latch" );
					} else {
						Log.d( "cant wait anymore for latch" );
					}
				} catch ( Exception e ) {
					Log.e( e, "Trouble while listening on port %s", port );
				}
			} catch ( Exception e ) {
				Log.e( e, "Trouble opening port %s", port );
			} finally {
				if ( serverSocket != null ) {
					Log.d( "Disconnecting Port %s", port );
					serverSocket.close();
				}
			}
		}
	}

	public void shutdown() {
		Log.d( "Shutting Down" );
		try {
			if ( running ) {
				running = false;
				WeakHashMap<ClientConnection, Long> connections = this.connections;
				this.connections = null;
				for ( ClientConnection connection : connections.keySet() ) {
					try {
						Log.d( "Shutting Down Connection" );
						if ( connection != null ) {
							connection.stop();
						}
					} catch ( Exception e ) {
						Log.e( e );
					}
				}
			}
		} finally {
			if ( latch != null ) {
				latch.countDown();
			}
		}
	}

	public int getConnectionCount() {
		int count = 0;
		for ( ClientConnection connection : connections.keySet() ) {
			if ( connection != null ) {
				count++ ;
			}
		}
		return count;
	}

	public void relayMessage( String message, ClientConnection src ) {
		Set<ClientConnection> connections = this.connections.keySet();
		for ( ClientConnection conn : connections ) {
			if ( !src.equals( conn ) ) {
				if ( conn.isSocketOpen() ) {
					conn.writeLnMsgSafe( message );
				}
			}
		}
	}

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

	public void associateThisThreadToPort( int port ) {
		lock.writeLock().lock();
		try {
			threadPort.put( Thread.currentThread().getId(), port );
			portThread.put( port, Thread.currentThread().getId() );
		} finally {
			lock.writeLock().unlock();
		}
	}

	public Long getThreadId( int port ) {
		lock.readLock().lock();
		try {
			return portThread.get( port );
		} finally {
			lock.readLock().unlock();
		}
	}

	public Integer getPort() {
		lock.readLock().lock();
		try {
			return threadPort.get( Thread.currentThread().getId() );
		} finally {
			lock.readLock().unlock();
		}
	}
}
