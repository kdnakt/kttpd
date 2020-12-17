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
        assertFailsWith<NotFoundException> { reader.content() }
    }

    @Test
    fun testURIencodedDirectoryTraversal() {
        val reader = FileReader("publicTest/%2e%2e%2fpublic/index.html")
        assertFailsWith<NotFoundException> { reader.content() }
    }
}