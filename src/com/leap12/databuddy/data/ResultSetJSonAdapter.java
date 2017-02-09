package com.leap12.databuddy.data;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;

import org.json.JSONArray;
import org.json.JSONObject;

import com.leap12.databuddy.data.var.VarType;

public final class ResultSetJSonAdapter {

	private ResultSetJSonAdapter() {
	}

	/**
	 * When you expect a single record
	 * 
	 * @return null when no record.
	 **/
	public static final JSONObject varToJsonOne( ResultSet rs ) throws Exception {
		if ( rs.next() ) {
			return VarType.fromResultSet( rs, null ).toJsonObject();
		}
		return null;
	}

	/**
	 * @return never null, just an empty array
	 */
	public static final JSONArray varToJsonArrayOfKeyVals( ResultSet rs ) throws Exception {
		JSONArray json = new JSONArray();
		while ( rs.next() ) {
			json.put( VarType.fromResultSet( rs, null ).toJsonObject() );
		}
		return json;
	}

	/**
	 * @return never null, just an empty array
	 */
	public static final JSONArray varToJsonArrayOfVals( ResultSet rs ) throws Exception {
		JSONArray json = new JSONArray();
		while ( rs.next() ) {
			VarType.fromResultSet( rs, null ).toJsonArray( json );
		}
		return json;
	}

	/**
	 * When you expect a many records and want them collapsed into a map
	 * 
	 * @return never null, just an empty object
	 */
	public static final JSONObject varToJsonMap( ResultSet rs ) throws Exception {
		JSONObject json = new JSONObject();
		while ( rs.next() ) {
			VarType.fromResultSet( rs, null ).toJsonObject( json );
		}
		return json;
	}

	public static final JSONObject toJsonOne( ResultSet rs ) throws Exception {
		JSONObject json = toJson( rs );
		if ( json.has( "rows" ) ) {
			JSONArray array = json.getJSONArray( "rows" );
			if ( array.length() >= 1 ) {
				return array.getJSONObject( 0 );
			}
		}
		return json;
	}

	public static final JSONObject toJson( ResultSet rs ) throws Exception {
		JSONObject out = new JSONObject();

		ResultSetMetaData meta = rs.getMetaData();
		int columnCount = meta.getColumnCount();

		while ( rs.next() ) {
			JSONObject jsonRow = new JSONObject();
			for ( int i = 1; i < columnCount + 1; i++ ) {
				int columnIndex = i;
				String columnName = meta.getColumnName( columnIndex );
				int type = meta.getColumnType( i );

				switch ( type ) {
				case java.sql.Types.ARRAY:
					jsonRow.put( columnName, rs.getArray( columnIndex ) );
					break;
				case java.sql.Types.BIGINT:
					jsonRow.put( columnName, rs.getInt( columnIndex ) );
					break;
				case java.sql.Types.BOOLEAN:
					jsonRow.put( columnName, rs.getBoolean( columnIndex ) );
					break;
				case java.sql.Types.BLOB:
					jsonRow.put( columnName, rs.getBlob( columnIndex ) );
					break;
				case java.sql.Types.DOUBLE:
					jsonRow.put( columnName, rs.getDouble( columnIndex ) );
					break;
				case java.sql.Types.FLOAT:
					jsonRow.put( columnName, rs.getFloat( columnIndex ) );
					break;
				case java.sql.Types.INTEGER:
					jsonRow.put( columnName, rs.getInt( columnIndex ) );
					break;
				case java.sql.Types.NVARCHAR:
					jsonRow.put( columnName, rs.getNString( columnIndex ) );
					break;
				case java.sql.Types.VARCHAR:
					jsonRow.put( columnName, rs.getString( columnIndex ) );
					break;
				case java.sql.Types.TINYINT:
					jsonRow.put( columnName, rs.getInt( columnIndex ) );
					break;
				case java.sql.Types.SMALLINT:
					jsonRow.put( columnName, rs.getInt( columnIndex ) );
					break;
				case java.sql.Types.DATE:
					jsonRow.put( columnName, rs.getDate( columnIndex ) );
					break;
				case java.sql.Types.TIMESTAMP:
					jsonRow.put( columnName, rs.getTimestamp( columnIndex ) );
					break;
				default:
					jsonRow.put( columnName, rs.getObject( columnIndex ) );
					break;
				}
			}
			out.append( "rows", jsonRow );
		}

		return out;
	}
}
