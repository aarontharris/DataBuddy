package com.leap12.databuddy.data;

public interface DataStore {

	public void saveString(String topic, String subtopic, String key, String value) throws Exception;

	public String loadString(String topic, String subtopic, String key) throws Exception;

}
