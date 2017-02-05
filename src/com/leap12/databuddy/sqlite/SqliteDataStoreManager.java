package com.leap12.databuddy.sqlite;

import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.json.JSONArray;
import org.json.JSONObject;

import com.leap12.common.Coercer;
import com.leap12.common.Log;
import com.leap12.common.StrUtl;
import com.leap12.databuddy.data.DataStore;
import com.leap12.databuddy.data.DataStoreManager;
import com.leap12.databuddy.data.ResultSetJSonAdapter;
import com.leap12.databuddy.data.var.Type;
import com.leap12.databuddy.data.var.VarType;

public class SqliteDataStoreManager implements DataStoreManager {
	private static final String charEncoding = "UTF-8";
	private static final Charset CHARSET_UTF8 = Charset.forName( charEncoding );
	private static final int MAX_LIMIT = 50;

	private String mDbName = "dataBuddy.db";

	protected SqliteDataStoreManager( String dbName ) {
		if ( !StrUtl.isEmpty( dbName ) ) {
			mDbName = dbName;
		}
	}

	@Override
	public void startup() throws Exception {
	}

	@Override
	public void shutdown() throws Exception {
	}

	//
	//
	//
	// FIXME: need a way to have multiple connections get the same instance of the DataStore if the ShardKey is the same.
	// that way the lock on the datastore will prevent concurrent read writes.
	//
	//
	//

	private final Map<String, WeakReference<SqliteDataStore>> stores = new HashMap<>();

	@Override
	public DataStore attainDataStore( String shardKey ) throws Exception {
		SqliteDataStore store = null;
		WeakReference<SqliteDataStore> storeRef = stores.get( shardKey );
		if ( storeRef != null ) {
			store = storeRef.get();
		}

		if ( store == null ) {
			store = new SqliteDataStore();
			String pathToDbStr = String.format( "./db/%s/%s", shardKey, mDbName );
			Path pathToFile = Paths.get( pathToDbStr );
			if ( !Files.exists( pathToFile ) ) {
				Files.createDirectories( pathToFile.getParent() );
				Files.createFile( pathToFile );
			}
			store.openConnection( pathToDbStr );
			stores.put( shardKey, new WeakReference<>( store ) );
		}
		return store;
	}

	// @Override
	// public void releaseDataStore( DataStore store ) {
	// if ( store instanceof SqliteDataStore ) {
	// ( (SqliteDataStore) store ).closeConnection();
	// }
	// }

	private static final String toQuerySchema( String table ) {
		String format = ""
		        + "CREATE TABLE %s "
		        + "("
		        + "  idkey          TEXT PRIMARY KEY NOT NULL, "
		        + "  valtype        INT NOT NULL, "
		        + "  textval        TEXT, "
		        + "  blobval        BLOB, "
		        + "  intval         INT, "
		        + "  floatval       REAL "
		        + ")";
		return String.format( format, table );
	}

	private static final String toQueryMetaSchema( String table ) {
		String format = ""
		        + "CREATE TABLE %s "
		        + "("
		        + "  idkey          TEXT PRIMARY KEY NOT NULL, "
		        + "  "
		        + "  valtype        INT NOT NULL, "
		        + "  textval        TEXT, "
		        + "  blobval        BLOB, "
		        + "  intval         INT, "
		        + "  floatval       REAL "
		        + ")";
		return String.format( format, table );
	}

	private static final String toQueryInsertOrReplace( String table, String idKey, Type type, String strVal, byte[] byteVal, int intVal, float floatVal ) {
		String format = "INSERT OR REPLACE INTO %s "
		        + "(idkey,valtype,textval,blobval,intval,floatval) VALUES "
		        + "('%s', %s, '%s', '%s', %s, %s );";
		return String.format( format, table, idKey, type.getTypeId(), strVal, byteVal, intVal, floatVal );
	}

	private static final String toQueryInsertOrReplaceBind( String table ) {
		String format = "INSERT OR REPLACE INTO " + table + " "
		        + "(idkey,valtype,textval,blobval,intval,floatval) VALUES "
		        + "(?,?,?,?,?,?);";
		return format;
	}

	private static final String toQueryDelete( String table, String key ) {
		String format = "DELETE * FROM %s WHERE idkey='%s'";
		return String.format( format, table, key );
	}

	private static final String toQuerySelect( String table, String key ) {
		String format = "SELECT * FROM %s WHERE idkey='%s'";
		return String.format( format, table, key );
	}

	private static final String toQuerySelectMany( String table, Integer offset, Integer limit ) {
		offset = ( offset == null ) ? 0 : offset;
		limit = ( limit == null || limit >= MAX_LIMIT ) ? MAX_LIMIT : limit;
		String format = "SELECT * FROM %s ORDER BY idkey LIMIT %s OFFSET %s";
		return String.format( format, table, limit, offset );
	}





	/** Thread safe */
	public static class SqliteDataStore implements DataStore {
		private Connection connection;
		private final Set<String> knownTables; // TODO: maybe make this a shared resource for all connections? Beware major concurrency ClusterF
		private final Lock lock = new ReentrantLock();

