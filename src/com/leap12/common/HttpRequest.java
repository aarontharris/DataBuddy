package com.leap12.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
	private Map<String, String> headers;
	private Map<String, String> queryParams;
	private String body;
	private String description = null;

	public HttpRequest( String requestStr ) {
		try {
			parse( requestStr );
		} catch ( Exception e ) {
			// squelch -- use isValid()
		}
	}

	public static boolean isPotentiallyHttpRequest( String requestStr ) {
		if ( StrUtl.isNotEmpty( requestStr ) ) {
			requestStr.matches( "[Cc]ontent-[Ll]ength: \\d+\r\n" );
		}
		return false;
	}

	public boolean isValid() {
		throw new UnsupportedOperationException();
	}

	public String getHeader( String key ) {
		return headers.get( key );
	}

	/**
	 * @param key
	 * @return null if key did not exist, empty string if key had no value.
	 */
	public String getQueryParam( String key ) {
		return queryParams.get( key );
	}

	public String describe() {
		return describe( "\n" );
	}

	public String describe( String newLine ) {
		if ( description == null ) {
			StringBuilder sb = new StringBuilder();
			for ( String key : headers.keySet() ) {
				String value = headers.get( key );
				sb.append( String.format( "HEADER: '%s'='%s'", key, value ) ).append( newLine );
			}
			for ( String key : queryParams.keySet() ) {
				String value = queryParams.get( key );
				sb.append( String.format( "PARAMS: '%s'='%s'", key, value ) ).append( newLine );
			}
			if ( body != null ) {
				sb.append( String.format( "  BODY: '%s'", body ) ).append( newLine );
			}
			description = sb.toString();
		}
		return description;
	}

	private void parse( String requestStr ) throws Exception {
		this.headers = new HashMap<>();
		this.queryParams = Collections.emptyMap();

		String[] httpParts = requestStr.split( "\r\n\r\n" );
		String responseHeaders = httpParts[0];

		String[] lines = responseHeaders.split( "\r\n" );
		for ( int i = 0; i < lines.length; i++ ) {
			String line = lines[i];
			if ( i == 0 ) { // METHOD TYPE, PATH, PARAMS
				String[] parts = line.split( " " );

				String method = parts[0];
				headers.put( "method", method );

				String httpVersion = parts[2].split( "/" )[1];
				headers.put( "httpVersion", httpVersion );

				String pathAndParams = parts[1];
				String path = pathAndParams;
				if ( pathAndParams.contains( "?" ) ) {
					String[] pathAndParamsParts = pathAndParams.split( "\\?", 2 );
					path = pathAndParamsParts[0];

					if ( pathAndParamsParts.length > 1 ) {
						String params = pathAndParamsParts[1];
						queryParams = StrUtl.toMap( params, "=", "&" );
					}
				}
				headers.put( "path", path );
			} else { // HEADER VALUES
				String[] parts = line.split( ": ", 2 );
				String key = parts[0];
				String value = parts[1];
				headers.put( key, value );

				headers.put( "hostName", value );
				if ( "host".equalsIgnoreCase( key ) && value.contains( ":" ) ) {
					String[] nameAndPort = value.split( ":" );
					headers.put( "hostName", nameAndPort[0] );
					headers.put( "hostPort", nameAndPort[1] );
				}
			}
		}

		if ( httpParts.length > 1 ) {
			body = httpParts[1];
		}
	}

	public static void main( String args[] ) {
		String requestStr = "Content-Length: 6457\r\n";
		if ( HttpRequest.isPotentiallyHttpRequest( requestStr ) ) {
			System.out.println( "IS" );
		} else {
			System.out.println( "IS NOT" );
		}
	}
}
