package com.leap12.databuddy.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.leap12.common.NonNull;
import com.leap12.common.NotThreadSafe;

// see http://www.service-architecture.com/articles/database/mapping_sql_and_java_data_types.html

@NotThreadSafe
public class SqlRequest {
	private static class Binding {
		int sqlType;
		Object value;

		/** @param sqlType EX: {@link java.sql.Types#INTEGER} */
		public Binding( int sqlType, Object value ) {
			this.sqlType = sqlType;
			this.value = value;
		}
	}

	private final String query;
	private final List<Binding> bindings = new ArrayList<>();

	public SqlRequest( String queryFormat, Object... args ) {
		this.query = String.format( queryFormat, args );
	}

	public SqlRequest b( Boolean value ) {
		bindings.add( new Binding( Types.BOOLEAN, value ) );
		return this;
	}

	public SqlRequest i( Integer value ) {
		bindings.add( new Binding( Types.INTEGER, value ) );
		return this;
	}

	public SqlRequest l( Long value ) {
		bindings.add( new Binding( Types.BIGINT, value ) );
		return this;
	}

	public SqlRequest f( Float value ) {
		bindings.add( new Binding( Types.FLOAT, value ) );
		return this;
	}

	public SqlRequest d( Double value ) {
		bindings.add( new Binding( Types.DOUBLE, value ) );
		return this;
	}

	public SqlRequest s( String value ) {
		bindings.add( new Binding( Types.VARCHAR, value ) );
		return this;
	}

	public @NonNull Statement prepare( Connection connection ) throws SQLException {
		int index = 1; // the first parameter is index 0... why?

		Statement out = null;
		if ( bindings.size() > 0 ) {
			PreparedStatement stmt = connection.prepareStatement( getQuery() );
			out = stmt;

			for ( Binding b : bindings ) {
				if ( b.value == null ) {
					stmt.setNull( index, b.sqlType );
				} else {
					switch ( b.sqlType ) {
					case Types.BOOLEAN:
						stmt.setBoolean( index, (Boolean) b.value );
						break;
					case Types.INTEGER:
						stmt.setInt( index, (Integer) b.value );
						break;
					case Types.BIGINT:
						stmt.setLong( index, (Long) b.value );
						break;
					case Types.FLOAT:
						stmt.setFloat( index, (Float) b.value );
						break;
					case Types.DOUBLE:
						stmt.setDouble( index, (Double) b.value );
						break;
					case Types.VARCHAR:
						stmt.setString( index, (String) b.value );
						break;
					default:
						throw new SQLException( "Unsupported SqlType: " + b.sqlType );
					}
				}
				index += 1;
			}
		} else {
			out = connection.createStatement();
		}
		return out;
	}

	public String getQuery() {
		return query;
	}
}
