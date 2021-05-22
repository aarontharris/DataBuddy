package com.leap12.databuddy.aspects.handlers;

import com.leap12.common.http.HttpRequest;
import com.leap12.common.http.HttpResponse;
import com.leap12.common.http.annot.HttpGet;
import com.leap12.common.props.PropsRead;
import com.leap12.databuddy.aspects.BaseHttpHandler;
import com.leap12.databuddy.data.store.KeyValStore;

public class DbReadHandler extends BaseHttpHandler {
    @HttpGet("/read?topic={str$topic}&subtopic={str$subtopic}&key={str$key}")
    public void onReadGet(HttpRequest request, HttpResponse response, PropsRead params) throws Exception {
        KeyValStore store = new KeyValStore();
        String topic = params.getString("topic");
        String subtopic = params.getString("subtopic");
        String key = params.getString("key");
        String result = store.loadString(topic, subtopic, key);
        response.appendBody(result);
    }
}
