package com.leap12.databuddy.data;

import org.json.JSONObject;

public interface DataStore {

	public void saveString( String topic, String subtopic, String key, String value ) throws Exception;

	public String loadString( String topic, String subtopic, String key ) throws Exception;

	public void saveBlob( String topic, String subtopic, String key, byte[] value ) throws Exception;

	public byte[] loadBlob( String topic, String subtopic, String key ) throws Exception;

	public void saveInt( String topic, String subtopic, String key, int value ) throws Exception;

	public int loadInt( String topic, String subtopic, String key ) throws Exception;

	public void saveBoolean( String topic, String subtopic, String key, boolean value ) throws Exception;

	public boolean loadBoolean( String topic, String subtopic, String key ) throws Exception;

	public void saveFloat( String topic, String subtopic, String key, byte[] value ) throws Exception;

	public float loadFloat( String topic, String subtopic, String key ) throws Exception;

	public void saveJSONObject( String topic, String subtopic, String key, JSONObject value ) throws Exception;

	public JSONObject loadJSONObject( String topic, String subtopic, String key ) throws Exception;

}
