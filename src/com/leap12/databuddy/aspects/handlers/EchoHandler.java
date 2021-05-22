package com.leap12.databuddy.aspects.handlers;

import com.leap12.common.http.HttpRequest;
import com.leap12.common.http.HttpResponse;
import com.leap12.common.http.HttpResponse.HttpException;
import com.leap12.common.http.annot.HttpGet;
import com.leap12.common.props.PropsRead;
import com.leap12.databuddy.aspects.BaseHttpHandler;

public class EchoHandler extends BaseHttpHandler {

    // This method is called when an appropriate URI match (below) is not found
    @Override
    public void onReceivedHttpMsg(HttpRequest request, HttpResponse response) throws HttpException {
        response.appendBody("echo echo");
    }

    //
    // These are case specific handlers
    // If multiple URIs match, the first one is used -- reflectively ordered by appearance in this file.
    // When there is a match, the handler is called, else onReceivedHttpMsg() is called
    //

    @HttpGet("/echo?query={str$query}")
    public void onEchoGetSimple(HttpRequest request, HttpResponse response, PropsRead params) {
        response.appendBody(String.format("Path='%s', Query='%s'",
                params.getString("pathVal1"),
                params.getString("query")));
    }

    @HttpGet("/echo")
    public void onEchoGet(HttpRequest request, HttpResponse response, PropsRead params) {
        response.appendBody(request.describe());
    }

    @HttpGet("/echo/{str$name}/and/{Str$attr}/search?query={Str$query}")
    public void onEchoGetComplex(HttpRequest request, HttpResponse response, PropsRead params) {
        response.appendBody(params.toString());
    }

}
