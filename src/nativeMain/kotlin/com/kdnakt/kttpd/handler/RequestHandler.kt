package com.kdnakt.kttpd.handler

import com.kdnakt.kttpd.HttpVersion
import com.kdnakt.kttpd.RequestContext
import com.kdnakt.kttpd.Response

abstract class RequestHandler {
    fun handle(req: RequestContext): String {
        val (content, res) = handleImpl(req)

        return when (req.httpVersion) {
            HttpVersion.HTTP_0_9 -> content
            HttpVersion.HTTP_1_0,
            HttpVersion.HTTP_1_1 ->
                "${req.httpVersion.version} ${res.status} ${res.reason}\r\n"
                        .plus("Content-Length: ${content.length}\r\n")
                        .plus("\r\n").plus("${content}\r\n")
        }
    }

    protected abstract fun handleImpl(req: RequestContext): Pair<String, Response>
}