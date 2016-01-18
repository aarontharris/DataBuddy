package com.leap12.common;

import java.util.HashMap;
import java.util.Map;

import com.leap12.common.props.Props;
import com.leap12.common.props.PropsRead;
import com.leap12.common.props.PropsReadWrite;

// http://tools.ietf.org/html/rfc2616
public class HttpRequest {
	public static final String CONTENT_TYPE_APP_X_WWW_FORM_URLENCODED = "application/x-www-form-urlencoded";

	public static HttpRequest newTestHttpRequest() {
		HttpRequest out = new HttpRequest();
		out.valid = true;
		out.headers = new HashMap<>();
		out.queryParams = new PropsReadWrite();
		out.bodyParams = Props.EMPTY;
		return out;
	}

	private Map<String, String> headers;
	private PropsReadWrite bodyParams;
	private PropsReadWrite queryParams;
	private String body;
	private String description = null;

	private boolean valid = false;

	private HttpRequest() {
	}

	public HttpRequest( String requestStr ) {
		try {
			parse( requestStr );
			valid = headers.size() > 0;
		} catch ( Exception e ) {
			valid = false;
		}
	}

	public static boolean isPotentiallyHttpRequest( String requestStr ) {
		// likely candidate if "HTTP/1.1" is in the first line.
		if ( StrUtl.isBefore( requestStr, "HTTP/1.1", "\r\n" ) ) {
			return true;
		}
		return false;
	}

	public boolean isValid() {
		return valid;
	}

	public String getBody() {
		return body;
	}

	/**
	 * @param key - not case-sensitive
	 */
	public boolean containsHeader( String key ) {
		return headers.containsKey( key.toLowerCase() );
	}

	/**
	 * @param key - not case-sensitive
	 */
	public boolean containsHeaders( String... keys ) {
		for ( String key : keys ) {
			if ( !containsHeader( key ) ) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param key - not case-sensitive
	 * @return defVal if key did not exist, empty string if key had no value.
	 */
	public String getHeader( String key, String defVal ) {
		String lkey = key.toLowerCase();
		if ( headers.containsKey( lkey ) ) {
			return headers.get( lkey );
		}
		return defVal;
	}

	/**
	 * @param key - not case-sensitive
	 * @return null if key did not exist, empty string if key had no value.
	 */
	public String getHeader( String key ) {
		return getHeader( key, null );
	}

	public String getUserAgent() {
		return getHeader( "user-agent" );
	}

	public String getPath() {
		return getHeader( "path" );
	}

	public String getHost() {
		return getHeader( "host" );
	}

	public String getMethod() {
		return getHeader( "method" );
	}

	public boolean isGet() {
		return getHeader( "method", "" ).equalsIgnoreCase( "GET" );
	}

	public boolean isPost() {
		return getHeader( "method", "" ).equalsIgnoreCase( "POST" );
	}

	public String getContentType() {
		return getHeader( "content-type" );
	}

	public boolean isContentTypeXwwwFormUrlEncoded() {
		return CONTENT_TYPE_APP_X_WWW_FORM_URLENCODED.equalsIgnoreCase( getContentType() );
	}

	public PropsRead getQueryParams() {
		return queryParams;
	}

	/**
	 * @param key - case-sensitive
	 * @return defVal if key did not exist, empty string if key had no value.
	 */
	public String getQueryParam( String key, String defVal ) {
		return queryParams.getString( key, defVal );
	}

	/**
	 * @param key - case-insensitive
	 * @return null if key did not exist, empty string if key had no value.
	 */
	public String getQueryParam( String key ) {
		return getQueryParam( key, null );
	}

	/**
	 * @param key - case-insensitive
	 */
	public boolean containsQueryParam( String key ) {
		return queryParams.containsKey( key );
	}

	/**
	 * @param key - case-insensitive
	 */
	public boolean containsQueryParams( String... keys ) {
		for ( String key : keys ) {
			if ( !containsQueryParam( key ) ) {
				return false;
			}
		}
		return true;
	}

	public PropsRead getBodyParams() {
		return bodyParams;
	}

	/**
	 * @param key - case-sensitive
	 * @return defVal if key did not exist, empty string if key had no value.
	 */
	public String getBodyParam( String key, String defVal ) {
		return bodyParams.getString( key, defVal );
	}

	/**
	 * @param key - case-insensitive
	 * @return null if key did not exist, empty string if key had no value.
	 */
	public String getBodyParam( String key ) {
		return getBodyParam( key, null );
	}

	/**
	 * @param key - case-insensitive
	 */
	public boolean containsBodyParam( String key ) {
		return bodyParams.containsKey( key );
	}

	/**
	 * @param key - case-insensitive
	 */
	public boolean containsBodyParams( String... keys ) {
		for ( String key : keys ) {
			if ( !containsBodyParam( key ) ) {
				return false;
			}
		}
		return true;
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
				String value = queryParams.getString( key );
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
		this.queryParams = new PropsReadWrite();

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
				headers.put( "httpversion", httpVersion );

				String pathAndParams = parts[1];
				String path = pathAndParams;
				if ( pathAndParams.contains( "?" ) ) {
					String[] pathAndParamsParts = pathAndParams.split( "\\?", 2 );
					path = pathAndParamsParts[0];

					if ( pathAndParamsParts.length > 1 ) {
						String params = pathAndParamsParts[1];
						queryParams.putAll( StrUtl.toMap( params, "=", "&" ) );
					}
				}
				headers.put( "path", path );
			} else { // HEADER VALUES
				String[] parts = line.split( ": ", 2 );
				String key = parts[0];
				String value = parts[1];
				headers.put( key.toLowerCase(), value );

				headers.put( "hostname", value );
				if ( "host".equalsIgnoreCase( key ) && value.contains( ":" ) ) {
					String[] nameAndPort = value.split( ":" );
					headers.put( "hostname", nameAndPort[0] );
					headers.put( "hostport", nameAndPort[1] );
				}
			}
		}

		if ( httpParts.length > 1 ) {
			body = httpParts[1];

			if ( isContentTypeXwwwFormUrlEncoded() ) {
				bodyParams = new PropsReadWrite();
				bodyParams.putAll( StrUtl.toMap( body, "=", "&" ) );
			}
		}
	}
}
