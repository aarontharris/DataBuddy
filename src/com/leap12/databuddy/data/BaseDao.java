package com.leap12.databuddy.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class BaseDao {
	private final Gson gson;

	private BaseDao() {
		gson = new GsonBuilder().create();
	}

}
