package com.leap12.databuddy.commands.http;

import com.leap12.common.HttpRequest;
import com.leap12.common.HttpResponse;
import com.leap12.common.HttpResponse.HttpStatusCode;
import com.leap12.common.Log;
import com.leap12.common.props.PropsRead.FieldException;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.CmdResponse.CmdResponseMutable;
import com.leap12.databuddy.data.ContextShardKey;
import com.leap12.databuddy.data.DataStore;
import com.leap12.databuddy.data.ShardKey;

public abstract class HttpContextualCmd extends HttpCmd {

	protected abstract void exec( BaseConnectionDelegate connection, HttpRequest req, HttpResponse resp, DataStore db ) throws Exception;

	@Override
	public final CmdResponse<HttpResponse> executeCommand( BaseConnectionDelegate connection, HttpRequest input ) {
		HttpResponse resp = new HttpResponse();
		CmdResponseMutable<HttpResponse> out = new CmdResponseMutable<>( HttpResponse.class );
		try {
			String username = input.getQueryParam( "context" );
			ShardKey shardKey = new ContextShardKey( username );
			DataStore db = connection.getDb( shardKey );

			exec( connection, input, resp, db );

			resp.setStatusCode( HttpStatusCode.OK, null );
			out.setStatusSuccess( resp );
		} catch ( FieldException e ) {
			Log.e( e );
			resp.setBody( e.getMessage() );
			resp.setStatusCode( HttpStatusCode.ERR_BAD_REQ, e );
			out.setStatusSuccess( resp );
		} catch ( Exception e ) {
			Log.e( e );
			out.setStatusFail( e );
		}
		return out;
	}

	@Override
	protected float computeRelevance( HttpRequest request ) throws Exception {
		if ( request.containsQueryParam( "context" ) ) {
			return 1f;
		}
		return 0f;
	}

}
