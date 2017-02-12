package com.leap12.databuddy.data;

import org.json.JSONObject;

import com.leap12.common.NonNull;

public interface DataStore {

	public interface ReadDataStore {

		JSONObject selectMany( @NonNull SqlRequest req ) throws Exception;

		JSONObject selectOne( @NonNull SqlRequest req ) throws Exception;

	}



	public interface ReadWriteDataStore extends ReadDataStore {

		boolean ensureTable( @NonNull String table, @NonNull String query ) throws Exception;

		void update( @NonNull SqlRequest req ) throws Exception;

		JSONObject insertAndSelect( @NonNull String table, @NonNull String pkey, @NonNull SqlRequest req ) throws Exception;

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
