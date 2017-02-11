package com.leap12.databuddy.data;

import org.json.JSONObject;

public interface DataStore {

	public interface ReadDataStore {

		JSONObject selectMany( String query ) throws Exception;

		JSONObject selectOne( String query ) throws Exception;

	}



	public interface ReadWriteDataStore extends ReadDataStore {

		boolean ensureTable( String table, String query ) throws Exception;

		void update( String queryFormat , Object... args  ) throws Exception;

		JSONObject insertAndSelect( String table, String pkey, String insertQuery ) throws Exception;

	}



	public interface ReadLockExec<T> {
		public T exec( ReadDataStore db ) throws Exception;
	}



	public interface WriteLockExec<T> {
		public T exec( ReadWriteDataStore db ) throws Exception;
	}

	public <T> T read( ReadLockExec<T> run ) throws Exception;

	public <T> T readWrite( WriteLockExec<T> run ) throws Exception;

}
