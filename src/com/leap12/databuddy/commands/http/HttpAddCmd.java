package com.leap12.databuddy.commands.http;

import com.leap12.common.HttpRequest;
import com.leap12.common.HttpResponse;
import com.leap12.common.StrUtl;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.data.DataStore;

/**
 * Add an item without needing to specify a key.<br>
 * The purose is to treat this as a collection of items<br>
 * so that they may be retrieved as a collection.<br>
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
 * action=add
 * topic=[the primary focus of the data]
 * subtopic=[the secondary focus of the data]
 * key=[OPTIONAL: defaults to DEFAULT_{currentTimeMillis}_{TID}]
 * value=[the actual value]
 * 
 * </pre>
 */
public class HttpAddCmd extends HttpPostCmd {

	@Override
	protected float computeRelevance( HttpRequest request ) throws Exception {
		if ( "add".equalsIgnoreCase( request.getBodyParam( "action", StrUtl.EMPTY ) ) ) {
			if ( request.isContentTypeXwwwFormUrlEncoded() && request.containsBodyParams( "topic", "subtopic", "value" ) ) {
				return 1f * super.computeRelevance( request );
			}
		}
		return 0f;
	}

	@Override
	protected void exec( BaseConnectionDelegate connection, HttpRequest req, HttpResponse resp, DataStore db ) throws Exception {
		String topic = req.getBodyParams().getStringRequired( "topic" );
		String subtopic = req.getBodyParams().getStringRequired( "subtopic" );
		String key = req.getBodyParams().getString( "key", ( "DEFAULT_" + String.valueOf( System.currentTimeMillis() ) + "_" + +Thread.currentThread().getId() ) );
		String value = req.getBodyParams().getStringRequired( "value" );

		try {
			db.begin();
			db.saveString( topic, subtopic, key, value );
		} finally {
			db.end();
		}
	}

}
