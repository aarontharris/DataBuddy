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

import com.leap12.databuddy.DataBuddy;

public class ClientConnection {
	private static ConnectionDelegate DEFAULT_DELEGATE = new ConnectionDelegate();

	public static long count = 0;
	public static long millis = 0;
	public static long longest = 0;
	public static long shortest = -1;
	public static long dMillis = 0;
	public static long dLongest = 0;
	public static long wMillis = 0;
	public static long wLongest = 0;

	private final Socket socket;
	private ConnectionDelegate delegate;
	private CopyOnWriteArrayList<WeakReference<ConnectionEventListener>> listeners;
	private boolean running = false;
	private String mLineSeparator;
	private boolean keepAlive = false;
	private int mInactivityTimeoutMillis = 0;
	private int mInactivityPollIntervalMillis = 250;
	private BufferedInputStream bIn;
	private long startTime = 0L;

	public ClientConnection( Socket socket ) {
		this.socket = socket;
		setDelegate( DEFAULT_DELEGATE );
		setLineSeparator( System.getProperty( "line.separator" ) );
	}

	@Override
	public void finalize() {
		Log.e( "ClientConnection.finalize()" );
		try {
			super.finalize();
		} catch ( Throwable e ) {
			Log.e( e );
		}
	}

	public Socket getSocket() {
		return socket;
	}

	public void setKeepAlive( boolean keepAlive ) {
		this.keepAlive = keepAlive;
	}

	public boolean getKeepAlive() {
		return keepAlive;
	}

	public int getInactivityTimeout() {
		return mInactivityTimeoutMillis;
	}

	public void setInactivityTimeout( int millis ) {
		this.mInactivityTimeoutMillis = millis;
	}

	public int getInactivityPollInterval() {
		return mInactivityPollIntervalMillis;
	}

	/**
	 * When a client sitting idle during the handshake phase, we will loop and check for activity.<br>
	 * 0 millis would eat up the CPU until the inactivityTimeout occurred.<BR>
	 * 1000 millis would seem sluggishly responsive to the user during the handshake<br>
	 * Ideal is something imperceivably fast but slow enough to not eat up CPU.<br>
	 * Again, this is ONLY during handshake, after that whether a browser or game client, its immediate after that as it instead blocks on I/O.<br>
	 * 500ms is default<br>
	 * 
	 * @param millis
	 */
	public void setInactivityPollInterval( int millis ) {
		if ( millis < 1 ) {
			throw new IllegalArgumentException( "Can't travel back in time" );
		}
		this.mInactivityPollIntervalMillis = millis;
	}

	public void setDelegate( ConnectionDelegate delegate ) {
		try {
			if ( this.delegate != null ) {
				this.delegate.doDetatched();
			}
		} catch ( Exception e ) {
			Log.e( e );
		}
		this.delegate = delegate == null ? DEFAULT_DELEGATE : delegate; // singular transaction, no period of nullness (thread safety)
		try {
			if ( this.delegate != null ) {
				this.delegate.doAttached( this );
			}
		} catch ( Exception e ) {
			Log.e( e );
		}
	}

	public synchronized void registerListener( ConnectionEventListener listener ) {
		if ( listener != null ) {
			if ( listeners == null ) {
				listeners = new CopyOnWriteArrayList<>();
			}
			listeners.add( new WeakReference<>( listener ) );
		}
	}

	public synchronized void unregisterListener( ConnectionEventListener listener ) {
		if ( listener != null ) {
			if ( listeners != null ) {
				for ( WeakReference<ConnectionEventListener> weakListener : listeners ) {
					try {
						ConnectionEventListener aListener = weakListener.get();
						if ( listener == aListener || aListener == null ) {
							listeners.remove( weakListener );
						}
					} catch ( Exception e ) {
						Log.e( e );
					}
				}
			}
		}
	}

	/**
	 * Auto-Flushes every line see {@link #writeLnMsgSafe(String)} if you want to batch never throws - convenience for when you don't care
	 * about errors
	 */
	public final void writeLnMsgSafe( String msg ) {
		try {
			writeLnMsg( msg );
		} catch ( Exception e ) {
			Log.e( e );
		}
	}

	public final void writeLnMsg( String msg ) throws Exception {
		writeMsg( msg, 1 );
	}

	public final void writeMsg( String msg ) throws Exception {
		writeMsg( msg, 0 );
	}

