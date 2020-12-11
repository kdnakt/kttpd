package com.kdnakt.kttpd

import kotlin.test.Test
import kotlin.test.assertEquals

class RequestParserTest {
    private val parser: RequestParser = RequestParser()
    private val crlf = "\r\n"
    @Test
    fun shouldParseGetMethod() {
        val reqByteArray = ("GET /index.html HTTP/1.1" + crlf
                + "Host: localhost:8080" + crlf
                + crlf).encodeToByteArray()
        val context = parser.parse(reqByteArray)
        assertEquals(HttpMethod.GET, context.method, "should return GET method")
    }

    @Test
    fun shouldParseRequestTarget() {
        val reqByteArray = ("GET /index.html HTTP/1.1" + crlf
                + "Host: localhost:8080" + crlf
                + crlf).encodeToByteArray()
        val context = parser.parse(reqByteArray)
        assertEquals("/index.html", context.requestTarget, "should return /index.html")
    }

    @Test
    fun shouldParseHttpVersion_1_1() {
        val reqByteArray = ("GET /index.html HTTP/1.1" + crlf
                + "Host: localhost:8080" + crlf
                + crlf).encodeToByteArray()
        val context = parser.parse(reqByteArray)
        assertEquals(HttpVersion.HTTP_1_1, context.httpVersion, "should return HTTP/1.1")
    }

    @Test
    fun shouldParseHttpVersion_1_0() {
        val reqByteArray = ("GET /index.html HTTP/1.0" + crlf
                + crlf).encodeToByteArray()
        val context = parser.parse(reqByteArray)
        assertEquals(HttpVersion.HTTP_1_0, context.httpVersion, "should return HTTP/1.1")
    }

    @Test
    fun shouldParseHttpVersion_0_9() {
        val reqByteArray = ("GET /index.html" + crlf
                + crlf).encodeToByteArray()
        val context = parser.parse(reqByteArray)
        assertEquals(HttpVersion.HTTP_0_9, context.httpVersion, "should return HTTP/1.1")
    }
}