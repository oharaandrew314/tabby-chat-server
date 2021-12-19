package io.andrewohara.tabbychat.protocol.v1

import io.andrewohara.tabbychat.TabbyChatError
import org.http4k.core.Response
import org.http4k.core.Status
import java.lang.IllegalStateException

fun Response.toError() = when(status) {
    Status.UNAUTHORIZED -> TabbyChatError.Forbidden
    Status.NOT_FOUND -> TabbyChatError.NotFound
    Status.FORBIDDEN -> TabbyChatError.Forbidden
    else -> throw IllegalStateException(toMessage())
}
fun TabbyChatError.toResponse() = when(this) {
    TabbyChatError.NotFound -> Response(Status.NOT_FOUND)
    TabbyChatError.Forbidden -> Response(Status.FORBIDDEN)
    TabbyChatError.NotContact -> Response(Status.NOT_FOUND)
}