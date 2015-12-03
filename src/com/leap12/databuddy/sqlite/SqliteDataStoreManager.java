package com.leap12.databuddy.sqlite;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.leap12.common.Log;
import com.leap12.common.StrUtl;
import com.leap12.databuddy.data.DataStore;
import com.leap12.databuddy.data.DataStoreManager;
import com.leap12.databuddy.data.ResultSetJSonAdapter;

public class SqliteDataStoreManager implements DataStoreManager {
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

	@Override
	public DataStore attainDataStore( String shardKey ) throws Exception {
		SqliteDataStore store = new SqliteDataStore();
		String pathToDbStr = String.format( "./db/%s/dbBuddy.db", shardKey );
		Path pathToFile = Paths.get( pathToDbStr );
		if ( !Files.exists( pathToFile ) ) {
			Files.createDirectories( pathToFile.getParent() );
			Files.createFile( pathToFile );
		}
		store.openConnection( pathToDbStr );
		return store;
	}

	@Override
	public void releaseDataStore( DataStore store ) {
		if ( store instanceof SqliteDataStore ) {
			( (SqliteDataStore) store ).closeConnection();
		}
	}

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

	private static final String toQueryInsert( String table, String idKey, Type type, String strVal, byte[] byteVal, int intVal, float floatVal ) {
		String format = "INSERT OR REPLACE INTO %s "
				+ "(idkey,valtype,textval,blobval,intval,floatval) VALUES "
				+ "('%s', %s, '%s', '%s', %s, %s );";
		return String.format( format, table, idKey, type.getTypeId(), strVal, byteVal, intVal, floatVal );
	}

	private static final String toQueryInsertBind( String table ) {
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



	private enum Type {
		// Careful this order can never change without a data migration
		// as the ids will be stored in the database and may may the wrong value if changed.
		BooleanValue( 1, "intval", Boolean.class ),
		IntegerValue( 2, "intval", Integer.class ),
		FloatValue( 3, "floatval", Float.class ),
		StringValue( 4, "textval", String.class ),
		BlobValue( 5, "blobval", String.class ),
		JsonValue( 6, "textval", JsonObject.class );

		private static final Type[] idMap = new Type[] {
				// ZERO is invalid
				BooleanValue, // 1
				IntegerValue, // 2
				FloatValue, // 3
				StringValue, // 4
				BlobValue, // 5
				JsonValue, // 6
		};

		private int typeId;
		private String fieldName;
		private Class<?> type;

		Type( int typeId, String fieldName, Class<?> type ) {
			this.typeId = typeId;
			this.fieldName = fieldName;
			this.type = type;
		}

		public int getTypeId() {
			return typeId;
		}

		public String getFieldName() {
			return fieldName;
		}

		public Class<?> getType() {
			return type;
		}

		public static Type fromTypeId( int id ) {
			return idMap[id - 1]; // we do the -1 so we can do 1-5 instead of 0-4 because we dont want to use zeros in the database.
		}
	}



	private static class VarType {
		String key;
		int type;
		String textVal;
		byte[] blobVal;
		int intVal;
		float floatVal;
	}



	/** Thread safe */
	public static class SqliteDataStore implements DataStore {
		private Connection connection;
		private final Set<String> knownTables; // TODO: maybe make this a shared resource for all connections? Beware major concurrency ClusterF

		public SqliteDataStore() {
			knownTables = new HashSet<>();
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
		public void saveBlob( String topic, String subtopic, String key, byte[] value ) throws Exception {
			String table = toTableName( topic, subtopic );
			insertOrReplace( table, Type.BlobValue, key, null, value, 0, 0f );
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

		private JSONObject selectOne( String query ) throws Exception {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery( query );
			JSONObject json = ResultSetJSonAdapter.toJson( rs );
			rs.close();
			stmt.close();
			return json;
		}

		private JSONArray selectMany( String query ) throws Exception {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery( query );
			JSONArray json = ResultSetJSonAdapter.toJsonArray( rs );
			rs.close();
			stmt.close();
			return json;
		}

		private VarType selectOne( String table, String key, Type type ) throws Exception {
			String query = SqliteDataStoreManager.toQuerySelect( table, key );
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery( query );
			VarType row = null;
			if ( rs.next() ) {
				row = new VarType();
				row.type = rs.getInt( "valtype" );
				if ( type.getTypeId() == row.type ) {
					switch ( type ) {
					case BlobValue:
						row.blobVal = rs.getBytes( type.getFieldName() );
						break;
					case StringValue:
					case JsonValue:
						row.textVal = rs.getString( type.getFieldName() );
						break;
					case IntegerValue:
					case BooleanValue:
						row.intVal = rs.getInt( type.getFieldName() );
						break;
					case FloatValue:
						row.floatVal = rs.getFloat( type.getFieldName() );
						break;
					default:
						throw new UnsupportedOperationException( type + " not yet supported" );
					}
				} else {
					if ( row.type == 0 ) {
						throw new IllegalStateException( "Type mismatch, requested " + type + " but found no type" );
					} else {
						throw new IllegalStateException( "Type mismatch, requested " + type + " but found " + Type.fromTypeId( row.type ) );
					}
				}
			}
			rs.close();
			stmt.close();
			return row;
		}

		// just synchronize the whole method, it should be very fast except the one time and everyone needs to wait for that one anyway
		private synchronized void ensureTable( String table ) throws Exception {
			if ( !knownTables.contains( table ) ) {
				Statement stmt = connection.createStatement();
				String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "'";
				ResultSet rs = stmt.executeQuery( sql );
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
			String query = SqliteDataStoreManager.toQueryInsert( table, key, Type.StringValue, value, null, 0, 0f );
			update( query );
		}

		private void insertOrReplace( String table, Type type, String key,
				String textVal, byte[] byteVal, int intVal, float floatVal )
				throws Exception {
			ensureTable( table );
			String query = SqliteDataStoreManager.toQueryInsertBind( table );
			PreparedStatement stmt = connection.prepareStatement( query );
			stmt.setString( 1, key );
			stmt.setInt( 2, type.typeId );
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
