package com.kdnakt.kttpd

abstract class Response(val status: Int, val reason: String)

class OkResponse(): Response(200, "OK")

class ErrorResponse(e: HttpException): Response(e.status, e.reason)
