package com.leap12.common;

public class Log {
	private static final long startTime = System.currentTimeMillis();

	private static String prefix() {
		long now = System.currentTimeMillis();
		return String.format("[%04d][%08d] ", Thread.currentThread().getId(), (now - startTime));
	}

	public static void d(String format, Object... args) {
		System.out.println(String.format(prefix() + format, args));
	}

	public static void e(Throwable error, String format, Object... args) {
		e(format, args);
		e(error);
	}

	public static void e(String format, Object... args) {
		System.err.println(prefix() + String.format(format, args));
	}

	public static void e(Throwable error) {
		error.printStackTrace();
	}

}
