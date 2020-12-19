package com.kdnakt.kttpd

import kotlin.test.Test
import kotlin.test.assertEquals

class URLDecoderTest {
    @Test
    fun testAlphabet() {
        assertEquals("ABC", decodeURL("ABC"))
    }

    @Test
    fun testPlus() {
        assertEquals(" ", decodeURL("+"))
    }

    @Test
    fun testColon() {
        assertEquals(":", decodeURL("%3A"))
    }

    @Test
    fun testSlash() {
        assertEquals("/", decodeURL("%2f"))
    }

    @Test
    fun testJapaneseKatakana() {
        assertEquals("ア", decodeURL("%E3%82%A2"))
    }

    @Test
    fun testJapaneseKanji() {
        assertEquals("日本", decodeURL("%E6%97%A5%E6%9C%AC"))
    }
}