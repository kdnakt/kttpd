package com.kdnakt.kttpd

import kotlinx.cinterop.toKString

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
                    bArr += hex.toInt(16).toByte()
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