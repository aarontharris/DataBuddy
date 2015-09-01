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
import com.leap12.databuddy.commands.HelpCmd;

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
		 * <B>Never Throws</b> instead see {@link CmdRequest#getError()} and {@link CmdRequest#getStatus()}
		 * @param connection
		 * @param msg
		 * @return
		 */
		public abstract CmdRequest<T> parseCommand(BaseConnection connection, String msg);
	}

	public static class CmdRequest<T> {
		public static enum RequestStatus {
			SUCCESS(0, "Success"), //
			FAIL_UNKNOWN(1, "Internal Failure please log a bug."), //
			FAIL_INVALID_CMD(2, "Invalid Command"), //
			FAIL_INVALID_CMD_FORMAT(3, "Invalid Command Format"), //
			FAIL_INVALID_CMD_ARGUMENTS(4, "Invalid Command Arguments"), //
			FAIL_NOT_AUTHORIZED(5, "Not Authorized"), //
			;

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
				return RequestStatus.values()[code]; // not safe but a hell of a lot more efficient and easier, so I'll be careful I promise.
			}
		}

		/**
		 * Seems cleaner and more flexible than a builder pattern.<br>
		 * However unlike the builder pattern, validation is the responsibility of the creator (this is intentional).
		 */
		public static class CmdRequestMutable<T> extends CmdRequest<T> {
			public CmdRequestMutable() {
				super();
			}

			public CmdRequestMutable(T value, RequestStatus status) {
				super(value, status);
			}

			public CmdRequestMutable(T value, RequestStatus status, Throwable error) {
				super(value, status, error);
			}

			public CmdRequestMutable(T value, RequestStatus status, Throwable error, Props args) {
				super(value, status, error, args);
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
		RequestStatus mStatus;
		String mStatusMessage;
		Throwable mError;
		Props mArgs;

		private CmdRequest() {
			this(null, RequestStatus.FAIL_UNKNOWN);
		}

		public CmdRequest(T value, RequestStatus status) {
			this(value, status, null);
		}

		public CmdRequest(T value, RequestStatus status, Throwable error) {
			this(value, status, error, Props.EMPTY);
		}

		public CmdRequest(T value, RequestStatus status, Throwable error, Props args) {
			this.mValue = value;
			this.mStatus = status;
			this.mError = error;
			this.mArgs = args;
			this.mStatusMessage = StrUtl.EMPTY;
			if (this.mArgs == null) {
				this.mArgs = Props.EMPTY;
			}
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
