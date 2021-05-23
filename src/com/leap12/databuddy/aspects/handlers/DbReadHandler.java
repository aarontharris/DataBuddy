package com.leap12.databuddy.aspects.handlers;

import com.leap12.common.Log;
import com.leap12.common.http.HttpRequest;
import com.leap12.common.http.HttpResponse;
import com.leap12.common.http.annot.HttpGet;
import com.leap12.common.props.PropsRead;
import com.leap12.databuddy.aspects.BaseHttpHandler;
import com.leap12.databuddy.data.store.KeyValStore;
import org.json.JSONArray;

public class DbReadHandler extends BaseHttpHandler {
    @HttpGet("/read?topic={str$topic}&subtopic={str$subtopic}&key={str$key}")
    public void onReadOneGet(HttpRequest request, HttpResponse response, PropsRead params) throws Exception {
        KeyValStore store = new KeyValStore();
        String topic = params.getString("topic");
        String subtopic = params.getString("subtopic");
        String key = params.getString("key");
        String result = store.loadString(topic, subtopic, key);
        response.appendBody(result);
    }

    @HttpGet("/read?topic={str$topic}&subtopic={str$subtopic}&all={bool$all}")
    public void onReadAllGet(HttpRequest request, HttpResponse response, PropsRead params) throws Exception {
        KeyValStore store = new KeyValStore();
        String topic = params.getString("topic");
        String subtopic = params.getString("subtopic");
        String all = params.getString("all");
        Log.d("Params: " + all);
        JSONArray result = store.loadAll(topic, subtopic);
        response.appendBody(result.toString());
        //throw new IllegalStateException("Must be all");
    }
}
