package com.leap12.dbexample;

import com.leap12.common.ClientConnection;
import com.leap12.common.HttpRequest;
import com.leap12.common.Log;
import com.leap12.databuddy.BaseConnectionDelegate;

public class MySpecialDelegate extends BaseConnectionDelegate {

	/**
	 * This connection only gets called if this connection was active
	 */
	@Override
	protected void onConnectionOpened() throws Exception {
		super.onConnectionOpened();
	}

	@Override
	protected void onAttached( ClientConnection connection ) throws Exception {
		connection.setInactivityTimeout( 10000 );
		connection.setKeepAlive( false ); // we don't know the client protocol
											// yet, could be HTTP or GAME
	}

	@Override
	protected void onReceivedMsg( String msg ) throws Exception {
		Log.debugNewlineChars( msg ); // log the incoming request for fun

		if ( HttpRequest.isPotentiallyHttpRequest( msg ) ) { // not guaranteed but pretty likely
			HttpRequest req = new HttpRequest( msg );
			if ( req.isValid() ) {
				getClientConnection().setKeepAlive( false );

				writeMsg( "" + "<html>" + "<body>"
						+ "<b>Boo... I'm a webserver...</b>" + "</body>"
						+ "</html>\r\n\r\n" );

				// Now die because we don't want to keep a http connection open
			}
		} else {
			writeMsg( "I am a telnet bot...beep bop boop beep." );
			MyTelnetDelegate delegate = new MyTelnetDelegate();
			getClientConnection().setDelegate( delegate ); // hand control over to the new delegate
		}
	}

}
