package com.leap12.databuddy.aspects;

import com.leap12.common.Log;
import com.leap12.databuddy.Commands;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.Role;
import com.leap12.databuddy.Commands.StrCommand;
import com.leap12.databuddy.DataBuddy;

/**
 * A UserConnection with advanced commands
 */
public class SysOpDelegate extends UserDelegate {

	private static final StrCommand<?>[] commands = new StrCommand[] {
			Commands.CMD_TEST,
	};

	@Override
	public Role getRole() {
		return Role.sysop;
	}

	@Override
	protected void onReceivedMsg( String msg ) throws Exception {
		boolean handled = false;

		if ( "get connection count".equals( msg ) ) {
			int count = DataBuddy.get().getConnectionCount();
			writeLnMsgSafe( String.valueOf( count ) );
			handled = true;
		} else if ( "gc".equals( msg ) ) {
			System.gc();
			handled = true;
		}

		for ( StrCommand<?> cmd : commands ) {
			if ( 1.0f == cmd.isCommand( msg ) ) {
				CmdResponse<?> response = cmd.executeCommand( this, msg );
				writeResponse( response );
				if ( response.getError() != null ) {
					Log.e( response.getError() );
				}
				handled = true;
			}
		}

		if ( !handled ) {
			super.onReceivedMsg( msg );
		}
	}

}
