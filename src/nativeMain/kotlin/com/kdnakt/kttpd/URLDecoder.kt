package com.kdnakt.kttpd

import kotlinx.cinterop.toKString

private const val CHAR_COLON = 58
private const val INDEX_ZERO = 48
private const val INDEX_A    = 55

fun String.toByteObj(): Byte {
    val cArr = toUpperCase().toCharArray()
    var res = 0
    var p = 1
    for (i in cArr.size - 1 downTo 0) {
        val c = cArr[i].toInt()
        val sub = if (c <= CHAR_COLON) INDEX_ZERO else INDEX_A
        res += (c - sub) * p
        p *= 16
    }
    return res.toByte()
}

fun decodeURL(url: String): String {
    val res = StringBuilder()
    var i = 0
    while (i < url.length) {
        when (val c = url[i]) {
            '+' -> {
                res.append(' ')
                i++
            }
            '%' -> {
                var bArr = byteArrayOf()
                var curr = c
                while (i + 2 < url.length && '%' == curr) {
                    val hex = url.substring(i + 1, i + 3)
                    bArr += hex.toByteObj()
                    i += 3
                    if (i < url.length) {
                        curr = url[i]
                    }
                }

                res.append(bArr.toKString())
            }
            else -> {
                res.append(c)
                i++
            }
        }
    }
    return res.toString()
}