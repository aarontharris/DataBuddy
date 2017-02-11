package com.leap12.databuddy.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.json.JSONObject;

import com.leap12.common.Log;
import com.leap12.common.NotThreadSafe;
import com.leap12.databuddy.data.DataStore;
import com.leap12.databuddy.data.DataStore.ReadWriteDataStore;
import com.leap12.databuddy.data.ResultSetJSonAdapter;

public class SqliteDataStore implements DataStore, ReadWriteDataStore {
	private final Set<String> knownTables; // TODO: maybe make this a shared resource for all connections? Beware major concurrency ClusterF

	@NotThreadSafe
	protected void addKnownTable( String table ) {
		knownTables.add( table );
	}

	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private Connection connection;

	public SqliteDataStore() {
		knownTables = new HashSet<>();
	}

	void openConnection( String dbFile ) {
		try {
			Class.forName( "org.sqlite.JDBC" );
			connection = DriverManager.getConnection( "jdbc:sqlite:" + dbFile );
			connection.setAutoCommit( true ); // we're only doing simple queries.
		} catch ( Exception e ) {
			Log.e( "Unable to open sqlite connection File: " + dbFile );
			throw new IllegalStateException( e );
		}
	}

	void closeConnection() {
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

	protected Connection getConnection() {
		return connection;
	}

	@Override
	public <T> T read( ReadLockExec<T> run ) throws Exception {
		lock.readLock().lock();
		try {
			return run.exec( SqliteDataStore.this );
		} finally {
			lock.readLock().unlock();
		}
	}

	@Override
	public <T> T readWrite( WriteLockExec<T> run ) throws Exception {
		lock.writeLock().lock();
		try {
			return run.exec( SqliteDataStore.this );
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Best practice is to ensure your tables during startup, not while open to users.
	 * 
	 * @param table name of the table to create - used to check if it already exists.
	 * @param query
	 * @throws Exception
	 */
	@Override
	public boolean ensureTable( String table, String query ) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			if ( !knownTables.contains( table ) ) {
				stmt = connection.createStatement();
				String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + table + "'";
				rs = stmt.executeQuery( sql );
				if ( rs.next() ) {
					String found = rs.getString( "name" );
					if ( table.equals( found ) ) {
						rs.close();
						stmt.close();
						knownTables.add( table );
						return false;
					}
				}

				// I have confirmed that there is no limit to the number of tables in SQLite (other than physical storage space)
				stmt.executeUpdate( query );
				stmt.close();
				knownTables.add( table );
				return true;
			}
		} catch ( Exception e ) {
			throw new IllegalStateException( "Could not create table " + table, e );
		} finally {
			if ( rs != null && !rs.isClosed() ) {
				rs.close();
			}
			if ( stmt != null && !stmt.isClosed() ) {
				stmt.close();
			}
		}
		return false;
	}

	@Override
	public void update( String queryFormat, Object... args ) throws Exception {
		Statement stmt = connection.createStatement();
		try {
			stmt.executeUpdate( String.format( queryFormat, args ) );
		} finally {
			stmt.close();
		}
	}

	@Override
	public JSONObject insertAndSelect( String table, String pkey, String insertQuery ) throws Exception {
		update( insertQuery );
		JSONObject object = selectOne( String.format( "select * from %s where %s in ( select max(%s) from %s )", table, pkey, pkey, table ) );
		return object;
	}

	@Override
	public JSONObject selectMany( String query ) throws Exception {
		Statement stmt = null;
		ResultSet rs = null;
		JSONObject json = null;
		try {
			stmt = connection.createStatement();
			rs = stmt.executeQuery( query );
			json = ResultSetJSonAdapter.toJson( rs );
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

	@Override
	public JSONObject selectOne( String query ) throws Exception {
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
}
