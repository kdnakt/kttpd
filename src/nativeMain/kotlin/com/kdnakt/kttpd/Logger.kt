package com.kdnakt.kttpd

import kotlinx.cinterop.memScoped
import platform.posix.EOF
import platform.posix.fclose
import platform.posix.fopen
import platform.posix.fputs
import kotlinx.datetime.*

enum class LogLevel(val logPrefix: String) {
    ERROR("[ERROR]"),
    INFO("[INFO ]"),
    DEBUG("[DEBUG]"),
}

class Logger(val path: String,
             val level: LogLevel = LogLevel.INFO)

fun Logger.error(log: String) {
    write(log, LogLevel.ERROR)
}

fun Logger.info(log: String) {
    write(log, LogLevel.INFO)
}

fun Logger.debug(log: String) {
    write(log, LogLevel.DEBUG)
}

private fun Logger.write(log: String, minLevel: LogLevel) {
    if (level < minLevel) return
    val logString = "${minLevel.logPrefix} ${now()} $log"
    println(logString)
    val file = fopen(path, "a") ?:
        throw IllegalArgumentException("Cannot open output file $path")
    try {
        memScoped {
            if(fputs("$logString\n", file) == EOF) throw Error("File write error")
        }
    } finally {
        fclose(file)
    }
}

private fun now() = Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Tokyo"))