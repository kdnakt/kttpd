package com.kdnakt.kttpd

import kotlinx.cinterop.toKString

class RequestParser {
    fun parse(byteArray: ByteArray): RequestContext {
        val startLineElements = byteArray.toKString()
                .split("\r\n")[0]
                .split(" ")
        val httpVersion = if (startLineElements.size <= 2)
                "" else startLineElements[2]
        return RequestContext(
                HttpMethod.valueOf(startLineElements[0]),
                startLineElements[1],
                HttpVersion.from(httpVersion)
        )
    }
}