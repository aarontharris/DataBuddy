package com.leap12.common;

import java.util.HashSet;
import java.util.Set;

public class HttpResponse {
	public static enum HttpStatusCode {
		CODE_200(200, "OK"),
		CODE_400(400, "Internal Server Error");

		private final int code;
		private final String msg;
		HttpStatusCode(int code, String msg) {
			this.code = code;
			this.msg = msg;
		}
	}

	private final Set<Pair<String, String>> defaultHeaders;
	private HttpStatusCode mCode = HttpStatusCode.CODE_200;
	private Set<Pair<String, String>> mHeaders;
	private StringBuilder mBodyBuilder;

	public HttpResponse() {
		defaultHeaders = new HashSet<>();
		defaultHeaders.add(new Pair<String, String>("Content-Type", "text/html"));
		defaultHeaders.add(new Pair<String, String>("Server", "DataBuddy/1.1"));
	}

	public void addHeader(String key, String value) {
		if (mHeaders == null) {
			mHeaders = new HashSet<>();
		}
		mHeaders.add(new Pair<String, String>(key, value));
	}

	public void setStatusCode(HttpStatusCode code) {
		this.mCode = code;
	}

	public StringBuilder getBodyBuilder() {
		if (mBodyBuilder == null) {
			setBody(null); // initialize the builder
		}
		return mBodyBuilder;
	}

	public void setBody(String body) {
		mBodyBuilder = new StringBuilder();
		if (body != null) {
			mBodyBuilder.append(body);
		}
	}

	public void appendBody(String string) {
		getBodyBuilder().append(string);
	}

	@Override
	public String toString() {
		String body = getBodyBuilder().toString();
		StringBuilder out = new StringBuilder();

		Set<Pair<String, String>> headers = new HashSet<>();
		headers.addAll(mHeaders); // precedence over defaults
		headers.addAll(defaultHeaders); // does not overwrite

		out.append("HTTP/1.1 " + mCode.code + " " + mCode.msg + "\r\n");
		for (Pair<String, String> pair : headers) {
			if ("Content-Length".equals(pair.a)) {
				// skip
			} else {
				out.append(pair.a + ": " + pair.b + "\r\n");
			}
		}
		out.append("Content-Length: " + body.length() + "\r\n");
		out.append("\r\n");
		out.append(body);
		return out.toString();
	}

	public static void test() {
		String output = "{\"color\": \"green\",\"message\": \"Hello!\", \"message_format\": \"text\", \"notify\": false }";

		HttpResponse resp = new HttpResponse();
		resp.setStatusCode(HttpStatusCode.CODE_200);
		resp.addHeader("Content-Type", "application/json;charset=ISO-8859-1");
		resp.setBody(output);
		Log.debugNewlineChars(resp.toString());
	}
}
