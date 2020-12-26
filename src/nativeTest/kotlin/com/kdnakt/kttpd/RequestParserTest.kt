package com.kdnakt.kttpd

import kotlin.test.*

class RequestParserTest {
    private val crlf = "\r\n"
    @Test
    fun shouldParseGetMethod() {
        val reqByteArray = ("GET /index.html HTTP/1.1" + crlf
                + "Host: localhost:8080" + crlf
                + crlf).encodeToByteArray()
        val context = parse(reqByteArray)
        assertEquals(HttpMethod.GET, context.method, "should return GET method")
    }

    @Test
    fun shouldParseRequestTarget() {
        val reqByteArray = ("GET /index.html HTTP/1.1" + crlf
                + "Host: localhost:8080" + crlf
                + crlf).encodeToByteArray()
        val context = parse(reqByteArray)
        assertEquals("/index.html", context.requestTarget, "should return /index.html")
    }

    @Test
    fun shouldParseHttpVersion_1_1() {
        val reqByteArray = ("GET /index.html HTTP/1.1" + crlf
                + "Host: localhost:8080" + crlf
                + crlf).encodeToByteArray()
        val context = parse(reqByteArray)
        assertEquals(HttpVersion.HTTP_1_1, context.httpVersion, "should return HTTP/1.1")
    }

    @Test
    fun shouldParseHeaderWithRightSize() {
        val reqByteArray = ("GET /index.html HTTP/1.1" + crlf
                + "Host: localhost:8080" + crlf
                + crlf).encodeToByteArray()
        val context = parse(reqByteArray)
        assertEquals(1, context.headers.size)
    }

    @Test
    fun shouldParseHeaderContent() {
        val reqByteArray = ("GET /index.html HTTP/1.1" + crlf
                + "Host: localhost:8080" + crlf
                + crlf).encodeToByteArray()
        val context = parse(reqByteArray)
        assertEquals("localhost:8080", context.headers["Host"])
    }

    @Test
    fun shouldParseHeaderContentWithoutOWS() {
        val reqByteArray = ("GET /index.html HTTP/1.1" + crlf
                + "Host:localhost:8080" + crlf
                + crlf).encodeToByteArray()
        val context = parse(reqByteArray)
        assertEquals("localhost:8080", context.headers["Host"])
    }

    @Test
    fun shouldParseHeaderContentWithOWSAfterFieldValue() {
        val reqByteArray = ("GET /index.html HTTP/1.1" + crlf
                + "Host: localhost:8080\t" + crlf
                + crlf).encodeToByteArray()
        val context = parse(reqByteArray)
        assertEquals("localhost:8080", context.headers["Host"])
    }

    @Test
    fun shouldParseHttpVersion_1_0() {
        val reqByteArray = ("GET /index.html HTTP/1.0" + crlf
                + crlf).encodeToByteArray()
        val context = parse(reqByteArray)
        assertEquals(HttpVersion.HTTP_1_0, context.httpVersion, "should return HTTP/1.0")
    }

    @Test
    fun shouldParseHttpVersion_0_9() {
        val reqByteArray = ("GET /index.html" + crlf
                + crlf).encodeToByteArray()
        val context = parse(reqByteArray)
        assertEquals(HttpVersion.HTTP_0_9, context.httpVersion, "should return HTTP/0.9")
    }

    @Test
    fun shouldNotParseHttpResponse() {
        val resByteArray = ("HTTP/1.1 200 OK" + crlf
                + crlf).encodeToByteArray()
        val actual = assertFailsWith<BadRequestException> { parse(resByteArray) }
        assertEquals(400, actual.status)
        assertEquals("Bad Request", actual.reason)
    }
}