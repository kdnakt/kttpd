package com.kdnakt.kttpd.handler

import com.kdnakt.kttpd.OkResponse
import com.kdnakt.kttpd.RequestContext
import com.kdnakt.kttpd.Response

class PostHandler(
        val contentType: String,
        val body: String): RequestHandler() {
    override fun handleImpl(req: RequestContext): Pair<String, Response> {
        val res = "Hello, ${body.split("=")[1]} san!"
        return Pair(res, OkResponse())
    }
}