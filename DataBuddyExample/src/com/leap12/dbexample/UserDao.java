package com.leap12.dbexample;

import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.leap12.common.Log;
import com.leap12.databuddy.data.DataStore;
import com.leap12.databuddy.data.SqlRequest;
import com.leap12.databuddy.sqlite.SqliteDataStoreManager;
import com.leap12.databuddy.sqlite.SqliteShardKey;

public class UserDao implements Dao {

	public static final Gson vanilla = new GsonBuilder().create();

	private final SqliteShardKey userKey = new TopicShardKey( "users" );
	private DataStore dataStore;

	public UserDao() {
		try {
			dataStore = SqliteDataStoreManager.get().attainDataStore( userKey, 1000 );
		} catch ( Exception e ) {
			throw new IllegalStateException( e );
		}
	}

	@Override
	public void ensureTables() throws Exception {
		dataStore.readWrite( ( db ) -> {
			String table = null;
			if ( ( null != ( table = "user" ) ) && db.ensureTable( table, toUserTable() ) ) {
				Log.d( "Created %s", table );
			}
			return null;
		} );

		// test();
	}



	public class User {
		private Integer id;
		private String username;
		private String nickname;
		private String firstname;
		private String lastname;
		private String email;
		private String creds;

		private User() {
		}

		private User( String username, String creds ) {
			this.username = username;
			this.creds = creds;
		}

		public Integer getId() {
			return id;
		}

		private void setId( int id ) {
			this.id = id;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername( String username ) {
			this.username = username;
		}

		public String getNickname() {
			return nickname;
		}

		public void setNickname( String nickname ) {
			this.nickname = nickname;
		}

		public String getFirstname() {
			return firstname;
		}

		public void setFirstname( String firstname ) {
			this.firstname = firstname;
		}

		public String getLastname() {
			return lastname;
		}

		public void setLastname( String lastname ) {
			this.lastname = lastname;
		}

		public String getEmail() {
			return email;
		}

		public void setEmail( String email ) {
			this.email = email;
		}

		public String getCreds() {
			return creds;
		}

		public void setCreds( String creds ) {
			this.creds = creds;
		}
	}

	private static final String toUserTable() {
		String sql = ""
		        + "CREATE TABLE user "
		        + "("
		        + "  id             INTEGER PRIMARY KEY NOT NULL, "
		        + "  username	    TEXT UNIQUE NOT NULL,"
		        + "  nickname	    TEXT,"
		        + "  firstname	    TEXT,"
		        + "  lastname	    TEXT,"
		        + "  email		    TEXT,"
		        + "  creds		    TEXT NOT NULL "
		        + ")";
		return sql;
	}

	private static SqlRequest toUserInsert( User user ) {
		String sql = ""
		        + "INSERT INTO user "
		        + "( username, nickname, firstname, lastname, email, creds ) "
		        + "VALUES "
		        + "( ?, ?, ?, ?, ?, ? ) ";
		SqlRequest req = new SqlRequest( sql )
		        .s( user.username )
		        .s( user.nickname )
		        .s( user.firstname )
		        .s( user.lastname )
		        .s( user.email )
		        .s( user.creds );
		return req;
	}

	private static final SqlRequest toUserUpdate( User user ) {
		String sql = ""
		        + "UPDATE user "
		        + "SET username=?, nickname=?, firstname=?, lastname=?, email=?, creds=? "
		        + "WHERE id=? ";
		// + "( ?, ?, ?, ?, ?, ? ) ";
		SqlRequest req = new SqlRequest( sql )
		        .s( user.username )
		        .s( user.nickname )
		        .s( user.firstname )
		        .s( user.lastname )
		        .s( user.email )
		        .s( user.creds )
		        .i( user.id );
		return req;
	}

	private void test() {
		try {
			// Create user
			Log.d( "Create User" );
			User user = new User( "Aaron" + System.currentTimeMillis(), "creds" );
			writeUser( user );

			// verify json
			Log.d( "Verify json" );
			String json = vanilla.toJson( user );
			Log.d( "User: %s", json );

			// find user, verify and compare json
			Log.d( "find user" );
			User found = findUser( "Aaron" );
			String foundJson = vanilla.toJson( user );
			Log.d( "User: %s", foundJson );
			if ( json != null && json.equals( foundJson ) ) {
				Log.d( "Match" );
			} else {
				Log.e( "Not Match" );
			}

			// update user
			Log.d( "update user" );
			user.setCreds( "blah" );
			writeUser( user );

			// verify change
			Log.d( "verify update" );
			found = findUser( "Aaron" );
			foundJson = vanilla.toJson( user );
			Log.d( "User: %s", foundJson );
			if ( found.creds.equals( "blah" ) ) {
				Log.d( "Update Success" );
			} else {
				Log.e( "Update Fail" );
			}

		} catch ( Exception e ) {
			Log.e( e );
		}
	}

	public JSONObject findUserJson( String username ) throws Exception {
		return dataStore.read( db -> db.selectOne( new SqlRequest( "select * from user where username=?" ).s( username ) ) );
	}

	public User findUser( String username ) throws Exception {
		JSONObject json = findUserJson( username );
		if ( json != null ) {
			User user = vanilla.fromJson( json.toString(), User.class );
			return user;
		}
		return null;
	}

	/**
	 * Insert or Update a user record.<br>
	 * If the user.id is null, then insert is assumed.<br>
	 * if the user.id is not null, then update is assmumed.<br>
	 * 
	 * @param user
	 * @throws Exception
	 */
	public void writeUser( User user ) throws Exception {
		if ( user.getId() == null ) {
			JSONObject json = dataStore.readWrite( db -> db.insertAndSelect( "user", "id", toUserInsert( user ) ) );
			user.setId( json.getInt( "id" ) );
		} else {
			dataStore.readWrite( db -> {
				db.update( toUserUpdate( user ) );
				return null;
			} );
		}
	}
}
