package com.kdnakt.kttpd

import kotlinx.cinterop.*
import platform.posix.*
import kotlin.coroutines.*

fun main() {
    println("kttpd start!")
    val port: Short = 8080

    memScoped {
        val serverAddr = alloc<sockaddr_in>()
        val listenFd = socket(AF_INET, SOCK_STREAM, 0)
                .ensureUnixCallResult("socket") { !it.isMinusOne() }

        with(serverAddr) {
            memset(this.ptr, 0, sizeOf<sockaddr_in>().convert())
            sin_family = AF_INET.convert()
            sin_addr.s_addr = posix_htons(0).convert()
            sin_port = posix_htons(port).convert()
        }

        bind(listenFd, serverAddr.ptr.reinterpret(), sizeOf<sockaddr_in>().convert())
                .ensureUnixCallResult("bind") { it == 0 }

        fcntl(listenFd, F_SETFL, O_NONBLOCK)
                .ensureUnixCallResult("fcntl") { it == 0 }

        listen(listenFd, 10)
                .ensureUnixCallResult("listen") { it == 0 }

        val parser = RequestParser()
        var connectionId = 0
        acceptClientsAndRun(listenFd) {
            memScoped {
                val bufferLength = 1024uL
                val buffer = allocArray<ByteVar>(bufferLength.toLong())
                val connectionIdString = "#${++connectionId}: "

                try {
                    while (true) {
                        val length = read(buffer, bufferLength)

                        if (length == 0uL)
                            break

                        println("[DEBUG]: $connectionIdString")
                        println(buffer.toKString())

                        val request = parser.parse(buffer.toKString())
                        println(request)

                        var content: String
                        var res = OkResponse() as Response
                        try {
                            content = FileReader("public" + request.requestTarget).content()
                        } catch (e: NotFoundException) {
                            res = ErrorResponse(e)
                            content = "${e.status} ${e.reason}"
                        }

                        val ret = when (request.httpVersion) {
                            HttpVersion.HTTP_0_9 -> content
                            HttpVersion.HTTP_1_0,
                            HttpVersion.HTTP_1_1 ->
                                "${request.httpVersion.version} ${res.status} ${res.reason}\r\n"
                                        .plus("Content-Length: ${content.length}\r\n")
                                        .plus("\r\n").plus("${content}\r\n")
                        }

                        write(ret)
                    }
                } catch (e: IOException) {
                    println("I/O error occured: ${e.message}")
                }
            }
        }
    }
}


sealed class WaitingFor {
    class Accept : WaitingFor()

    class Read(val data: CArrayPointer<ByteVar>,
               val length: ULong,
               val continuation: Continuation<ULong>) : WaitingFor()

    class Write(val data: String,
                val continuation: Continuation<Unit>) : WaitingFor()
}

class Client(val clientFd: Int, val waitingList: MutableMap<Int, WaitingFor>) {
    suspend fun read(data: CArrayPointer<ByteVar>, dataLength: ULong): ULong {
        val length = read(clientFd, data, dataLength)
        if (length >= 0)
            return length.toULong()
        if (posix_errno() != EWOULDBLOCK)
            throw IOException(getUnixError())
        // Save continuation and suspend.
        return suspendCoroutine { continuation ->
            waitingList.put(clientFd, WaitingFor.Read(data, dataLength, continuation))
        }
    }

    suspend fun write(data: String) {
        val written = write(clientFd, data.cstr, data.cstr.size.toULong())
        if (written >= 0)
            return
        if (posix_errno() != EWOULDBLOCK)
            throw IOException(getUnixError())
        // Save continuation and suspend.
        return suspendCoroutine { continuation ->
            waitingList.put(clientFd, WaitingFor.Write(data, continuation))
        }
    }
}

fun acceptClientsAndRun(serverFd: Int, block: suspend Client.() -> Unit) {
    memScoped {
        val waitingList = mutableMapOf<Int, WaitingFor>(serverFd to WaitingFor.Accept())
        val readfds = alloc<fd_set>()
        val writefds = alloc<fd_set>()
        val errorfds = alloc<fd_set>()
        var maxfd = serverFd
        while (true) {
            posix_FD_ZERO(readfds.ptr)
            posix_FD_ZERO(writefds.ptr)
            posix_FD_ZERO(errorfds.ptr)
            for ((socketFd, waitingFor) in waitingList) {
                when (waitingFor) {
                    is WaitingFor.Accept -> posix_FD_SET(socketFd, readfds.ptr)
                    is WaitingFor.Read   -> posix_FD_SET(socketFd, readfds.ptr)
                    is WaitingFor.Write  -> posix_FD_SET(socketFd, writefds.ptr)
                }
                posix_FD_SET(socketFd, errorfds.ptr)
            }
            pselect(maxfd + 1, readfds.ptr, writefds.ptr, errorfds.ptr, null, null)
                    .ensureUnixCallResult("pselect") { it >= 0 }
            loop@for (socketFd in 0..maxfd) {
                val waitingFor = waitingList[socketFd]
                val errorOccured = posix_FD_ISSET(socketFd, errorfds.ptr) != 0
                if (posix_FD_ISSET(socketFd, readfds.ptr) != 0
                        || posix_FD_ISSET(socketFd, writefds.ptr) != 0
                        || errorOccured) {
                    when (waitingFor) {
                        is WaitingFor.Accept -> {
                            if (errorOccured)
                                throw Error("Socket has been closed externally")

                            // Accept new client.
                            val clientFd = accept(serverFd, null, null)
                            if (clientFd.isMinusOne()) {
                                if (posix_errno() != EWOULDBLOCK)
                                    throw Error(getUnixError())
                                break@loop
                            }
                            fcntl(clientFd, F_SETFL, O_NONBLOCK)
                                    .ensureUnixCallResult("fcntl") { it == 0 }
                            if (maxfd < clientFd)
                                maxfd = clientFd
                            block.startCoroutine(Client(clientFd, waitingList), EmptyContinuation)
                        }
                        is WaitingFor.Read -> {
                            if (errorOccured)
                                waitingFor.continuation.resumeWithException(IOException("Connection was closed by peer"))

                            // Resume reading operation.
                            waitingList.remove(socketFd)
                            val length = read(socketFd, waitingFor.data, waitingFor.length)
                            if (length < 0) // Read error.
                                waitingFor.continuation.resumeWithException(IOException(getUnixError()))
                            waitingFor.continuation.resume(length.toULong())
                        }
                        is WaitingFor.Write -> {
                            if (errorOccured)
                                waitingFor.continuation.resumeWithException(IOException("Connection was closed by peer"))

                            // Resume writing operation.
                            waitingList.remove(socketFd)
                            val written = write(socketFd, waitingFor.data.cstr, waitingFor.data.cstr.size.toULong())
                            if (written < 0) // Write error.
                                waitingFor.continuation.resumeWithException(IOException(getUnixError()))
                            waitingFor.continuation.resume(Unit)
                        }
                    }
                }
            }
        }
    }
}

open class EmptyContinuation(override val context: CoroutineContext = EmptyCoroutineContext) : Continuation<Any?> {
    companion object : EmptyContinuation()
    override fun resumeWith(result: Result<Any?>) { result.getOrThrow() }
}

class IOException(message: String): RuntimeException(message)

fun getUnixError() = strerror(posix_errno())!!.toKString()

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