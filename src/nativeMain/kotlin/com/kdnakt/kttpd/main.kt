package com.kdnakt.kttpd

import kotlinx.cinterop.*
import platform.posix.*

fun main() {
    println("Hello Kotlin/Native!")
    val port: Short = 8080

    memScoped {
        val buffer = ByteArray(1024)
        val serverAddr = alloc<sockaddr_in>()
        val listenFd = socket(AF_INET, SOCK_STREAM, 0)
                .ensureUnixCallResult("socket") { !it.isMinusOne() }

        with(serverAddr) {
            memset(this.ptr, 0, sizeOf<sockaddr_in>().convert())
            sin_family = AF_INET.convert()
            sin_port = posix_htons(port).convert()
        }

        bind(listenFd, serverAddr.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
                .ensureUnixCallResult("bind") { it == 0 }

        listen(listenFd, 10)
                .ensureUnixCallResult("listen") { it == 0 }

        val commFd = accept(listenFd, null, null)
                .ensureUnixCallResult("accept") { !it.isMinusOne() }

        val parser = RequestParser()
        buffer.usePinned { pinned ->
            while (true) {
                val length = recv(commFd, pinned.addressOf(0), buffer.size.convert(), 0).toInt()
                        .ensureUnixCallResult("read") { it >= 0 }

                if (length == 0) {
                    break
                }

                println("[DEBUG]:")
                println(pinned.get().toKString())

                val request = parser.parse(pinned.get())
                println(request)

                var content = ""
                var res = OkResponse() as Response
                try {
                    content = FileReader("public" + request.requestTarget).content()
                } catch (e: NotFoundException) {
                    res = ErrorResponse(e)
                }

                val ret = when (request.httpVersion) {
                    HttpVersion.HTTP_0_9 -> content
                    HttpVersion.HTTP_1_0,
                    HttpVersion.HTTP_1_1 ->
                        "${request.httpVersion.version} ${res.status} ${res.reason}\r\n"
                                .plus("Content-Length: ${content.length}\r\n")
                                        .plus("\r\n").plus("${content}\r\n")
                }

                send(commFd, ret.cstr, ret.cstr.size.toULong(), 0)
                        .ensureUnixCallResult("write") { it >= 0}
            }
        }
    }
}


inline fun Int.ensureUnixCallResult(op: String, predicate: (Int) -> Boolean): Int {
    if (!predicate(this)) {
        throw Error("$op: ${strerror(posix_errno())!!.toKString()}")
    }
    return this
}

inline fun Long.ensureUnixCallResult(op: String, predicate: (Long) -> Boolean): Long {
    if (!predicate(this)) {
        throw Error("$op: ${strerror(posix_errno())!!.toKString()}")
    }
    return this
}

inline fun ULong.ensureUnixCallResult(op: String, predicate: (ULong) -> Boolean): ULong {
    if (!predicate(this)) {
        throw Error("$op: ${strerror(posix_errno())!!.toKString()}")
    }
    return this
}

private fun Int.isMinusOne() = (this == -1)
private fun Long.isMinusOne() = (this == -1L)
private fun ULong.isMinusOne() = (this == ULong.MAX_VALUE)