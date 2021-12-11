package io.andrewohara.tabbychat

import org.http4k.serverless.ApiGatewayV2LambdaFunction
import org.http4k.serverless.AppLoader

class ApiLambdaHandler: ApiGatewayV2LambdaFunction(AppLoader { env ->
    val service = ServiceFactory.fromEnv(env)
    service.createHttp()
})