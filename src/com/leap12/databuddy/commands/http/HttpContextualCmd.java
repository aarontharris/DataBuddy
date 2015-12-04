package com.leap12.databuddy.commands.http;

import com.leap12.common.HttpRequest;
import com.leap12.common.HttpResponse;
import com.leap12.common.HttpResponse.HttpStatusCode;
import com.leap12.common.Log;
import com.leap12.common.props.PropsRead;
import com.leap12.common.props.PropsRead.FieldException;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.CmdResponse.CmdResponseMutable;

/**
 * The purpose of this command is to "catch all" and act based on the given context<br>
 * If encrypted=true, then the context is expected to be associated with the encryption key and the encrypted msg contains more information about the action.<br>
 * if encrypted=false, then the context should tell you what to do with the message.<br>
 * <br>
 * /httpcmd?encrypted=true&context=hashedUsername&msg=encryptedMessage
 */
public class HttpContextualCmd extends HttpCmd {

	@Override
	public CmdResponse<HttpResponse> executeCommand( BaseConnectionDelegate connection, HttpRequest request ) {
		HttpResponse resp = new HttpResponse();
		CmdResponseMutable<HttpResponse> out = new CmdResponseMutable<>( HttpResponse.class );
		try {
			boolean encrypted = request.getQueryParams().getBooleanChecked( "encrypted", false );
			String context = request.getQueryParams().getStringRequired( "context" );
			String msg = request.getQueryParams().getStringRequired( "msg" );

			resp.getBodyBuilder().append( "Encrypted: " + encrypted ).append( "\n" );
			resp.getBodyBuilder().append( "Context: " + context ).append( "\n" );
			resp.getBodyBuilder().append( "Msg: " + msg ).append( "\n" );

			resp.setStatusCode( HttpStatusCode.OK );
			out.setStatusSuccess( resp );
		} catch ( FieldException e ) {
			Log.e( e );
			resp.setBody( e.getMessage() );
			resp.setStatusCode( HttpStatusCode.ERR_BAD_REQ );
			out.setStatusSuccess( resp );
		} catch ( Exception e ) {
			Log.e( e );
			out.setStatusFail( e );
		}
		return out;
	}

	@Override
	protected float computeRelevance( HttpRequest request ) throws Exception {
		if ( request.getPath().endsWith( "httpcmd" ) ) {
			PropsRead props = request.getQueryParams();
			if ( props.containsKeys( "context", "msg" ) ) {
				return 1f;
			}
			throw new Exception( "Improper usage of the Save Command" );
		}
		return 0f;
	}
}