		public SqliteDataStore() {
			knownTables = new HashSet<>();
		}

		@Override
		public void begin() {
			lock.lock();
		}

		@Override
		public void end() {
			lock.unlock();
		}

		@Override
		public void saveString( String topic, String subtopic, String key, String value ) throws Exception {
			String table = toTableName( topic, subtopic );
			insertOrReplace( table, key, value );
		}

		@Override
		public String loadString( String topic, String subtopic, String key ) throws Exception {
			String table = toTableName( topic, subtopic );
			VarType row = selectOne( table, key, Type.StringValue );
			if ( row != null ) {
				return row.textVal;
			}
			return null;
		}

		@Override
		public JSONArray loadArrayOfVals( String topic, String subtopic, Integer offset, Integer limit ) throws Exception {
			String table = toTableName( topic, subtopic );
			return selectArrayOfVals( table, offset, limit );
		}

		@Override
		public JSONArray loadArrayOfKeyVals( String topic, String subtopic, Integer offset, Integer limit ) throws Exception {
			String table = toTableName( topic, subtopic );
			return selectArrayOfKeyVals( table, offset, limit );
		}

		@Override
		public JSONObject loadMap( String topic, String subtopic, Integer offset, Integer limit ) throws Exception {
			String table = toTableName( topic, subtopic );
			return selectMap( table, offset, limit );
		}

		@Override
		public void saveBlob( String topic, String subtopic, String key, byte[] value ) throws Exception {
			String table = toTableName( topic, subtopic );
			insertOrReplace( table, Type.BlobValue, key, null, value, 0, 0f );
		}

		@Override
		public void saveBlobUtf8( String topic, String subtopic, String key, String value ) throws Exception {
			saveBlob( topic, subtopic, key, value.getBytes( CHARSET_UTF8 ) );
		}

		@Override
		public byte[] loadBlob( String topic, String subtopic, String key ) throws Exception {
			String table = toTableName( topic, subtopic );
			VarType row = selectOne( table, key, Type.BlobValue );
			if ( row != null ) {
				return row.blobVal;
			}
			return null;
		}

		@Override
		public String loadBlobUtf8( String topic, String subtopic, String key ) throws Exception {
			byte[] value = loadBlob( topic, subtopic, key );
			if ( value != null ) {
				return new String( value, CHARSET_UTF8 );
			}
			return null;
		}

		@Override
		public void saveInt( String topic, String subtopic, String key, int value ) throws Exception {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException( "Not Implemented" );
		}

		@Override
		public int loadInt( String topic, String subtopic, String key ) throws Exception {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException( "Not Implemented" );
		}

		@Override
		public void saveBoolean( String topic, String subtopic, String key, boolean value ) throws Exception {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException( "Not Implemented" );
		}

		@Override
		public boolean loadBoolean( String topic, String subtopic, String key ) throws Exception {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException( "Not Implemented" );
		}

		@Override
		public void saveFloat( String topic, String subtopic, String key, byte[] value ) throws Exception {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException( "Not Implemented" );
		}

		@Override
		public float loadFloat( String topic, String subtopic, String key ) throws Exception {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException( "Not Implemented" );
		}

		@Override
		public void saveJSONObject( String topic, String subtopic, String key, JSONObject value ) throws Exception {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException( "Not Implemented" );
		}

		@Override
		public JSONObject loadJSONObject( String topic, String subtopic, String key ) throws Exception {
			// TODO Auto-generated method stub
			throw new UnsupportedOperationException( "Not Implemented" );
		}

		private void openConnection( String dbFile ) {
			try {
				Class.forName( "org.sqlite.JDBC" );
				connection = DriverManager.getConnection( "jdbc:sqlite:" + dbFile );
				connection.setAutoCommit( true ); // we're only doing simple queries.
			} catch ( Exception e ) {
				Log.e( "Unable to open sqlite connection File: " + dbFile );
				throw new IllegalStateException( e );
			}
		}

		private void closeConnection() {
			try {
				if ( connection != null ) {
					// connection.rollback();
					connection.close();
					connection = null;
				}
			} catch ( Exception e ) {
				Log.e( e );
			}
		}

		private <T> T selectMany( String query, Coercer<ResultSet, T> coercer ) throws Exception {
			Statement stmt = null;
			ResultSet rs = null;
			T out = null;
			try {
				stmt = connection.createStatement();
				rs = stmt.executeQuery( query );
				// json = ResultSetJSonAdapter.toJsonArrayOfVals( rs );
				out = coercer.coerce( rs );
			} finally {
				if ( rs != null ) {
					rs.close();
				}
				if ( stmt != null ) {
					stmt.close();
				}
			}
			return out;
		}

