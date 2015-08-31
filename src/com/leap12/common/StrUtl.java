package com.leap12.common;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StrUtl {
	public static final String EMPTY = "";

	public static boolean isNotEmpty(String string) {
		return !isEmpty(string);
	}

	public static boolean isEmpty(String string) {
		return string == null || string.isEmpty();
	}

	/** @return true if both are equal, but false if either is empty */
	public static boolean equals(String a, String b) {
		if (isNotEmpty(a) && isNotEmpty(b)) {
			a.equals(b);
		}
		return false;
	}

	/** @return true if A starts with B, false if either is empty */
	public static boolean startsWith(String a, String b) {
		if (isNotEmpty(a) && isNotEmpty(b)) {
			return a.startsWith(b);
		}
		return false;
	}

	/** @return true if A ends with B, false if either is empty */
	public static boolean endsWith(String a, String b) {
		if (isNotEmpty(a) && isNotEmpty(b)) {
			a.endsWith(b);
		}
		return false;
	}

	/** @return true if A contains B, false if either is empty */
	public static boolean contains(String a, String b) {
		if (isNotEmpty(a) && isNotEmpty(b)) {
			a.contains(b);
		}
		return false;
	}

	/**
	 * @param in
	 * @param pairDelim
	 * @param recordDelim
	 * @return
	 * @throws Exception if any of the values are null
	 */
	public static List<Pair<String, String>> toPairs(String in, String pairDelim, String recordDelim) throws Exception {
		List<Pair<String, String>> out = Collections.emptyList();
		String[] records = in.split(recordDelim);
		if (records.length > 0) {
			List<Pair<String, String>> tmp = new ArrayList<>();
			for (String record : records) {
				String[] parts = record.split(pairDelim);
				if (parts.length != 2) {
					throw new IllegalStateException("Expected pairs but got an odd number");
				}
				tmp.add(new Pair<String, String>(parts[0], parts[1]));
			}
			out = tmp;
		}
		return out;
	}

	/**
	 * @param in
	 * @param pairDelim
	 * @param recordDelim
	 * @return
	 * @throws Exception if any of the values are null
	 */
	public static Map<String, String> toMap(String in, String pairDelim, String recordDelim) throws Exception {
		Map<String, String> out = Collections.emptyMap();
		String[] records = in.split(recordDelim);
		if (records.length > 0) {
			Map<String, String> tmp = new HashMap<>();
			for (String record : records) {
				String[] parts = record.split(pairDelim);
				if (parts.length != 2) {
					throw new IllegalStateException("Expected pairs but got an odd number");
				}
				tmp.put(parts[0], parts[1]);
			}
			out = tmp;
		}
		return out;
	}

	public static String toString(InputStream in) throws Exception {
		return toString(in, 1024);
	}

	public static String toString(InputStream in, int readSize) throws Exception {
		if (in == null) {
			return null;
		}

		BufferedInputStream bIn = new BufferedInputStream(in, readSize);

		byte[] data = new byte[readSize];
		StringBuilder sb = new StringBuilder();
		while (bIn.read(data, 0, readSize) > 0) {
			sb.append(new String(data, StandardCharsets.UTF_8));
		}
		return sb.toString();
	}

}
