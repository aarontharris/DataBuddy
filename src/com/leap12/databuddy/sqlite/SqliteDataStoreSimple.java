package com.leap12.databuddy.sqlite;

import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.JSONArray;
import org.json.JSONObject;

import com.leap12.common.Coercer;
import com.leap12.common.Log;
import com.leap12.databuddy.data.ResultSetJSonAdapter;
import com.leap12.databuddy.data.var.Type;
import com.leap12.databuddy.data.var.VarType;


public class SqliteDataStoreSimple extends SqliteDataStore {
	public static final String charEncoding = SqliteDataStoreManager.charEncoding;
	public static final Charset CHARSET_UTF8 = SqliteDataStoreManager.CHARSET_UTF8;
	public static final int MAX_LIMIT = 50;

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

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private ResultSet executeQuery;

	public void begin() {
		lock.writeLock().lock();
	}

	public void end() {
		lock.writeLock().unlock();
	}

	public void saveString( String topic, String subtopic, String key, String value ) throws Exception {
		String table = toTableName( topic, subtopic );
		insertOrReplace( table, key, value );
	}

	public String loadString( String topic, String subtopic, String key ) throws Exception {
		String table = toTableName( topic, subtopic );
		VarType row = selectOne( table, key, Type.StringValue );
		if ( row != null ) {
			return row.textVal;
		}
		return null;
	}

	public JSONArray loadArrayOfVals( String topic, String subtopic, Integer offset, Integer limit ) throws Exception {
		String table = toTableName( topic, subtopic );
		return selectArrayOfVals( table, offset, limit );
	}

	public JSONArray loadArrayOfKeyVals( String topic, String subtopic, Integer offset, Integer limit ) throws Exception {
		String table = toTableName( topic, subtopic );
		return selectArrayOfKeyVals( table, offset, limit );
	}

	public JSONObject loadMap( String topic, String subtopic, Integer offset, Integer limit ) throws Exception {
		String table = toTableName( topic, subtopic );
		return selectMap( table, offset, limit );
	}

	public void saveBlob( String topic, String subtopic, String key, byte[] value ) throws Exception {
		String table = toTableName( topic, subtopic );
		insertOrReplace( table, Type.BlobValue, key, null, value, 0, 0f );
	}

	public void saveBlobUtf8( String topic, String subtopic, String key, String value ) throws Exception {
		saveBlob( topic, subtopic, key, value.getBytes( CHARSET_UTF8 ) );
	}

	public byte[] loadBlob( String topic, String subtopic, String key ) throws Exception {
		String table = toTableName( topic, subtopic );
		VarType row = selectOne( table, key, Type.BlobValue );
		if ( row != null ) {
			return row.blobVal;
		}
		return null;
	}

	public String loadBlobUtf8( String topic, String subtopic, String key ) throws Exception {
		byte[] value = loadBlob( topic, subtopic, key );
		if ( value != null ) {
			return new String( value, CHARSET_UTF8 );
		}
		return null;
	}

	public void saveInt( String topic, String subtopic, String key, int value ) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException( "Not Implemented" );
	}

	public int loadInt( String topic, String subtopic, String key ) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException( "Not Implemented" );
	}

	public void saveBoolean( String topic, String subtopic, String key, boolean value ) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException( "Not Implemented" );
	}

	public boolean loadBoolean( String topic, String subtopic, String key ) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException( "Not Implemented" );
	}

	public void saveFloat( String topic, String subtopic, String key, byte[] value ) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException( "Not Implemented" );
	}

	public float loadFloat( String topic, String subtopic, String key ) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException( "Not Implemented" );
	}

	public void saveJSONObject( String topic, String subtopic, String key, JSONObject value ) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException( "Not Implemented" );
	}

	public JSONObject loadJSONObject( String topic, String subtopic, String key ) throws Exception {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException( "Not Implemented" );
	}

	private <T> T selectMany( String query, Coercer<ResultSet, T> coercer ) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		T out = null;
		try {
			stmt = getConnection().createStatement();
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
		String query = toQuerySelectMany( table, offset, limit );
		return selectMany( query, ( v ) -> {
			try {
				return ResultSetJSonAdapter.varToJsonArrayOfVals( v );
			} catch ( Exception e ) {
				Log.e( e );
				return new JSONArray();
			}
		} );
	}

	private JSONArray selectArrayOfKeyVals( String table, Integer offset, Integer limit ) throws Exception {
		String query = toQuerySelectMany( table, offset, limit );
		return selectMany( query, ( v ) -> {
			try {
				return ResultSetJSonAdapter.varToJsonArrayOfKeyVals( v );
			} catch ( Exception e ) {
				Log.e( e );
				return new JSONArray();
			}
		} );
	}

	private JSONObject selectMap( String table, Integer offset, Integer limit ) throws Exception {
		String query = toQuerySelectMany( table, offset, limit );
		return selectMany( query, ( v ) -> {
			try {
				return ResultSetJSonAdapter.varToJsonMap( v );
			} catch ( Exception e ) {
				Log.e( e );
				return new JSONObject();
			}
		} );
	}

	private VarType selectOne( String table, String key, Type type ) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		VarType row = null;
		try {
			String query = toQuerySelect( table, key );
			stmt = getConnection().createStatement();
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
		ensureTable( table, toQuerySchema( table ) );
	}


	// FIXME: Need to delete rows when value is null to reduce garbage
	// FIXME: Need to delete table when rows is zero to reduce garbage
	// FIXME: do it on insertion, or do a full scan periodically? insertion seems more efficient but more impactful to the user as we can do a full scan off peak
	// hours.
	private void insertOrReplace( String table, String key, String value ) throws Exception {
		ensureTable( table );
		String query = toQueryInsertOrReplace( table, key, Type.StringValue, value, null, 0, 0f );
		update( query, null );
	}

	private void insertOrReplace( String table, Type type, String key,
	        String textVal, byte[] byteVal, int intVal, float floatVal )
	        throws Exception {
		ensureTable( table );
		String query = toQueryInsertOrReplaceBind( table );
		PreparedStatement stmt = getConnection().prepareStatement( query );
		stmt.setString( 1, key );
		stmt.setInt( 2, type.getTypeId() );
		stmt.setString( 3, textVal );
		stmt.setBytes( 4, byteVal );
		stmt.setInt( 5, intVal );
		stmt.setFloat( 6, floatVal );
		stmt.executeUpdate( query );
		update( query, null );
	}

	private void delete( String table, String key ) throws Exception {
		String query = toQueryDelete( table, key );
		update( query, null );
	}

	// I realize dynamic table naming sounds dangerous but I have confirmed that there is no limit to the number of tables in SQLite (other than physical storage
	// space)
	// breaking a table name into topic and subtopic should help reduce number of rows per table
	// and make the data easier for human consumption when topics and subtopics are not user generated.
	private String toTableName( String topic, String subtopic ) {
		return topic + "_" + subtopic;
	}

}
