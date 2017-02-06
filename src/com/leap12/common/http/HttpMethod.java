package com.leap12.common.http;

import java.lang.annotation.Annotation;

import com.leap12.common.http.annot.HttpGet;
import com.leap12.common.http.annot.HttpPost;


public enum HttpMethod {
	GET( HttpGet.class ), // Requests data from a specified resource
	POST( HttpPost.class ), // Submits data to be processed to a specified resource
	// HEAD, // Same as GET but returns only HTTP headers and no document body
	PUT( null ), // Uploads a representation of the specified URI
	DELETE( null ), // Deletes the specified resource
	OPTIONS( null ), // Returns the HTTP methods that the server supports
	// CONNECT // Converts the request connection to a transparent TCP/IP tunnel
	;

	private Class<? extends Annotation> annotationClass;

	HttpMethod( Class<? extends Annotation> annotationClass ) {
		this.annotationClass = annotationClass;
	}

	public Class<? extends Annotation> getAnnotationClass() {
		if ( this.annotationClass == null ) {
			throw new IllegalStateException( this.name() + " is not yet supported" );
		}
		return this.annotationClass;
	}

	public boolean isGet() {
		return GET.equals( this );
	}

	public boolean isPost() {
		return POST.equals( this );
	}

	public boolean isPut() {
		return PUT.equals( this );
	}

	public boolean isDelete() {
		return DELETE.equals( this );
	}

	public boolean isOptions() {
		return OPTIONS.equals( this );
	}
}
