package com.leap12.databuddy.aspects;

import com.leap12.databuddy.Commands.Role;
import com.leap12.databuddy.DataBuddy;

/**
 * A UserConnection with advanced commands
 */
public class SysOpDelegate extends UserDelegate {

	@Override
	public Role getRole() {
		return Role.sysop;
	}

	@Override
	protected void onReceivedMsg( String msg ) throws Exception {
		if ( "get connection count".equals( msg ) ) {
			int count = DataBuddy.get().getConnectionCount();
			writeLnMsgSafe( String.valueOf( count ) );
		} else if ( "gc".equals( msg ) ) {
			System.gc();
		} else {
			super.onReceivedMsg( msg );
		}
	}

}
