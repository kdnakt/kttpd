package com.kdnakt.kttpd

data class RequestContext(
        val method: HttpMethod,
        val requestTarget: String,
        val httpVersion: HttpVersion)

enum class HttpMethod {
    GET,
    POST,
}

enum class HttpVersion() {
    HTTP_0_9,
    HTTP_1_0,
    HTTP_1_1;

    companion object {
        fun from(version: String) = when(version) {
            "" -> HTTP_0_9
            "HTTP/1.0" -> HTTP_1_0
            "HTTP/1.1" -> HTTP_1_1
            else -> HTTP_1_1
        }
    }
}
