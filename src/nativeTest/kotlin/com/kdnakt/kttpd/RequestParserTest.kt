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

}