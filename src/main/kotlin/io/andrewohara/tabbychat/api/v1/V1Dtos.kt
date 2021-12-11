package io.andrewohara.tabbychat.api.v1

import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.messages.Message
import io.andrewohara.tabbychat.messages.MessageContent
import io.andrewohara.tabbychat.users.RealName
import io.andrewohara.tabbychat.users.User
import io.andrewohara.tabbychat.users.UserId
import java.net.URL

data class UserIdDtoV1(
    val realm: String,
    val id: String,
)

data class UserDtoV1(
    val id: UserIdDtoV1,
    val name: RealNameDtoV1?,
    val icon: String?
)

data class RealNameDtoV1(
    val first: String,
    val middle: String?,
    val last: String?
)

data class MessageDtoV1(
    val senderId: UserIdDtoV1,
    val content: MessageContentDtoV1,
    val received: String
)

data class MessageContentDtoV1(
    val text: String?
)

data class AccessTokenDtoV1(
    val realm: String,
    val value: String,
)

data class SendMessageRequestV1(
    val recipient: UserIdDtoV1,
    val content: MessageContentDtoV1,
)

data class SpecDtoV1(
    val realm: String,
    val version: Int
)

class V1DtoMapper {

    operator fun invoke(user: User) = UserDtoV1(
        id = invoke(user.id),
        name = user.name?.let {
            RealNameDtoV1(
                first = it.first,
                middle = it.middle,
                last = it.last
            )
        },
        icon = user.icon?.toString()
    )
    operator fun invoke(user: UserDtoV1) = User(
        id = invoke(user.id),
        icon = user.icon?.let { URL(it) },
        name = user.name?.let {
            RealName(
                first = it.first,
                middle = it.middle,
                last = it.last
            )
        }
    )

    operator fun invoke(message: Message) = MessageDtoV1(
        senderId = invoke(message.sender),
        content = MessageContentDtoV1(
            text = message.content.text
        ),
        received = message.received.toString()
    )

    operator fun invoke(content: MessageContentDtoV1) = MessageContent(
        text = content.text,
    )
    operator fun invoke(content: MessageContent) = MessageContentDtoV1(
        text = content.text,
    )

    operator fun invoke(token: AccessToken) = AccessTokenDtoV1(
        realm = token.realm,
        value = token.value,
    )
    operator fun invoke(token: AccessTokenDtoV1) = AccessToken(
        realm = token.realm,
        value = token.value,
    )

    operator fun invoke(request: SendMessageRequestV1): Pair<UserId, MessageContent> {
        val userId = invoke(request.recipient)
        val content = invoke(request.content)
        return userId to content
    }

    operator fun invoke(userId: UserId) = UserIdDtoV1(
        realm = userId.realm,
        id = userId.id
    )
    operator fun invoke(userId: UserIdDtoV1) = UserId(
        realm = userId.realm,
        id = userId.id
    )
}