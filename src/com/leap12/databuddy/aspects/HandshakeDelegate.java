package com.leap12.databuddy.aspects;

import java.util.ArrayList;
import java.util.List;

import com.leap12.common.ClientConnection;
import com.leap12.common.HttpRequest;
import com.leap12.common.HttpResponse;
import com.leap12.common.HttpResponse.HttpStatusCode;
import com.leap12.common.Log;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.Commands;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.ResponseStatus;
import com.leap12.databuddy.Commands.Role;
import com.leap12.databuddy.commands.http.HttpCmd;
import com.leap12.databuddy.commands.http.HttpCmdFactory;

/**
 * The default launchpad connection. It serves as the Connection "Factory", routing a client to the appropriate connection based on how they connect.
 */
public class HandshakeDelegate extends BaseConnectionDelegate {

	@Override
	protected void onAttached( ClientConnection connection ) throws Exception {
		connection.setInactivityTimeout( 10000 );
		connection.setKeepAlive( false ); // we don't know the client protocol yet, could be HTTP or GAME
	}

	@Override
	protected void onReceivedMsg( String msg ) throws Exception {
		msg = msg.trim();
		// Log.debugNewlineChars( msg );

		// If we are a proper auth command, then deal with it
		// hand control over to the UserDelegate for the remainder of the session
		if ( 1.0f == Commands.CMD_AUTH.isCommand( msg ) ) { // 1.0f == 100% match
			getClientConnection().setKeepAlive( true );
			try {
				UserDelegate connection = routeUser( msg );
				getClientConnection().setDelegate( connection );
			} catch ( Exception e ) {
				Log.e( e );
				writeResponse( e.getMessage() );
				getClientConnection().stop();
			}
		} else if ( HttpRequest.isPotentiallyHttpRequest( msg ) ) {
			onReceivedHttpMsg( msg );
		} else {
			Log.d( "\n\n## Unrecognized Request ##\n\n" );
		}
	}

	// If this is a proper http request
	// hand control over to a Cmd handler, not a delegate.
	// Why? Delegates are for dealing with a mult-request-session, where Cmd deal with a single request
	// May be worth considering a restful-like session and building a delegate for that
	protected void onReceivedHttpMsg( String msg ) throws Exception {
		try {
			HttpRequest request = new HttpRequest( msg );
			if ( request.isValid() ) {
				getClientConnection().setKeepAlive( false );
				handleHttpRequest( request );
			}
		} catch ( Exception e ) {
			HttpResponse errResp = new HttpResponse();
			errResp.setStatusCode( HttpStatusCode.ERR_INTERNAL );
			writeMsg( errResp.toString() );
		}
	}

	protected void handleHttpRequest( HttpRequest request ) throws Exception {
		// Log.d( request.describe() );

		List<HttpCmd> commands = new ArrayList<>();
		commands.add( Commands.CMD_HTTP_SAVE );
		commands.add( Commands.CMD_HTTP_READ );
		commands.add( Commands.CMD_HTTP_ECHO );
		commands.add( Commands.CMD_HTTP_ADD );
		commands.add( Commands.CMD_HTTP_LIST );

		Iterable<HttpCmd> bestFirst = new HttpCmdFactory( commands ).bestFirst( request );
		for ( HttpCmd httpCmd : bestFirst ) {
			CmdResponse<HttpResponse> cmdResp = httpCmd.executeCommand( this, request );

			// bounce out if cmd failed
			if ( cmdResp.getError() != null ) {
				throw cmdResp.getError();
			} else if ( cmdResp.getStatus().isFailure() ) {
				throw new Exception( "Unrecognized Internal Error" );
			}

			// If unfulfilled, then it was obviously a cmd mismatch, let the next best handle it.
			if ( cmdResp.getStatus().isUnFulfilled() ) {
				continue;
			}

			// We've ruled out the bad, so the response must be usable, use it
			HttpResponse resp = cmdResp.getValue();
			writeMsg( resp.toString() );
			break;
		}
	}

	/**
	 * expects:
	 *
	 * <pre>
	 * auth request_auth=user&username=theUsername&password=thePassword
	 * or
	 * auth request_auth=sysop&username=theUsername&password=thePassword
	 * </pre>
	 *
	 * @param msg
	 * @return Appropriate connection;
	 */
	private UserDelegate routeUser( String msg ) throws Exception {
		Log.d( "handleAuthenticateUser: '%s'", msg );
		CmdResponse<Role> request = Commands.CMD_AUTH.executeCommand( this, msg );
		if ( ResponseStatus.SUCCESS == request.getStatus() ) {
			UserDelegate delegate = toConnection( request );
			if ( delegate.authenticate( request ) ) {
				return delegate;
			}
		}
		throw new Exception( request.getError() );
	}

	private UserDelegate toConnection( CmdResponse<Role> request ) {
		Role role = request.getValue();
		switch ( role ) {
		case sysop:
			return new SysOpDelegate();
		case user:
			return new UserDelegate();
		}
		throw new IllegalStateException( "Unknown Role " + role );
	}

}
