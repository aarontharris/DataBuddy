package com.leap12.databuddy.commands.http;

import java.sql.SQLException;

import com.leap12.common.HttpRequest;
import com.leap12.common.HttpResponse;
import com.leap12.common.Log;
import com.leap12.common.StrUtl;
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
 * action=read
 * topic=[the primary focus of the data]
 * subtopic=[the secondary focus of the data]
 * key=[key to access value]
 * 
 * Expected Output:
 * value
 * 
 * </pre>
 */
public class HttpReadCmd extends HttpPostCmd {

	@Override
	protected float computeRelevance( HttpRequest request ) throws Exception {
		if ( "read".equalsIgnoreCase( request.getBodyParam( "action", StrUtl.EMPTY ) ) ) {
			if ( request.isContentTypeXwwwFormUrlEncoded() && request.containsBodyParams( "topic", "subtopic", "key" ) ) {
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
		String value = null;
		try {
			db.begin();
			value = db.loadString( topic, subtopic, key );
		} catch ( SQLException e ) {
			// mute - missing table exception, just return null
			Log.e( e );
		} finally {
			db.end();
		}
		resp.setBody( value );
	}

}
