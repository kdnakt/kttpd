package com.kdnakt.kttpd

import kotlinx.cinterop.toKString

fun parse(byteArray: ByteArray): RequestContext {
    return parse(byteArray.toKString())
}

fun parse(reqString: String): RequestContext {
    val startLineElements = reqString
            .split("\r\n")[0]
            .split(" ")
    val httpMethod = HttpMethod.values()
            .firstOrNull { it.name == startLineElements[0] }
            ?: throw BadRequestException()
    val httpVersion = if (startLineElements.size <= 2)
            HttpVersion.HTTP_0_9 else HttpVersion.from(startLineElements[2])
    val req = RequestContext(httpMethod,
            startLineElements[1],
            httpVersion
    )
    reqString.split("\r\n\r\n")[0]
            .split("\r\n")
            .let { it.slice(1 until it.size) }
            .map { headerString ->
                val indexOfFirstColon = headerString.indexOfFirst { it == ':' }
                req.headers.put(
                        headerString.substring(0 until indexOfFirstColon),
                        headerString.substring(indexOfFirstColon + 1).trim())
            }
    return req
}

abstract class HttpException(val status: Int, val reason: String): RuntimeException()

class BadRequestException(): HttpException(400, "Bad Request")