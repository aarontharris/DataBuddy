package com.leap12.databuddy.aspects;

import com.leap12.common.http.HttpRequest;
import com.leap12.common.http.HttpResponse;
import com.leap12.common.http.HttpResponse.HttpException;
import com.leap12.databuddy.aspects.handlers.DbReadHandler;
import com.leap12.databuddy.aspects.handlers.DbWriteHandler;
import com.leap12.databuddy.aspects.handlers.EchoHandler;

import java.util.HashMap;
import java.util.Map;

public class BaseHttpHandler {

    private static final Map<String, Class<? extends BaseHttpHandler>> handlers = new HashMap<>();

    static {
        handlers.put("/echo", EchoHandler.class);
        handlers.put("/write", DbWriteHandler.class);
        handlers.put("/read", DbReadHandler.class);
        // handlers.put("/kot", KotlinHandler.class);
    }

    public static BaseHttpHandler getHandlerByPathName(String name) throws InstantiationException, IllegalAccessException {
        Class<? extends BaseHttpHandler> handlerClass = handlers.get(name);
        if (handlerClass != null) {
            return handlerClass.newInstance();
        }
        return null;
    }

    public void onReceivedHttpMsg(HttpRequest request, HttpResponse response) throws HttpException {
    }

}
