package com.leap12.databuddy.commands.http;

import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.leap12.common.Log;
import com.leap12.common.StrUtl;
import com.leap12.common.http.HttpRequest;
import com.leap12.common.http.HttpResponse;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.data.DataStore;

/**
 * Retrieve a list of items based on topic and subtopic<br>
 * without the need for a key.<br>
 * <br>
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
 * action=list
 * topic=[the primary focus of the data]
 * subtopic=[the secondary focus of the data]
 * 
 * Expected Output:
 * [ value, ... ]
 * 
 * </pre>
 */
public class HttpListCmd extends HttpPostCmd {

	@Override
	protected float computeRelevance( HttpRequest request ) throws Exception {
		if ( "list".equalsIgnoreCase( request.getBodyParam( "action", StrUtl.EMPTY ) ) ) {
			if ( request.isContentTypeXwwwFormUrlEncoded() && request.containsBodyParams( "topic", "subtopic" ) ) {
				return 1f * super.computeRelevance( request );
			}
		}
		return 0f;
	}

	@Override
	protected void exec( BaseConnectionDelegate connection, HttpRequest req, HttpResponse resp, DataStore db ) throws Exception {
		String topic = req.getBodyParams().getStringRequired( "topic" );
		String subtopic = req.getBodyParams().getStringRequired( "subtopic" );
		Integer offset = req.getBodyParams().getInteger( "offset", null );
		Integer limit = req.getBodyParams().getInteger( "limit", null );
		JSONObject value = new JSONObject();
		try {
			db.begin();
			JSONArray values = db.loadArrayOfKeyVals( topic, subtopic, offset, limit );
			value.put( "topic", topic );
			value.put( "subtopic", subtopic );
			value.put( "values", values );
		} catch ( SQLException e ) {
			// mute - missing table exception, just return null // FIXME: not always true
			Log.e( e );
		} finally {
			db.end();
		}
		resp.setBody( value.toString() );
	}

}
