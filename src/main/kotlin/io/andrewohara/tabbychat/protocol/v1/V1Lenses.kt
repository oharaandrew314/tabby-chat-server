package io.andrewohara.tabbychat.protocol.v1

import io.andrewohara.tabbychat.contacts.TokenData
import io.andrewohara.tabbychat.messages.MessageContent
import io.andrewohara.tabbychat.messages.MessagePage
import io.andrewohara.tabbychat.messages.MessageReceipt
import io.andrewohara.tabbychat.users.User
import io.andrewohara.tabbychat.users.UserId
import org.http4k.core.Body
import org.http4k.format.Moshi.auto
import org.http4k.lens.*

object V1Lenses {
    val userId = Path.string()
        .map({ UserId(it) }, UserId::value)
        .of("user_id")
    val accessToken = Path.nonEmptyString().of("access_token")

    val since = Query.instant().required("since")

    val messageContent = Body.auto<MessageContentDtoV1>()
        .map(MessageContentDtoV1::toModel, MessageContent::toDtoV1)
        .toLens()
    val messagePage = Body.auto<MessagePageDtoV1>()
        .map(MessagePageDtoV1::toModel, MessagePage::toDtoV1)
        .toLens()
    val tokenData = Body.auto<AccessTokenDtoV1>()
        .map(AccessTokenDtoV1::toModel, TokenData::toDtoV1)
        .toLens()
    val user = Body.auto<UserDtoV1>()
        .map(UserDtoV1::toModel, User::toDtoV1)
        .toLens()
    val messageReceipt = Body.auto<MessageReceiptDtoV1>()
        .map(MessageReceiptDtoV1::toModel, MessageReceipt::toDtoV1)
        .toLens()
    val userIds = Body.auto<Array<String>>()
        .map( { ids -> ids.map { UserId(it) }.toTypedArray() }, { ids -> ids.map { it.value }.toTypedArray() })
        .toLens()
}