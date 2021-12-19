package io.andrewohara.tabbychat.protocol.v1

import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.contacts.TokenData
import io.andrewohara.tabbychat.messages.Message
import io.andrewohara.tabbychat.messages.MessageContent
import io.andrewohara.tabbychat.messages.MessagePage
import io.andrewohara.tabbychat.messages.MessageReceipt
import io.andrewohara.tabbychat.users.RealName
import io.andrewohara.tabbychat.users.User
import io.andrewohara.tabbychat.users.UserId
import java.net.URL
import java.time.Instant

data class UserDtoV1(
    val id: String,
    val name: RealNameDtoV1?,
    val icon: String?
)
fun User.toDtoV1() = UserDtoV1(
    id = id.value,
    name = name?.let {
        RealNameDtoV1(
            first = it.first,
            middle = it.middle,
            last = it.last
        )
    },
    icon = icon?.toString()
)
fun UserDtoV1.toModel() = User(
    id = UserId(id),
    icon = icon?.let { URL(it) },
    name = name?.let {
        RealName(
            first = it.first,
            middle = it.middle,
            last = it.last
        )
    }
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
fun MessageDtoV1.toModel() = Message(
    sender = UserId(senderId),
    recipient = UserId(recipientId),
    received = received,
    content = content.toModel()
)
fun Message.toDtoV1() = MessageDtoV1(
    senderId = sender.value,
    recipientId = recipient.value,
    received = received,
    content = MessageContentDtoV1(
        text = content.text
    )
)

data class MessageReceiptDtoV1(
    val sender: String,
    val recipient: String,
    val received: Instant
)
fun MessageReceiptDtoV1.toModel() = MessageReceipt(
    sender = UserId(sender),
    recipient = UserId(recipient),
    received = received
)
fun MessageReceipt.toDtoV1() = MessageReceiptDtoV1(
    sender = sender.value,
    recipient = recipient.value,
    received = received
)

data class MessagePageDtoV1(
    val messages: List<MessageDtoV1>,
    val nextTime: Instant?
)
fun MessagePage.toDtoV1() = MessagePageDtoV1(
    messages = messages.map { it.toDtoV1() },
    nextTime = nextTime
)
fun MessagePageDtoV1.toModel() = MessagePage(
    nextTime = nextTime,
    messages = messages.map { it.toModel() }
)

data class MessageContentDtoV1(
    val text: String?
)
fun MessageContentDtoV1.toModel() = MessageContent(
    text = text,
)
fun MessageContent.toDtoV1() = MessageContentDtoV1(
    text = text,
)

data class InvitationDtoV1(
    val userId: String,
    val token: String
)
fun InvitationDtoV1.toModel() = TokenData(userId = UserId(userId), token = AccessToken(token))
fun TokenData.toDtoV1() = InvitationDtoV1(userId = userId.value, token = token.value)