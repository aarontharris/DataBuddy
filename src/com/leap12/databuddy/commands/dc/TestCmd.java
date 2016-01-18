package com.leap12.databuddy.commands.dc;

import com.leap12.common.Log;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.CmdResponse.CmdResponseMutable;
import com.leap12.databuddy.Commands.StrCommand;

public class TestCmd extends StrCommand<Void> {

	public TestCmd() {
		super( "test",
				"",
				Void.class );
	}

	@Override
	public CmdResponse<Void> executeCommand( BaseConnectionDelegate connection, String msg ) {
		Log.d( "saving" );
		final CmdResponseMutable<Void> response = new CmdResponseMutable<>( Void.class );
		try {
			// String topic = "user";
			// String key = "coins";
			// int count = 1000000000;
			// for ( int i = 0; i < 3; i++ ) {
			// String subtopic = Integer.toString( i );
			// String data = Integer.toString( i );
			// Log.d( "save " + i );
			// connection.getDb().saveString( topic, subtopic, key, data );
			// }
			response.setStatusSuccess( null );
		} catch ( Exception e ) {
			Log.e( e );
			response.setStatusFail( e );
		}
		return response;
	}
}
