package com.leap12.databuddy.commands.http;

import com.leap12.common.Log;
import com.leap12.common.http.HttpRequest;
import com.leap12.common.http.HttpResponse;
import com.leap12.databuddy.Commands.Command;

public abstract class HttpCmd extends Command<HttpRequest, HttpResponse> {

	public HttpCmd() {
		super( HttpResponse.class );
	}

	@Override
	public final float isCommand( HttpRequest in ) {
		try {
			return computeRelevance( in );
		} catch ( Exception e ) {
			Log.e( e );
		}
		return 0f;
	}

	/**
	 * What is this command's relevance to the given request?<br>
	 * If an error is thrown, 0.0f is assumed.<br>
	 * 
	 * @return 1.0f for 100% match or 0.0f for 0% match
	 */
	protected abstract float computeRelevance( HttpRequest request ) throws Exception;

}