	/**
	 * Write a message to the client.
	 * 
	 * @param msg - the message
	 * @param appendNewLines - number of newline chars to be appended to the message
	 * @throws Exception - if something went wrong during the write
	 */
	public final void writeMsg( String msg, int appendNewLines ) throws Exception {
		if ( isSocketOpen() ) {
			OutputStream out = socket.getOutputStream();
			BufferedOutputStream bOut = new BufferedOutputStream( out );
			bOut.write( StrUtl.toBytes( msg ) );
			if ( appendNewLines == 1 ) {
				bOut.write( '\n' );
			} else if ( appendNewLines > 1 ) {
				for ( int i = 0; i < appendNewLines; i++ ) {
					bOut.write( '\n' );
				}
			}
			bOut.flush();
		}
	}

	public void setLineSeparator( String separator ) {
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
		} catch ( Exception e ) {
			Log.e( e );
		}
	}

	// FIXME: Test when the client application dies unexpectedly, do we detect and close? do we need an inactivity failsafe for the authorized state?
	private static final int BUF_SIZE = 1024;

	public final void run() {
		startTime = System.currentTimeMillis();
		new Thread( new Runnable() {
			@Override
			public void run() {
				Log.d( "New Connection on Port %s", socket.getPort() );
				DataBuddy.get().associateThisThreadToPort( socket.getPort() );
				long waitedForInput = 0;
				long processMessageTime = 0;

				try {
					if ( isConnected() ) {
						doConnectionOpened();

						running = true;
						socket.setKeepAlive( false );

						InputStream in = socket.getInputStream();
						bIn = new BufferedInputStream( in );

						byte[] inputBuffer = new byte[BUF_SIZE];
						// boolean running = true;

						// keepAlive causes the messaging loop to only run once when false
						// otherwise, it loops to stay alive
						// browsers or general HTTP GET or PUT should keepAlive=false
						// gameClient should keepAlive=true
						while ( isSocketOpen() ) { // keepAlive messaging loop
							try {
								int totalBytesRead = 0;

								long startHandleInactivity = System.currentTimeMillis();
								handleInactivity( bIn );
								waitedForInput = System.currentTimeMillis() - startHandleInactivity;

								boolean more = true;
								int bytesRead = 0;
								while ( more ) { // individual message loop
									bytesRead = bIn.read( inputBuffer, totalBytesRead, BUF_SIZE );
									if ( bytesRead == -1 ) {
										running = false;
										break;
									}

									// Thread.sleep(1000);

									totalBytesRead += bytesRead;
									more = bIn.available() > 0;
									// msg = StrUtl.toString(inputBuffer, 0, totalBytesRead);


									// Bulk Up ?
									// we've exceeded the expected common-case optimized limit, lets bulk up, should be a rare case
									// if not a rare case, then you may want to increase BUF_SIZE, however this will cost you
									// more overhead as the number of concurrent users increase, so its a trade off
									if ( more ) {
										if ( totalBytesRead + BUF_SIZE > inputBuffer.length ) {
											inputBuffer = Arrays.copyOf( inputBuffer, inputBuffer.length * 2 );
										}
									}
								}

								if ( totalBytesRead > 0 ) {
									String msg = StrUtl.toString( inputBuffer, 0, totalBytesRead );

									long startProcessMessage = System.currentTimeMillis();
									processMessage( msg, inputBuffer, totalBytesRead );
									processMessageTime = System.currentTimeMillis() - startProcessMessage;

									// Trim Down ?
									// We assume the previous bulk-up was an outlier case so we'll want to trim down to save memory.
									// However if we're bulking up and triming down alot, that costs performance and you might consider upping the BUF_SIZE.
									if ( inputBuffer.length > BUF_SIZE ) {
										Log.d( "trim down from %s to %s", inputBuffer.length, BUF_SIZE );
										inputBuffer = Arrays.copyOf( inputBuffer, BUF_SIZE );
									}
								}
							} finally {
								running = running && keepAlive;
							}
						}
					}
				} catch ( IOException e ) {
					if ( !running ) {
						Log.e( "IOException while not running likely connection timeout" );
					} else {
						Log.e( "IOException while running not sure why, client terminated?" );
						Log.e( e );
					}
				} catch ( Exception e ) {
					Log.e( e );
				} finally {
					try {
						socket.close(); // closes input stream
						bIn.close();
					} catch ( IOException e ) {
						Log.e( "IOException while closing socket, running = " + running );
						Log.e( e );
					}
					stop();
				}
				// Log.d( "Thread died naturally" );

				long stop = System.currentTimeMillis();
				long delta = stop - startTime;

				count += 1;
				millis += delta;
				dMillis += processMessageTime;
				wMillis += waitedForInput;

				if ( delta < shortest || shortest == -1 ) {
					shortest = delta;
				}
				if ( delta > longest ) {
					longest = delta;
				}
				if ( processMessageTime > dLongest ) {
					dLongest = processMessageTime;
				}
				if ( waitedForInput > wLongest ) {
					wLongest = waitedForInput;
				}

				float avg = (float) millis / (float) count;
				float davg = (float) dMillis / (float) count;
				float wavg = (float) wMillis / (float) count;

				Log.d( "This: %s (w=%s|%s|%s,p=%s|%s|%s), Avg: %s, Short: %s, Long: %s", delta,
				        waitedForInput, wavg, wLongest,
				        processMessageTime, davg, dLongest,
				        avg, shortest, longest );
			}
		} ).start();
	}

	private void handleInactivity( InputStream in ) throws Exception {
		long sleepTime = 1;
		long timeout = getInactivityTimeout();
		if ( timeout > 0 ) {
			long start = System.currentTimeMillis();
			while ( in.available() == 0 ) {
				long now = System.currentTimeMillis();
				long delta = now - start;
				if ( delta > timeout ) {
					ClientConnection.this.stop();
				}

				Thread.sleep( sleepTime );

				sleepTime *= 2;
				if ( sleepTime > getInactivityPollInterval() ) {
					sleepTime = getInactivityPollInterval();
				}
			}
		}
	}

	private void processMessage( String msgString, byte[] msgBytes, int length ) {
		try {
			if ( msgString.startsWith( "quit" ) ) {
				doReceivedQuit();
			} else {
				doReceivedMsg( msgString, msgBytes, length );
			}
		} catch ( Exception e ) {
			Log.e( e );
		}
	}

	private void doConnectionOpened() throws Exception {
		delegate.doConnectionOpened(); // fail here bounce out to skip listeners
		if ( listeners != null ) {
			for ( WeakReference<ConnectionEventListener> weakListener : listeners ) {
				try {
					ConnectionEventListener listener = weakListener.get();
					if ( listener != null ) {
						listener.onConnectionOpened();
					}
				} catch ( Exception e ) {
					Log.e( e );
				}
			}
		}
	}

	private void doReceivedMsg( final String msg, final byte[] msgBytes, final int length ) throws Exception {
		delegate.doReceivedMsg( msg ); // fails here should bounce out to skip listeners
		if ( listeners != null ) {
			for ( WeakReference<ConnectionEventListener> weakListener : listeners ) {
				try {
					ConnectionEventListener listener = weakListener.get();
					if ( listener != null ) {
						listener.onReceivedMsg( msg );
					}
				} catch ( Exception e ) {
					Log.e( e );
				}
			}
		}
	}

	private void doReceivedQuit() throws Exception {
		try {
			if ( listeners != null ) {
				for ( WeakReference<ConnectionEventListener> weakListener : listeners ) {
					try {
						ConnectionEventListener listener = weakListener.get();
						if ( listener != null ) {
							listener.onReceivedQuit();
						}
					} catch ( Exception e ) {
						Log.e( e );
					}
				}
			}
		} catch ( Exception e ) { // a little overcautious but we can't have any listeners mucking up our delegate
			Log.e( e );
		}
		try {
			delegate.doReceivedQuit();
		} catch ( Exception e ) { // also overcautious but we want to ensure that we set the delegate to null for proper cycle
			Log.e( e );
		}
		try {
			setDelegate( null );
		} catch ( Exception e ) { // cant not close the socket and a finally doesnt make sense because this is all progressive
			Log.e( e );
		}
		running = false;
		socket.close();
		bIn.close();
	}

	private boolean isConnected() {
		if ( socket != null && socket.isBound() && socket.isConnected() && !socket.isClosed() && !socket.isOutputShutdown()
		        && !socket.isInputShutdown() ) {
			return true;
		}
		return false;
	}

}
