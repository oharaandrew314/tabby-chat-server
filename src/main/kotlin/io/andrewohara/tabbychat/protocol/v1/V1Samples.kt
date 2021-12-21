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
import org.http4k.core.Uri
import java.time.Instant

object V1Samples {

    val user1 = User(
        id = UserId("toggles"),
        name = RealName(first = "Toggles", middle = null, last = "Brown"),
        icon = "http://tabbychat.io/icons/togglesBrown.jpg"
    )

    val user2 = User(
        id = UserId("trollTroll"),
        name = RealName(first = "Toll", middle = "the", last = "Troll"),
        icon = "http://brownchat.com/icons/tollTroll.jpg"
    )

    val userIds = listOf(user1.id, user2.id)

    val tokenData = TokenData(
        accessToken = AccessToken("ABC123"),
        realm = Realm(Uri.of("http://tabby.chat")),
        expires = null
    )

    val message1 = Message(
        sender = user1.id,
        recipient = user2.id,
        received = Instant.ofEpochSecond(1337),
        content = MessageContent(
            text = "hai!"
        )
    )

    val messageReceipt = MessageReceipt(
        sender = user1.id,
        recipient = user2.id,
        received = Instant.ofEpochSecond(1337),
    )

    val messageList = MessagePage(
        messages = listOf(
            message1,
            Message(
                sender = user2.id,
                recipient = user1.id,
                received = Instant.ofEpochSecond(9001),
                content = MessageContent(
                    text = "sup"
                )
            )
        ),
        nextTime = Instant.ofEpochSecond(12345)
    )
}
