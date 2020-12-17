package com.kdnakt.kttpd

import kotlinx.cinterop.*
import platform.posix.*

class FileReader(private val path: String) {
    private var loaded = false
    private var content = ""
    fun content(): String {
        if (loaded) return content
        if (path.contains("..")) {
            throw NotFoundException()
        }
        val file = fopen(path, "r")
        try {
            if (file == null) {
                perror("cannot open file: $path")
                throw NotFoundException()
            }
            memScoped {
                val bufferLength = 64 * 1024
                val buffer = allocArray<ByteVar>(bufferLength)
                while (true) {
                    val nextLine = fgets(buffer, bufferLength, file)?.toKString()
                    if (nextLine == null || nextLine.isEmpty()) break
                    content += nextLine
                }
                loaded = true
            }
            return content
        } finally {
            fclose(file)
        }
    }

}

class NotFoundException(): HttpException(404, "Not Found")