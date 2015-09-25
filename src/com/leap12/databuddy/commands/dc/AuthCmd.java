package com.leap12.databuddy.commands.dc;

import java.util.Map;

import com.leap12.common.Log;
import com.leap12.common.Preconditions;
import com.leap12.common.StrUtl;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.CmdResponse.CmdResponseMutable;
import com.leap12.databuddy.Commands.Role;
import com.leap12.databuddy.Commands.StrCommand;
import com.leap12.databuddy.ex.DBCmdArgsException;
import com.leap12.databuddy.ex.DBCmdException;

public class AuthCmd extends StrCommand<Role> {
	private final int beginIndex;

	public AuthCmd() {
		super( "auth",
				"auth request_auth=[ROLE]&username=[USERNAME]&password=[PASSWORD]",
				Role.class );
		beginIndex = ( getName() + " " ).length();
	}

	@Override
	public CmdResponse<Role> executeCommand( BaseConnectionDelegate connection, String msg ) {
		final CmdResponseMutable<Role> response = new CmdResponseMutable<>( Role.class );
		try {
			msg = msg.substring( beginIndex, msg.length() ); // from=zero-based-inclusive, to=zero-based-inclusive
			Map<String, String> fields = StrUtl.toMap( msg, "=", "&" );
			String roleStr = fields.get( "request_auth" );

			Role role = Role.fromValue( roleStr );
			String userStr = fields.get( "username" );
			String passStr = fields.get( "password" );
			Log.d( "Received Request of Role %s for '%s' '%s'", roleStr, userStr, passStr );

			Preconditions.isValidArg( StrUtl.isNotEmptyAll( roleStr, userStr, passStr ) ); // throws if empty

			response.setArgs().putString( "username", userStr );
			response.setArgs().putString( "password", passStr );
			response.setStatusSuccess( role );
		} catch ( NullPointerException | IllegalArgumentException e ) {
			response.setStatusFail( new DBCmdArgsException( "Invalid Role" ) );
		} catch ( Exception e ) {
			response.setStatusFail( new DBCmdException( e.getMessage(), e ) );
		}
		return response;
	}

	@Override
	public Class<?> getDisplayType() {
		return Void.class;
	}
}
