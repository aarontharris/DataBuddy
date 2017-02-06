package com.leap12.databuddy.commands.http;

import com.leap12.common.StrUtl;
import com.leap12.common.http.HttpRequest;
import com.leap12.common.http.HttpResponse;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.data.DataStore;

/**
 * Key/Value pairs are grouped by topic and subtopic.<br>
 * Example topic might be the word "user"<br>
 * Example subtopic might be the user's username.<br>
 * Now the key/value pairs in this category are specific to this user.<br>
 * 
 * <pre>
 * 
 * Expects Headers:
 * content-type=application/x-www-form-urlencoded
 * 
 * Expects Query Param:
 * context=[topic context]
 * 
 * Expects Post Param:
 * action=save
 * topic=[the primary focus of the data]
 * subtopic=[the secondary focus of the data]
 * key=[key to access value]
 * value=[the actual value]
 * 
 * </pre>
 */
public class HttpSaveCmd extends HttpPostCmd {
	public static long count = 0;
	public static long time = 0;

	@Override
	protected float computeRelevance( HttpRequest request ) throws Exception {
		if ( "save".equalsIgnoreCase( request.getBodyParam( "action", StrUtl.EMPTY ) ) ) {
			if ( request.isContentTypeXwwwFormUrlEncoded() && request.containsBodyParams( "topic", "subtopic", "key", "value" ) ) {
				return 1f * super.computeRelevance( request );
			}
		}
		return 0f;
	}

	@Override
	protected void exec( BaseConnectionDelegate connection, HttpRequest req, HttpResponse resp, DataStore db ) throws Exception {
		String topic = req.getBodyParams().getStringRequired( "topic" );
		String subtopic = req.getBodyParams().getStringRequired( "subtopic" );
		String key = req.getBodyParams().getStringRequired( "key" );
		String value = req.getBodyParams().getStringRequired( "value" );

		try {
			db.begin();
			db.saveString( topic, subtopic, key, value );
		} finally {
			db.end();
		}
	}

}
