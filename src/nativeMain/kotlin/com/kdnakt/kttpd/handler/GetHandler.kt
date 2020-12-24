package com.kdnakt.kttpd.handler

import com.kdnakt.kttpd.*

class GetHandler: RequestHandler() {
    override fun handleImpl(req: RequestContext): Pair<String, Response> {
        var content: String
        var res = OkResponse() as Response
        try {
            content = FileReader("public" + req.requestTarget).content()
        } catch (e: NotFoundException) {
            res = ErrorResponse(e)
            content = "${e.status} ${e.reason}"
        }
        return Pair(content, res)
    }
}