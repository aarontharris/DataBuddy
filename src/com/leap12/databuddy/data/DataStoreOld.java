package com.leap12.databuddy.data;

import org.json.JSONArray;
import org.json.JSONObject;

import com.leap12.databuddy.data.BaseDao.InsideLock;


public interface DataStoreOld extends DataStore {

	public void begin();

	public void end();

	public void saveString( String topic, String subtopic, String key, String value ) throws Exception;

	public String loadString( String topic, String subtopic, String key ) throws Exception;

	public void saveBlob( String topic, String subtopic, String key, byte[] value ) throws Exception;

	public void saveBlobUtf8( String topic, String subtopic, String key, String value ) throws Exception;

	public byte[] loadBlob( String topic, String subtopic, String key ) throws Exception;

	public String loadBlobUtf8( String topic, String subtopic, String key ) throws Exception;

	public void saveInt( String topic, String subtopic, String key, int value ) throws Exception;

	public int loadInt( String topic, String subtopic, String key ) throws Exception;

	public void saveBoolean( String topic, String subtopic, String key, boolean value ) throws Exception;

	public boolean loadBoolean( String topic, String subtopic, String key ) throws Exception;

	public void saveFloat( String topic, String subtopic, String key, byte[] value ) throws Exception;

	public float loadFloat( String topic, String subtopic, String key ) throws Exception;

	public void saveJSONObject( String topic, String subtopic, String key, JSONObject value ) throws Exception;

	public JSONObject loadJSONObject( String topic, String subtopic, String key ) throws Exception;

	/** Fetch all records for this topic & subtopic as a JSONArray of record values */
	public JSONArray loadArrayOfVals( String topic, String subtopic, Integer offset, Integer limit ) throws Exception;

	/** Fetch all records for this topic & subtopic as a JSONArray of JSONObjects where each record key and value is an object */
	public JSONArray loadArrayOfKeyVals( String topic, String subtopic, Integer offset, Integer limit ) throws Exception;

	/** Fetch all records for this topic & subtopic as a single JSONObject where each record key maps to the record value */
	public JSONObject loadMap( String topic, String subtopic, Integer offset, Integer limit ) throws Exception;

	public boolean ensureTable( String table, String query ) throws Exception;

	public void update( String query ) throws Exception;

	public JSONObject select( String query ) throws Exception;

	public JSONObject selectOne( String query ) throws Exception;

	public void doInLock( InsideLock cmd ) throws Exception;
}
