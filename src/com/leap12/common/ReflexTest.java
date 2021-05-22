package com.leap12.common;

import com.leap12.common.http.HttpRequest;
import com.leap12.common.http.HttpResponse;
import com.leap12.common.http.annot.HttpGet;
import com.leap12.common.props.PropsRead;
import com.leap12.databuddy.aspects.DefaultHandshakeDelegate;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

class ReflexTest {

    private class TestDelegate extends DefaultHandshakeDelegate {

        @HttpGet("")
        private void onTestGet1(HttpRequest request, HttpResponse response, PropsRead params) {
            // no-op
        }

        private void onTestBlah(HttpRequest request, HttpResponse response, PropsRead params) {
            // no-op
        }
    }

    @Test
    public void testGetAllMethods() throws Exception {

        List<Method> allMethods = Reflex.getAllMethods(TestDelegate.class, DefaultHandshakeDelegate.class, HttpGet.class);
        for (Method m : allMethods) {
            Log.d("Method: " + m.getName());
        }

    }

}