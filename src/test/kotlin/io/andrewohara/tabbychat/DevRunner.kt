package io.andrewohara.tabbychat

import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8000

    val service = TabbyChatProvider.fromEnv(System.getenv())

    val server = service.http
        .asServer(SunHttp(port))
        .start()

    println("Running ${service.realm} provider on port $port")
    server.block()
}