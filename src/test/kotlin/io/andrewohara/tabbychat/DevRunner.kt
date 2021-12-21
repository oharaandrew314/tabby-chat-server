package io.andrewohara.tabbychat

import dev.forkhandles.result4k.valueOrNull
import org.http4k.server.SunHttp
import org.http4k.server.asServer

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8000

    val provider = TabbyChatProvider.fromEnv(System.getenv())

    val server = provider.http
        .asServer(SunHttp(port))
        .start()

    println("Running ${provider.realm} provider on port $port")

    val user = provider.createUser("Test User")
    val token = provider.service.createAccessToken(user.id).valueOrNull()!!

    println("Access Token: $token")

    server.block()
}