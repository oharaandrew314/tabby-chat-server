package io.andrewohara.tabbychat

import org.http4k.core.HttpHandler
import org.http4k.serverless.ApiGatewayV2LambdaFunction

val appLoader: (Map<String, String>) -> HttpHandler = { env ->
    val service = ServiceFactory.fromEnv(env)
    service.createHttp()
}

class ApiLambdaHandler: ApiGatewayV2LambdaFunction(appLoader)