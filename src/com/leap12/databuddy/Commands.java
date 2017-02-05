package com.leap12.databuddy;

import java.util.Collections;
import java.util.List;

import com.leap12.common.StrUtl;
import com.leap12.common.props.Props;
import com.leap12.common.props.PropsRead;
import com.leap12.common.props.PropsReadWrite;
import com.leap12.common.props.PropsWrite;
import com.leap12.databuddy.commands.http.HttpAddCmd;
import com.leap12.databuddy.commands.http.HttpEchoCmd;
import com.leap12.databuddy.commands.http.HttpListCmd;
import com.leap12.databuddy.commands.http.HttpReadCmd;
import com.leap12.databuddy.commands.http.HttpSaveCmd;
import com.leap12.databuddy.ex.DBCmdException;

public final class Commands {

	public static enum Role {
		user, sysop;

		public String toValue() {
			return this.name();
		}

		public static Role fromValue( String value ) throws NullPointerException, IllegalArgumentException {
			return Role.valueOf( value );
		}
	}



	public static abstract class Command<IN, OUT> {
		private final Class<OUT> mType;

		public Command( Class<OUT> type ) {
			this.mType = type;
		}

		public Class<OUT> getType() {
			return mType;
		}

		/**
		 * Type displayed to the user as the expected response type for their command.<br>
		 * Defaults to {@link #getType()}<br>
		 * <br>
		 * The type displayed to the user does not necessarily need to match the internal request type. Internally a command may return data to
		 * perform an action, but the response to the user could be Void or another type;
		 */
		public Class<?> getDisplayType() {
			return getType();
		}

		public String getDescription() {
			return StrUtl.EMPTY;
		}

		/**
		 * @param in
		 * @return 0.0 to 1.0 where 0.0 is 0% match and 1.0 is 100% match
		 */
		public abstract float isCommand( IN in );

		/**
		 * <B>Never Throws</b> instead see {@link CmdResponse#getError()} and {@link CmdResponse#getStatus()}
		 * 
		 * @param connection
		 * @param msg
		 * @return Never Null
		 */
		public abstract CmdResponse<OUT> executeCommand( BaseConnectionDelegate connection, IN input );
	}



	public static abstract class StrCommand<OUT> extends Command<String, OUT> {
		private final String mCmdName;
		private final String mCmdFormat;

		public StrCommand( String cmdName, String cmdFormat, Class<OUT> type ) {
			super( type );
			this.mCmdName = cmdName;
			this.mCmdFormat = cmdFormat;
		}

		public String getName() {
			return mCmdName;
		}

		public String getFormat() {
			return mCmdFormat;
		}

		@Override
		public float isCommand( String in ) {
			return StrUtl.isNotEmpty( in ) && in.startsWith( getName() ) ? 1f : 0f;
		}
	}

	public static ResponseStatus toResponseStatus( Exception e ) {
		if ( e instanceof DBCmdException ) {
			return ( (DBCmdException) e ).getStatus();
		}
		return ResponseStatus.FAIL_UNKNOWN;
	}



	public static enum ResponseStatus {
		SUCCESS( 0, "Success" ), //
		UNFULFILLED( 1, "Unfulfilled" ), //

		// < 0 is error
		FAIL_UNKNOWN( -1, "Internal Failure please log a bug." ), //
		FAIL_INVALID_CMD_STATE( -2, "Invalid Command State" ), //
		FAIL_INVALID_CMD_FORMAT( -3, "Invalid Command Format" ), //
		FAIL_INVALID_CMD_ARGUMENTS( -4, "Invalid Command Arguments" ), //
		FAIL_NOT_AUTHORIZED( -5, "Not Authorized" ), //
		;

		private static final ResponseStatus[] idMap = new ResponseStatus[] {
		        SUCCESS,
		        UNFULFILLED,
		        FAIL_UNKNOWN,
		        FAIL_INVALID_CMD_STATE,
		        FAIL_INVALID_CMD_FORMAT,
		        FAIL_INVALID_CMD_ARGUMENTS,
		        FAIL_NOT_AUTHORIZED,
		};

		private final int mCode;
		private final String mMessage;

		ResponseStatus( int code, String message ) {
			this.mCode = code;
			this.mMessage = message;
		}