		private JSONArray selectArrayOfVals( String table, Integer offset, Integer limit ) throws Exception {
			String query = SqliteDataStoreManager.toQuerySelectMany( table, offset, limit );
			return selectMany( query, ( v ) -> {
				try {
					return ResultSetJSonAdapter.toJsonArrayOfVals( v );
				} catch ( Exception e ) {
					Log.e( e );
					return new JSONArray();
				}
			} );
		}

		private JSONArray selectArrayOfKeyVals( String table, Integer offset, Integer limit ) throws Exception {
			String query = SqliteDataStoreManager.toQuerySelectMany( table, offset, limit );
			return selectMany( query, ( v ) -> {
				try {
					return ResultSetJSonAdapter.toJsonArrayOfKeyVals( v );
				} catch ( Exception e ) {
					Log.e( e );
					return new JSONArray();
				}
			} );
		}

		private JSONObject selectMap( String table, Integer offset, Integer limit ) throws Exception {
			String query = SqliteDataStoreManager.toQuerySelectMany( table, offset, limit );
			return selectMany( query, ( v ) -> {
				try {
					return ResultSetJSonAdapter.toJsonMap( v );
				} catch ( Exception e ) {
					Log.e( e );
					return new JSONObject();
				}
			} );
		}

		private JSONObject selectOne( String query ) throws Exception {
			Statement stmt = null;
			ResultSet rs = null;
			JSONObject json = null;
			try {
				stmt = connection.createStatement();
				rs = stmt.executeQuery( query );
				json = ResultSetJSonAdapter.toJsonOne( rs );
			} finally {
				if ( rs != null ) {
					rs.close();
				}
				if ( stmt != null ) {
					stmt.close();
				}
			}
			return json;
		}

		private VarType selectOne( String table, String key, Type type ) throws Exception {
			Statement stmt = null;
			ResultSet rs = null;
			VarType row = null;
			try {
				String query = SqliteDataStoreManager.toQuerySelect( table, key );
				stmt = connection.createStatement();
				rs = stmt.executeQuery( query );
				if ( rs.next() ) {
					row = VarType.fromResultSet( rs, type );
				}
			} finally {
				if ( rs != null ) {
					rs.close();
				}
				if ( stmt != null ) {
					stmt.close();
				}
			}
			return row;
		}


		// just synchronize the whole method, it should be very fast except the one time and everyone needs to wait for that one anyway
		private synchronized void ensureTable( String table ) throws Exception {
			Statement stmt = null;
			ResultSet rs = null;
			try {
				if ( !knownTables.contains( table ) ) {
					stmt = connection.createStatement();
					String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "'";
					rs = stmt.executeQuery( sql );
					if ( rs.next() && table.equals( rs.getString( "name" ) ) ) {
						rs.close();
						stmt.close();
						knownTables.add( table );
						return;
					}

					// I have confirmed that there is no limit to the number of tables in SQLite (other than physical storage space)
					stmt.executeUpdate( SqliteDataStoreManager.toQuerySchema( table ) );
					stmt.close();
					knownTables.add( table );
				}
			} finally {
				if ( rs != null && !rs.isClosed() ) {
					rs.close();
				}
				if ( stmt != null && !stmt.isClosed() ) {
					stmt.close();
				}
			}
		}

		private void update( String query ) throws Exception {
			Statement stmt = connection.createStatement();
			try {
				stmt.executeUpdate( query );
			} finally {
				stmt.close();
			}
		}

		// FIXME: Need to delete rows when value is null to reduce garbage
		// FIXME: Need to delete table when rows is zero to reduce garbage
		// FIXME: do it on insertion, or do a full scan periodically? insertion seems more efficient but more impactful to the user as we can do a full scan off peak
		// hours.
		private void insertOrReplace( String table, String key, String value ) throws Exception {
			ensureTable( table );
			String query = SqliteDataStoreManager.toQueryInsertOrReplace( table, key, Type.StringValue, value, null, 0, 0f );
			update( query );
		}

		private void insertOrReplace( String table, Type type, String key,
		        String textVal, byte[] byteVal, int intVal, float floatVal )
		        throws Exception {
			ensureTable( table );
			String query = SqliteDataStoreManager.toQueryInsertOrReplaceBind( table );
			PreparedStatement stmt = connection.prepareStatement( query );
			stmt.setString( 1, key );
			stmt.setInt( 2, type.getTypeId() );
			stmt.setString( 3, textVal );
			stmt.setBytes( 4, byteVal );
			stmt.setInt( 5, intVal );
			stmt.setFloat( 6, floatVal );
			stmt.executeUpdate( query );
			update( query );
		}

		private void delete( String table, String key ) throws Exception {
			String query = SqliteDataStoreManager.toQueryDelete( table, key );
			update( query );
		}

		// I realize dynamic table naming sounds dangerous but I have confirmed that there is no limit to the number of tables in SQLite (other than physical storage
		// space)
		// breaking a table name into topic and subtopic should help reduce number of rows per table
		// and make the data easier for human consumption when topics and subtopics are not user generated.
		private String toTableName( String topic, String subtopic ) {
			return topic + "_" + subtopic;
		}
	}
}
