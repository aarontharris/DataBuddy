package com.leap12.databuddy.data;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import org.json.JSONArray;
import org.json.JSONObject;

public final class ResultSetJSonAdapter {

	private ResultSetJSonAdapter() {
	}

	/**
	 * When you expect a single record
	 * 
	 * @return null when no record.
	 **/
	public static final JSONObject toJson( ResultSet rs ) throws Exception {
		if ( rs.next() ) {
			return toJson( rs, rs.getMetaData(), rs.getMetaData().getColumnCount() );
		}
		return null;
	}

	/**
	 * When you expect a many records
	 * 
	 * @return never null, just an empty array
	 */
	public static final JSONArray toJsonArray( ResultSet rs ) throws Exception {
		JSONArray json = new JSONArray();
		ResultSetMetaData meta = rs.getMetaData();
		int columnCount = meta.getColumnCount();

		while ( rs.next() ) {
			json.put( toJson( rs, meta, columnCount ) );
		}

		return json;
	}

	private static final JSONObject toJson( ResultSet rs, ResultSetMetaData meta, int columnCount ) throws Exception {
		JSONObject jsonObject = new JSONObject();

		for ( int i = 1; i < columnCount + 1; i++ ) {
			String columnName = meta.getColumnName( i );
			int type = meta.getColumnType( i );

			switch ( type ) {
			case java.sql.Types.ARRAY:
				jsonObject.put( columnName, rs.getArray( columnName ) );
				break;
			case java.sql.Types.BIGINT:
				jsonObject.put( columnName, rs.getInt( columnName ) );
				break;
			case java.sql.Types.BOOLEAN:
				jsonObject.put( columnName, rs.getBoolean( columnName ) );
				break;
			case java.sql.Types.BLOB:
				jsonObject.put( columnName, rs.getBlob( columnName ) );
				break;
			case java.sql.Types.DOUBLE:
				jsonObject.put( columnName, rs.getDouble( columnName ) );
				break;
			case java.sql.Types.FLOAT:
				jsonObject.put( columnName, rs.getFloat( columnName ) );
				break;
			case java.sql.Types.INTEGER:
				jsonObject.put( columnName, rs.getInt( columnName ) );
				break;
			case java.sql.Types.NVARCHAR:
				jsonObject.put( columnName, rs.getNString( columnName ) );
				break;
			case java.sql.Types.VARCHAR:
				jsonObject.put( columnName, rs.getString( columnName ) );
				break;
			case java.sql.Types.TINYINT:
				jsonObject.put( columnName, rs.getInt( columnName ) );
				break;
			case java.sql.Types.SMALLINT:
				jsonObject.put( columnName, rs.getInt( columnName ) );
				break;
			case java.sql.Types.DATE:
				jsonObject.put( columnName, rs.getDate( columnName ) );
				break;
			case java.sql.Types.TIMESTAMP:
				jsonObject.put( columnName, rs.getTimestamp( columnName ) );
				break;
			default:
				jsonObject.put( columnName, rs.getObject( columnName ) );
				break;
			}
		}

		return jsonObject;
	}
}
