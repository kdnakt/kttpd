package com.kdnakt.kttpd

import kotlinx.cinterop.memScoped
import kotlinx.datetime.*
import platform.posix.*

enum class LogLevel(val logPrefix: String) {
    ERROR("[ERROR]"),
    INFO("[INFO ]"),
    DEBUG("[DEBUG]"),
}

class Logger(val path: String,
             val level: LogLevel = LogLevel.INFO) {
    init {
        val dirPath = path.substring(0, path.lastIndexOf("/"))
        opendir(dirPath)
        if (ENOENT == errno) {// dir doesn't exist
            val permission = S_IRUSR.or(S_IWUSR).or(S_IXUSR) /* rwx */
                    .or(S_IRGRP).or(S_IWGRP).or(S_IXGRP)     /* rwx */
                    .or(S_IROTH).or(S_IWOTH).or(S_IXOTH)     /* rwx */
            if (mkdir(dirPath, permission.toUShort()) != 0)
                throw Error("cannot create log directory")
        }
    }
}

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
    val file = fopen(path, "a")
            ?: throw IllegalArgumentException("Cannot open output file $path [Errno $errno]")
    try {
        memScoped {
            if(fputs("$logString\n", file) == EOF) throw Error("File write error")
        }
    } finally {
        fclose(file)
    }
}

private fun now() = Clock.System.now().toLocalDateTime(TimeZone.of("Asia/Tokyo"))