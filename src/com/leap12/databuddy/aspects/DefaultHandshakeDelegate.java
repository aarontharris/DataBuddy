package com.leap12.databuddy.aspects;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import com.leap12.common.Log;
import com.leap12.common.NeverThrows;
import com.leap12.common.NonNull;
import com.leap12.common.Reflex;
import com.leap12.common.StringSubstitutor;
import com.leap12.common.http.HttpRequest;
import com.leap12.common.http.HttpResponse;
import com.leap12.common.http.HttpResponse.HttpException;
import com.leap12.common.http.HttpResponse.HttpStatusCode;
import com.leap12.common.http.annot.HttpGet;
import com.leap12.common.http.annot.HttpPost;
import com.leap12.common.http.err.HttpExceptionBadRequest;
import com.leap12.common.http.err.HttpExceptionInternal;
import com.leap12.common.http.err.HttpExceptionNotImplemented;
import com.leap12.common.props.PropsRead;
import com.leap12.common.props.PropsReadWrite;
import com.leap12.databuddy.BaseConnectionDelegate;

/**
 * The default launchpad connection. It serves as the Connection "Factory", routing a client to the appropriate connection based on how they connect.
 * 
 * <pre>
 * Example Post Request handler
 * 
 * // for ?at={str$at}, the at and {str$at} must match
 * @HttpPost( "/user/create/{str$username}?at={str$at}" )
 * private void createUser( HttpRequest request, HttpResponse response, PropsRead params ) {
 * 	try {
 * 		Log.d( &quot;Received Request matching '%s': %s, %s&quot;,
 * 		        request.getPath(),
 * 		        params.getString( &quot;username&quot; ),
 * 		        params.getString( &quot;at&quot; )
 * 		        );
 * 
 * 		// DataStore db = getDb( animalsKey );
 * 
 * 		response.appendBody( "WOOT" );
 * 	} catch ( Exception e ) {
 * 		response.setStatusCode( e );
 * 	}
 * }
 * 
 * </pre>
 */
public class DefaultHandshakeDelegate extends BaseConnectionDelegate {

	@Override
	protected void onReceivedMsg( String msg ) throws Exception {
		msg = msg.trim();

		if ( HttpRequest.isPotentiallyHttpRequest( msg ) ) {
			if ( onReceivedHttpMsg( msg ) ) {
				return; // handled
			}
		}

		// Unhandled
		Log.d( "\n\n## Unrecognized Request ##\n\n" );
	}

	/**
	 * Process the request if it is determined to be a Http Request.<br>
	 * True must be returned if this is a valid Http Request, even if we are unable to fulfill it.
	 * 
	 * @param msg the complete string request (may not be a Http Request)
	 * @return true if this is a Http Request, false if not.
	 */
	@NeverThrows
	protected boolean onReceivedHttpMsg( String msg ) {
		try {
			HttpRequest request = new HttpRequest( msg );
			if ( request.isValid() ) {
				try {
					getClientConnection().setKeepAlive( false );
					HttpResponse response = handleHttpRequest( request );
					if ( response.getError() != null ) {
						Log.e( response.getError() );
					}
					writeMsg( response.toString() );
				} catch ( Exception e ) {
					HttpResponse errResp = new HttpResponse();
					errResp.setStatusCode( HttpStatusCode.ERR_INTERNAL, e );
					if ( errResp.getError() != null ) {
						Log.e( errResp.getError() );
					}
					writeMsg( errResp.toString() );
				}
				return true;
			}
		} catch ( Exception e ) {
			Log.e( e );
		}
		return false;
	}

	private PropsRead parseHttpRequest( HttpRequest request, Method method ) throws Exception {
		if ( method.isAnnotationPresent( request.getMethod().getAnnotationClass() ) ) {

			String uri = null;

			switch ( request.getMethod() ) {
			case GET:
				uri = method.getAnnotation( HttpGet.class ).value();
				break;
			case POST:
				uri = method.getAnnotation( HttpPost.class ).value();
				break;
			default:
				throw new UnsupportedOperationException( "Not Yet" );
			}

			// acquire data from proposed match
			Map<String, String> params = request.toSlashParams( uri );
			if ( params != null ) {
				params.putAll( request.getQueryParams().toMap() );
				PropsReadWrite propsrw = new PropsReadWrite();
				propsrw.putAll( params );

				// validate the format or fail
				StringSubstitutor strsub = new StringSubstitutor();
				strsub.substitute( uri, params );

				return propsrw;
			}
		}

		return null;
	}

	@NonNull
	@NeverThrows
	protected HttpResponse handleHttpRequest( HttpRequest request ) {
		HttpResponse response = null;
		try {
			List<Method> methods = Reflex.getAllMethods( getClass(), DefaultHandshakeDelegate.class );
			Method method = null;
			PropsRead props = null;
			String uri = null;

			// Find a match
			try {
				for ( Method m : methods ) {
					PropsRead p = parseHttpRequest( request, m );
					if ( p != null ) {
						props = p;
						method = m;
						break;
					}
				}

				if ( method == null ) {
					throw new HttpExceptionNotImplemented( "Service does not exist " + request.getPath() );
				}
			} catch ( HttpException e ) {
				throw e;
			} catch ( Exception e ) {
				Log.e( request.describe() );
				throw new HttpExceptionBadRequest( "Bad Request " + request.getPath(), e );
			}

			// delegate the request to the successful match
			// method is never null at this point
			try {
				response = new HttpResponse();
				method.setAccessible( true );
				method.invoke( this, request, response, props );
			} catch ( Exception e ) {
				Log.e( "ERR: Likely invalid request method signature for %s", uri );
				if ( e instanceof HttpException ) {
					throw e;
				}
				throw new HttpExceptionInternal( "Error fulfilling request", e );
			}
		} catch ( HttpException e ) {
			Log.e( e );
			response = new HttpResponse();
			String statusId = response.setStatusCode( e.getStatusCode(), e );
			Log.e( "StatusID: " + statusId );
		} catch ( Exception e ) {
			Log.e( e );
			response = new HttpResponse();
			String statusId = response.setStatusCode( HttpStatusCode.ERR_INTERNAL, e );
			Log.e( "StatusID: " + statusId );
		}

		return response;
	}

}
