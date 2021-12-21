package io.andrewohara.tabbychat.protocol.v1

import org.http4k.core.Uri
import java.time.Instant

data class UserDtoV1(
    val id: String,
    val name: RealNameDtoV1?,
    val icon: Uri?
)

data class RealNameDtoV1(
    val first: String,
    val middle: String?,
    val last: String?
)

data class MessageDtoV1(
    val senderId: String,
    val recipientId: String,
    val received: Instant,
    val content: MessageContentDtoV1,
)

data class MessageReceiptDtoV1(
    val sender: String,
    val recipient: String,
    val received: Instant
)

data class MessagePageDtoV1(
    val messages: List<MessageDtoV1>,
    val nextTime: Instant?
)

data class MessageContentDtoV1(
    val text: String?
)

data class TokenDataDtoV1(
    val realm: Uri,
    val accessToken: String,
    val expires: Instant?
)