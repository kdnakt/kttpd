package com.kdnakt.kttpd

import kotlinx.cinterop.toKString

class RequestParser {
    fun parse(byteArray: ByteArray): RequestContext {
        val startLineElements = byteArray.toKString()
                .split("\r\n")[0]
                .split(" ")
        return RequestContext(
                HttpMethod.valueOf(startLineElements[0]),
                startLineElements[1],
                HttpVersion.HTTP_1_1
        )
    }
}