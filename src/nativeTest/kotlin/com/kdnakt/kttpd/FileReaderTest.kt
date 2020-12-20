package com.kdnakt.kttpd

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FileReaderTest {
    @Test
    fun testReadSampleTxt() {
        val reader = FileReader("publicTest/Sample.txt")
        assertEquals("""First line
            |Second line
            |Third line
        """.trimMargin(), reader.content())
    }

    @Test
    fun testFileNotExist() {
        val reader = FileReader("publicTest/FileNotExists.txt")
        val actual = assertFailsWith<NotFoundException> { reader.content() }
        assertEquals(404, actual.status)
        assertEquals("Not Found", actual.reason)
    }

    @Test
    fun testDirectoryTraversal() {
        val reader = FileReader("publicTest/../public/index.html")
        assertFailsWith<ForbiddenException> { reader.content() }
    }

    @Test
    fun testURIEncodedDirectoryTraversal() {
        val reader = FileReader("publicTest/%2e%2e%2fpublic/index.html")
        assertFailsWith<ForbiddenException> { reader.content() }
    }

    @Test
    fun testURIEncoded() {
        // publicTest/メモ.txt
        val reader = FileReader("publicTest/%E3%83%A1%E3%83%A2.txt")
        assertEquals("""test memo1
            |test memo2
        """.trimMargin(), reader.content())
    }

    @Test
    fun testURIEncodedContainsDirectoryTraversal() {
        // publicReader/../public/Sample.txt
        val reader = FileReader("publicTest/%2e%2E%2Fpublic/Sample.txt")
        assertFailsWith<ForbiddenException> { reader.content() }
    }

}