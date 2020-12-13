package com.kdnakt.kttpd

import kotlinx.cinterop.toKString

class RequestParser {
    fun parse(byteArray: ByteArray): RequestContext {
        val startLineElements = byteArray.toKString()
                .split("\r\n")[0]
                .split(" ")
        val httpMethod = HttpMethod.values()
                .firstOrNull { it.name == startLineElements[0] }
                ?: throw BadRequestException()
        val httpVersion = if (startLineElements.size <= 2)
                HttpVersion.HTTP_0_9 else HttpVersion.from(startLineElements[2])
        return RequestContext(httpMethod,
                startLineElements[1],
                httpVersion
        )
    }
}

abstract class HttpException(val status: Int, val reason: String): RuntimeException()

class BadRequestException(): HttpException(400, "Bad Request")