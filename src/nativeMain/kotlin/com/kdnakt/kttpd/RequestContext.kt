package com.kdnakt.kttpd

data class RequestContext(
        val method: HttpMethod = HttpMethod.GET,
        val requestTarget: String,
        val httpVersion: HttpVersion = HttpVersion.HTTP_1_1)

enum class HttpMethod {
    GET
}

enum class HttpVersion(val version: String) {
    HTTP_1_1("HTTP/1.1")
}
