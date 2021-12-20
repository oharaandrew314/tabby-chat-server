package io.andrewohara.tabbychat.protocol.v1

import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.Realm
import io.andrewohara.tabbychat.contacts.TokenData
import io.andrewohara.tabbychat.messages.Message
import io.andrewohara.tabbychat.messages.MessageContent
import io.andrewohara.tabbychat.messages.MessagePage
import io.andrewohara.tabbychat.messages.MessageReceipt
import io.andrewohara.tabbychat.users.RealName
import io.andrewohara.tabbychat.users.User
import io.andrewohara.tabbychat.users.UserId
import java.net.URL

// UserId
fun String.toUserId() = UserId(this)
fun UserId.toDtoV1() = value
fun List<UserId>.toDtoV1() = map { it.value }.toTypedArray()

// User
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

// Message
fun Message.toDtoV1() = MessageDtoV1(
    senderId = sender.value,
    recipientId = recipient.value,
    received = received,
    content = MessageContentDtoV1(
        text = content.text
    )
)

// Receipt
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

// Page
fun MessagePage.toDtoV1() = MessagePageDtoV1(
    messages = messages.map { it.toDtoV1() },
    nextTime = nextTime
)

// Content
fun MessageContentDtoV1.toModel() = MessageContent(
    text = text,
)
fun MessageContent.toDtoV1() = MessageContentDtoV1(
    text = text,
)

// Token
fun TokenDataDtoV1.toModel() = TokenData(
    accessToken = AccessToken(accessToken),
    realm = Realm(realm),
    expires = expires
)
fun TokenData.toDtoV1() = TokenDataDtoV1(
    accessToken = accessToken.value,
    realm = realm.value,
    expires = expires
)