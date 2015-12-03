package com.leap12.databuddy.commands.dc;

import java.util.Map;

import com.leap12.common.StrUtl;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.CmdResponse.CmdResponseMutable;
import com.leap12.databuddy.Commands.StrCommand;
import com.leap12.databuddy.ex.DBCmdArgsException;

public class PutCmd extends StrCommand<Void> {
	private final int beginIndex;

	public PutCmd() {
		super( "put",
				"put topic=[topic]&subtopic=[subtopic]&key=[key]&data=[data]",
				Void.class );
		beginIndex = ( getName() + " " ).length();
	}

	@Override
	public CmdResponse<Void> executeCommand( BaseConnectionDelegate connection, String msg ) {
		final CmdResponseMutable<Void> response = new CmdResponseMutable<>( Void.class );
		try {
			msg = msg.substring( beginIndex, msg.length() ); // from=zero-based-inclusive, to=zero-based-inclusive
			Map<String, String> fields = StrUtl.toMap( msg, "=", "&" );
			String topic = fields.get( "topic" );
			String subtopic = fields.get( "subtopic" );
			String key = fields.get( "key" );
			String data = fields.get( "data" );
			if ( StrUtl.isEmptyAny( topic, subtopic, key, data ) ) {
				throw new DBCmdArgsException( "invalid put command" );
			}
			connection.getDb( "default" ).saveString( topic, subtopic, key, data );
			response.setStatusSuccess( null );
		} catch ( Exception e ) {
			response.setStatusFail( e );
		}
		return response;
	}
}
