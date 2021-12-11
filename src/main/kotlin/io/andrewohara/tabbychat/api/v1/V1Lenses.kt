package io.andrewohara.tabbychat.api.v1

import org.http4k.core.Body
import org.http4k.format.Jackson.auto

object V1Lenses {
    val messageContent = Body.auto<MessageContentDtoV1>().toLens()
    val messageList = Body.auto<Array<MessageDtoV1>>().toLens()
    val accessToken = Body.auto<AccessTokenDtoV1>().toLens()
    val sendMessageRequest = Body.auto<SendMessageRequestV1>().toLens()
    val user = Body.auto<UserDtoV1>().toLens()
    val spec = Body.auto<SpecDtoV1>().toLens()
}