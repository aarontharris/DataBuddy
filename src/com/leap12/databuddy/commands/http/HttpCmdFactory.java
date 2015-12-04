package com.leap12.databuddy.commands.http;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.leap12.common.HttpRequest;

public class HttpCmdFactory {

	private static class HttpCmdRelevanceComparator implements Comparator<HttpCmd> {
		private final HttpRequest mReq;

		public HttpCmdRelevanceComparator( HttpRequest req ) {
			this.mReq = req;
		}

		@Override
		public int compare( HttpCmd a, HttpCmd b ) {
			float aRelevance = a.isCommand( mReq );
			float bRelevance = b.isCommand( mReq );
			if ( aRelevance > bRelevance ) {
				return -1;
			} else if ( aRelevance < bRelevance ) {
				return 1;
			}
			return 0;
		}
	}

	private final List<HttpCmd> commands;

	public HttpCmdFactory( List<HttpCmd> commands ) {
		this.commands = commands;
	}

	/** Not thread safe */
	public HttpCmd findBestCmd( HttpRequest request, float minThreshold, HttpCmd defCmd ) {
		Collections.sort( commands, new HttpCmdRelevanceComparator( request ) );
		HttpCmd cmd = commands.get( 0 );
		if ( cmd.isCommand( request ) >= minThreshold ) {
			return cmd;
		}
		return defCmd;
	}

	/** Not thread safe */
	public Iterable<HttpCmd> bestFirst( HttpRequest request ) {
		Collections.sort( commands, new HttpCmdRelevanceComparator( request ) );
		return commands;
	}
}
