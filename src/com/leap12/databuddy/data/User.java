package com.leap12.databuddy.data;

public class User {
	private String id;
	private String username;
	private String password;
	private String authToken;
	private String encryptionKey;

	public User() {
	}

	public User( String id, String username, String password ) {
		this.id = id;
		this.username = username;
		this.password = password;
	}

	public String getId() {
		return id;
	}

	public void setId( String id ) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername( String username ) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword( String password ) {
		this.password = password;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken( String authToken ) {
		this.authToken = authToken;
	}

	public String getEncryptionKey() {
		return encryptionKey;
	}

	public void setEncryptionKey( String encryptionKey ) {
		this.encryptionKey = encryptionKey;
	}

}
