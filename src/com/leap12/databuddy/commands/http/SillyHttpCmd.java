package com.leap12.databuddy.commands.http;

import com.leap12.common.HttpRequest;
import com.leap12.common.HttpResponse;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.Commands.CmdResponse.CmdResponseMutable;

public class SillyHttpCmd extends HttpCmd {

	@Override
	public CmdResponse<HttpResponse> executeCommand( BaseConnectionDelegate connection, HttpRequest input ) {
		try {
			HttpResponse response = new HttpResponse();
			response.getBodyBuilder().append( "<html>" );
			response.getBodyBuilder().append( "<body>" );
			response.getBodyBuilder().append( "<b>Yaay! I'm a webserver!</b>" );
			response.getBodyBuilder().append( "</br>" );
			response.getBodyBuilder().append( "</br>" );
			response.getBodyBuilder().append( input.describe( "<br/>\n" ) );
			response.getBodyBuilder().append( "</body>" );
			response.getBodyBuilder().append( "</html>" );
			connection.writeMsg( response.toString() );
			return new CmdResponseMutable<HttpResponse>( HttpResponse.class, response );
		} catch ( Exception e ) {
			return new CmdResponseMutable<HttpResponse>( HttpResponse.class, e );
		}
	}

	@Override
	protected float computeRelevance( HttpRequest request ) {
		return 0.1f;
	}

}
