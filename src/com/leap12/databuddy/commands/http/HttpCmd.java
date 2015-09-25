package com.leap12.databuddy.commands.http;

import com.leap12.common.HttpRequest;
import com.leap12.common.HttpResponse;
import com.leap12.databuddy.Commands.Command;

public abstract class HttpCmd extends Command<HttpRequest, HttpResponse> {

	public HttpCmd() {
		super( HttpResponse.class );
	}

}
