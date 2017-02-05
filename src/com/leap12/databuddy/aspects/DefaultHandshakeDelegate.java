package com.leap12.databuddy.aspects;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.leap12.common.HttpRequest;
import com.leap12.common.HttpResponse;
import com.leap12.common.HttpResponse.HttpException;
import com.leap12.common.HttpResponse.HttpStatusCode;
import com.leap12.common.Log;
import com.leap12.common.NeverThrows;
import com.leap12.common.NonNull;
import com.leap12.common.Reflex;
import com.leap12.common.StringSubstitutor;
import com.leap12.common.http.err.HttpExceptionBadRequest;
import com.leap12.common.http.err.HttpExceptionInternal;
import com.leap12.common.http.err.HttpExceptionNotImplemented;
import com.leap12.common.props.PropsRead;
import com.leap12.common.props.PropsReadWrite;
import com.leap12.databuddy.BaseConnectionDelegate;
import com.leap12.databuddy.Commands;
import com.leap12.databuddy.Commands.CmdResponse;
import com.leap12.databuddy.commands.http.HttpCmd;
import com.leap12.databuddy.commands.http.HttpCmdFactory;
import com.leap12.databuddy.commands.http.annotation.HttpGet;

/**
 * The default launchpad connection. It serves as the Connection "Factory", routing a client to the appropriate connection based on how they connect.
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
					writeMsg( response.toString() );
				} catch ( Exception e ) {
					HttpResponse errResp = new HttpResponse();
					String statusId = errResp.setStatusCode( HttpStatusCode.ERR_INTERNAL, e );
					Log.e( "StatusID: " + statusId );
					writeMsg( errResp.toString() );
				}
				return true;
			}
		} catch ( Exception e ) {
			Log.e( e );
		}
		return false;
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
					if ( m.isAnnotationPresent( HttpGet.class ) ) {
						HttpGet annotation = m.getAnnotation( HttpGet.class );
						uri = annotation.value();

						// acquire data from proposed match
						Map<String, String> params = request.toSlashParams( uri );
						if ( params == null ) {
							continue; // did not match
						}

						params.putAll( request.getQueryParams().toMap() );
						PropsReadWrite propsrw = new PropsReadWrite();
						propsrw.putAll( params );

						// validate the format or fail
						StringSubstitutor strsub = new StringSubstitutor();
						strsub.substitute( annotation.value(), params );

						props = propsrw;
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

	protected void xxhandleHttpRequest( HttpRequest request ) throws Exception {
		// Log.d( request.describe() );

		List<HttpCmd> commands = new ArrayList<>();
		commands.add( Commands.CMD_HTTP_SAVE );
		commands.add( Commands.CMD_HTTP_READ );
		commands.add( Commands.CMD_HTTP_ECHO );
		commands.add( Commands.CMD_HTTP_ADD );
		commands.add( Commands.CMD_HTTP_LIST );

		Iterable<HttpCmd> bestFirst = new HttpCmdFactory( commands ).bestFirst( request );
		for ( HttpCmd httpCmd : bestFirst ) {
			CmdResponse<HttpResponse> cmdResp = httpCmd.executeCommand( this, request );

			// bounce out if cmd failed
			if ( cmdResp.getError() != null ) {
				throw cmdResp.getError();
			} else if ( cmdResp.getStatus().isFailure() ) {
				throw new Exception( "Unrecognized Internal Error" );
			}

			// If unfulfilled, then it was obviously a cmd mismatch, let the next best handle it.
			if ( cmdResp.getStatus().isUnFulfilled() ) {
				continue;
			}

			// We've ruled out the bad, so the response must be usable, use it
			HttpResponse resp = cmdResp.getValue();
			writeMsg( resp.toString() );
			break;
		}
	}

}
