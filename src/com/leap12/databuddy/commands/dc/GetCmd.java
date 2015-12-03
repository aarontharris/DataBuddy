package com.leap12.databuddy.commands.dc;

import java.util.Map;

import com.leap12.common.StrUtl;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.CmdResponse.CmdResponseMutable;
import com.leap12.databuddy.Commands.StrCommand;
import com.leap12.databuddy.ex.DBCmdArgsException;

public class GetCmd extends StrCommand<String> {
	private final int beginIndex;

	public GetCmd() {
		super( "get",
				"get topic=[topic]&subtopic=[subtopic]&key=[key]",
				String.class );
		beginIndex = ( getName() + " " ).length();
	}

	@Override
	public CmdResponse<String> executeCommand( BaseConnectionDelegate connection, String msg ) {
		final CmdResponseMutable<String> response = new CmdResponseMutable<>( String.class );
		try {
			msg = msg.substring( beginIndex, msg.length() ); // from=zero-based-inclusive, to=zero-based-inclusive
			Map<String, String> fields = StrUtl.toMap( msg, "=", "&" );
			String topic = fields.get( "topic" );
			String subtopic = fields.get( "subtopic" );
			String key = fields.get( "key" );
			if ( StrUtl.isEmptyAny( topic, subtopic, key ) ) {
				throw new DBCmdArgsException( "invalid get command" );
			}
			String data = connection.getDb( "default" ).loadString( topic, subtopic, key );
			response.setStatusSuccess( data );
		} catch ( Exception e ) {
			response.setStatusFail( e );
		}
		return response;
	}

}