		public int getCode() {
			return mCode;
		}

		public String getMessage() {
			return mMessage;
		}

		public boolean isSuccess() {
			return SUCCESS.equals( this );
		}

		public boolean isUnFulfilled() {
			return UNFULFILLED.equals( this );
		}

		public boolean isFailure() {
			return this.mCode < 0;
		}

		public static ResponseStatus fromCode( int code ) {
			return idMap[code];
		}
	}



	public static class CmdResponse<T> {

		/**
		 * Seems cleaner and more flexible than a builder pattern.<br>
		 * However unlike the builder pattern, validation is the responsibility of the creator (this is intentional).
		 */
		public static class CmdResponseMutable<T> extends CmdResponse<T> {

			public CmdResponseMutable( Class<T> type ) {
				super( type );
			}

			public CmdResponseMutable( Class<T> type, T value ) {
				super( type, value );
			}

			public CmdResponseMutable( Class<T> type, Exception error ) {
				super( type, error );
			}

			public CmdResponseMutable( Class<T> type, DBCmdException error ) {
				super( type, error );
			}

			public void setStatusSuccess( T value ) {
				this.mValue = value;
				this.mStatusMessage = "success";
				this.mStatus = ResponseStatus.SUCCESS;
			}

			public void setStatusUnfulfilled( String statusMessage ) {
				this.mStatusMessage = statusMessage;
				this.mStatus = ResponseStatus.UNFULFILLED;
			}

			public void setStatusFail( DBCmdException e ) {
				this.mStatusMessage = e.getMessage();
				this.mStatus = e.getStatus();
			}

			public void setStatusFail( Exception e ) {
				this.mStatusMessage = e.getMessage();
				this.mStatus = ResponseStatus.FAIL_UNKNOWN;
			}

			public final PropsWrite setArgs() {
				if ( mArgs == Props.EMPTY ) {
					mArgs = new PropsReadWrite();
				}
				return mArgs;
			}
		}

		T mValue;
		Class<T> mType;
		ResponseStatus mStatus = ResponseStatus.UNFULFILLED;
		String mStatusMessage;
		Exception mError;
		PropsReadWrite mArgs;

		private CmdResponse( Class<T> type ) {
			this( type, null, Void.class.equals( type ) ? ResponseStatus.SUCCESS : ResponseStatus.UNFULFILLED, null, null );
		}

		private CmdResponse( Class<T> type, T value ) {
			this( type, value, ResponseStatus.SUCCESS, null, null );
		}

		private CmdResponse( Class<T> type, Exception error ) {
			this( type, null, ResponseStatus.FAIL_UNKNOWN, error, Props.EMPTY );
		}

		private CmdResponse( Class<T> type, DBCmdException error ) {
			this( type, null, error.getStatus(), error, Props.EMPTY );
		}

		private CmdResponse( Class<T> type, T value, ResponseStatus status, Exception error, PropsReadWrite args ) {
			this.mType = type;
			this.mValue = value;
			this.mStatus = status;
			this.mError = error;
			this.mArgs = args;
			this.mStatusMessage = error != null ? error.getMessage() : status.name();
			if ( this.mArgs == null ) {
				this.mArgs = Props.EMPTY;
			}
		}

		public Class<T> getType() {
			return mType;
		}

		public final T getValue() {
			return mValue;
		}

		public final ResponseStatus getStatus() {
			return mStatus;
		}

		public final String getStatusMessage() {
			return mStatusMessage;
		}

		public final Exception getError() {
			return mError;
		}

		public final PropsRead getArgs() {
			return mArgs;
		}

	}

	public static final HttpEchoCmd CMD_HTTP_ECHO = new HttpEchoCmd();
	public static final HttpSaveCmd CMD_HTTP_SAVE = new HttpSaveCmd();
	public static final HttpReadCmd CMD_HTTP_READ = new HttpReadCmd();
	public static final HttpAddCmd CMD_HTTP_ADD = new HttpAddCmd();
	public static final HttpListCmd CMD_HTTP_LIST = new HttpListCmd();

	private static final Commands self = new Commands();

	public static Commands get() {
		return self;
	}

	private Commands() {
	}

	public List<Command<?, ?>> getCommands( Role role ) {
		return Collections.emptyList();
	}

}
