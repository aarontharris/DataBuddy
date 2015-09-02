package com.leap12.databuddy;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import com.leap12.common.StrUtl;
import com.leap12.common.props.Props;
import com.leap12.common.props.PropsRead;
import com.leap12.common.props.PropsReadWrite;
import com.leap12.common.props.PropsWrite;
import com.leap12.databuddy.commands.AuthCmd;
import com.leap12.databuddy.commands.GetCmd;
import com.leap12.databuddy.commands.HelpCmd;
import com.leap12.databuddy.commands.PutCmd;

public final class Commands {
	public static final Comparator<Command<?>> COMMAND_COMPARATOR = new Comparator<Command<?>>() {
		@Override
		public int compare(Command<?> o1, Command<?> o2) {
			return o1.getName().compareTo(o2.getName());
		}
	};

	public static enum Role {
		user, sysop, ;

		public String toValue() {
			return this.name();
		}

		public static Role fromValue(String value) throws NullPointerException, IllegalArgumentException {
			return Role.valueOf(value);
		}
	}

	public static abstract class Command<T> {
		private final String mCmdName;
		private final String mCmdFormat;
		private final Class<T> mType;

		public Command(String cmdName, String cmdFormat, Class<T> type) {
			this.mCmdName = cmdName;
			this.mCmdFormat = cmdFormat;
			this.mType = type;
		}

		public String getName() {
			return mCmdName;
		}

		public String getFormat() {
			return mCmdFormat;
		}

		public Class<T> getType() {
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

		public boolean isCommand(String msg) {
			return StrUtl.startsWith(msg, getName());
		}

		/**
		 * <B>Never Throws</b> instead see {@link CmdResponse#getError()} and {@link CmdResponse#getStatus()}
		 * @param connection
		 * @param msg
		 * @return
		 */
		public abstract CmdResponse<T> executeCommand(BaseConnection connection, String msg);
	}

	@SuppressWarnings("serial")
	public static class DBuddyException extends Exception {
		public DBuddyException(String msg, Throwable e) {
			super(msg, e);
		}
	}

	@SuppressWarnings("serial")
	public static class DBuddyFormatException extends DBuddyException {
		public DBuddyFormatException(String msg, Throwable e) {
			super(msg, e);
		}
	}

	public static class DBuddyArgsException extends DBuddyException {
		public DBuddyArgsException(String msg, Throwable e) {
			super(msg, e);
		}
	}

	public static RequestStatus toRequestStatus(Throwable e) {
		if (e instanceof DBuddyArgsException) {
			return RequestStatus.FAIL_INVALID_CMD_ARGUMENTS;
		} else if (e instanceof DBuddyException) {
			return RequestStatus.FAIL_INVALID_CMD_FORMAT;
		}
		return RequestStatus.FAIL_UNKNOWN;
	}

	public static enum RequestStatus {
		SUCCESS(0, "Success"), //
		UNFULFILLED(1, "Unfulfilled"), //
		FAIL_UNKNOWN(2, "Internal Failure please log a bug."), //
		FAIL_INVALID_CMD(3, "Invalid Command"), //
		FAIL_INVALID_CMD_FORMAT(4, "Invalid Command Format"), //
		FAIL_INVALID_CMD_ARGUMENTS(5, "Invalid Command Arguments"), //
		FAIL_NOT_AUTHORIZED(6, "Not Authorized"), //
		;

		private static final RequestStatus[] idMap = new RequestStatus[] {
				SUCCESS, // 0
				UNFULFILLED, // 1
				FAIL_UNKNOWN, // 2
				FAIL_INVALID_CMD, // 3
				FAIL_INVALID_CMD_FORMAT, // 4
				FAIL_INVALID_CMD_ARGUMENTS, // 5
				FAIL_NOT_AUTHORIZED, // 6
		};

		private final int mCode;
		private final String mMessage;

		RequestStatus(int code, String message) {
			this.mCode = code;
			this.mMessage = message;
		}

		public int getCode() {
			return mCode;
		}

		public String getMessage() {
			return mMessage;
		}

		public static RequestStatus fromCode(int code) {
			return idMap[code];
		}
	}

	public static class CmdResponse<T> {

		/**
		 * Seems cleaner and more flexible than a builder pattern.<br>
		 * However unlike the builder pattern, validation is the responsibility of the creator (this is intentional).
		 */
		public static class CmdResponseMutable<T> extends CmdResponse<T> {
			public CmdResponseMutable(Class<T> type) {
				super(type);
			}

			public CmdResponseMutable(Class<T> type, T value, RequestStatus status) {
				super(type, value, status);
			}

			public CmdResponseMutable(Class<T> type, T value, RequestStatus status, Throwable error) {
				super(type, value, status, error);
			}

			public CmdResponseMutable(Class<T> type, T value, RequestStatus status, Throwable error, Props args) {
				super(type, value, status, error, args);
			}

			public void setValue(T value) {
				this.mValue = value;
			}

			public void setValue(T value, RequestStatus status) {
				setValue(value);
				setStatus(status);
			}

			public void setStatus(RequestStatus status) {
				this.mStatus = status;
			}

			public void setStatusMessage(String statusMessage) {
				this.mStatusMessage = statusMessage;
			}

			public void setError(Throwable error) {
				this.mError = error;
			}

			public void setStatus(RequestStatus status, String statusMessage) {
				setStatus(status);
				setStatusMessage(statusMessage);
			}

			public void setError(Throwable error, RequestStatus status, String statusMessage) {
				setError(error);
				setStatus(status);
				setStatusMessage(statusMessage);
			}

			public void setError(Throwable error, RequestStatus status) {
				setError(error);
				setStatus(status);
			}

			public final PropsWrite setArgs() {
				if (mArgs == Props.EMPTY) {
					mArgs = new PropsReadWrite();
				}
				return mArgs;
			}
		}

		T mValue;
		Class<T> mType;
		RequestStatus mStatus;
		String mStatusMessage;
		Throwable mError;
		Props mArgs;

		private CmdResponse(Class<T> type) {
			this(type, null, RequestStatus.FAIL_UNKNOWN);
		}

		public CmdResponse(Class<T> type, T value, RequestStatus status) {
			this(type, value, status, null);
		}

		public CmdResponse(Class<T> type, T value, RequestStatus status, Throwable error) {
			this(type, value, status, error, Props.EMPTY);
		}

		public CmdResponse(Class<T> type, T value, RequestStatus status, Throwable error, Props args) {
			this.mType = type;
			this.mValue = value;
			this.mStatus = status;
			this.mError = error;
			this.mArgs = args;
			this.mStatusMessage = StrUtl.EMPTY;
			if (this.mArgs == null) {
				this.mArgs = Props.EMPTY;
			}
		}

		public Class<T> getType() {
			return mType;
		}

		public final T getValue() {
			return mValue;
		}

		public final RequestStatus getStatus() {
			return mStatus;
		}

		public final String getStatusMessage() {
			return mStatusMessage;
		}

		public final Throwable getError() {
			return mError;
		}

		public final PropsRead getArgs() {
			return mArgs;
		}

	}

	public static final AuthCmd CMD_AUTH = new AuthCmd();
	public static final HelpCmd CMD_HELP = new HelpCmd();
	public static final PutCmd CMD_PUT = new PutCmd();
	public static final GetCmd CMD_GET = new GetCmd();

	private static final Commands self = new Commands();

	public static Commands get() {
		return self;
	}

	private Commands() {
	}

	public List<Command<?>> getCommands(Role role) {
		return Collections.emptyList();
	}

}
