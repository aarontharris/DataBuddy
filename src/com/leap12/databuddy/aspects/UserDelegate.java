package com.leap12.databuddy.aspects;

import java.util.UUID;

import javax.crypto.BadPaddingException;

import com.leap12.common.ClientConnection;
import com.leap12.common.Crypto;
import com.leap12.common.Log;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.Commands;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.Role;
import com.leap12.databuddy.Commands.StrCommand;

/**
 * This is a connection tailored for a user in need of a persistent connection. Ideal for pushing an pulling data without having to reconnect each
 * time as the connection remains open until explicitly closed by client or server.
 */
public class UserDelegate extends BaseConnectionDelegate {

	private static final StrCommand<?>[] commands = new StrCommand[] {
			Commands.CMD_HELP,
			Commands.CMD_PUT,
			Commands.CMD_GET,
			Commands.CMD_RELAY,
	};

	public Role getRole() {
		return Role.user;
	}

	protected boolean authenticate( CmdResponse<Role> request ) throws Exception {
		String username = request.getArgs().getString( "username" );
		String password = request.getArgs().getString( "password" );
		String newUser = request.getArgs().getString( "newuser" );
		Log.d( "%s : %s : %s", username, password, newUser );

		if ( "1".equals( newUser ) ) {
			long now = System.currentTimeMillis();
			UUID uuid = UUID.randomUUID();
			String id = uuid.toString() + "-" + now;
			Log.d( "ID: '%s'", id );
			// getDbDefault().loadString( "auth", "user", username );

			// User user = new User( id, username, password );
			// String strUser = Dao.gson.toJson( user );
			// Log.d( "User: '%s'", strUser );

			Crypto crypt = new Crypto();

			try {
				String SALT = "shouldBeSomethingRandom";
				String user, pass, inMsg, keyPhrase;
				// byte[] encMsg;

				inMsg = "Hello World";
				keyPhrase = username + password + SALT;

				String encMsg = crypt.encrypt( inMsg, keyPhrase );
				String outMsg = crypt.decrypt( encMsg, keyPhrase );

				Log.d( "Enc: '%s'", crypt.toStringLossy( encMsg.getBytes() ) );

				Log.d( "'%s', '%s', '%s', '%s', matched=%s", username, password, inMsg, outMsg, inMsg.equals( outMsg ) );
			} catch ( BadPaddingException e ) {
				Log.e( "Invalid Username or Password" );
			}

			// getDbDefault().saveString( "auth", "user", "id", strUser );
		} else {
			// getDbDefault().loadString( "auth", "user", username );
		}
		return false;
	}

	@Override
	protected void onAttached( ClientConnection connection ) throws Exception {
		connection.setInactivityTimeout( 120000 ); // 2 minute -- TODO: client will need to detect disconnect and reconnect.
		connection.setKeepAlive( true );
	}

	@Override
	protected void onReceivedMsg( String msg ) throws Exception {
		msg = msg.trim();
		Log.debugNewlineChars( msg );

		for ( StrCommand<?> cmd : commands ) {
			if ( 1.0f == cmd.isCommand( msg ) ) {
				CmdResponse<?> response = cmd.executeCommand( this, msg );
				writeResponse( response );
				if ( response.getError() != null ) {
					Log.e( response.getError() );
				}
			}
		}
	}

	@Override
	protected void onReceivedQuit() throws Exception {
		writeLnMsgSafe( "quitting" );
	}

}
