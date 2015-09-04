package com.leap12.common;

public class Log {

	public static void d(String format, Object... args) {
		System.out.println(String.format("[" + Thread.currentThread().getId() + "]" + format, args));
	}

	public static void e(Throwable error, String format, Object... args) {
		System.err.println(String.format(format, args));
		e(error);
	}

	public static void e(String format, Object... args) {
		System.err.println(String.format(format, args));
	}

	public static void e(Throwable error) {
		error.printStackTrace();
	}

}
