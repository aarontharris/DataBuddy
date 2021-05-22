package com.leap12.databuddy.aspects.handlers

import com.leap12.common.http.HttpRequest
import com.leap12.common.http.HttpResponse
import com.leap12.common.http.annot.HttpGet
import com.leap12.common.props.PropsRead
import com.leap12.databuddy.aspects.BaseHttpHandler

class KotlinHandler : BaseHttpHandler() {

    // public void onEchoGetSimple(HttpRequest request, HttpResponse response, PropsRead params) {

    @HttpGet("/kot")
    fun onKotlinGet(request: HttpRequest, response: HttpResponse, params: PropsRead) {
        response.appendBody("Yay!")
    }

}