package com.kdnakt.kttpd

data class RequestContext(
        val method: HttpMethod,
        val requestTarget: String,
        val httpVersion: HttpVersion) {
    val headers = mutableMapOf<String, String>()
}

enum class HttpMethod {
    GET,
    POST,
}

enum class HttpVersion(val version: String) {
    HTTP_0_9(""),
    HTTP_1_0("HTTP/1.0"),
    HTTP_1_1("HTTP/1.1");

    companion object {
        fun from(version: String) = when(version) {
            "" -> HTTP_0_9
            "HTTP/1.0" -> HTTP_1_0
            "HTTP/1.1" -> HTTP_1_1
            else -> HTTP_1_1
        }
    }
}
