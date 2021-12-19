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
import java.time.Instant

object V1Samples {

    val user1 = User(
        id = UserId.create(Realm("http://tabbychat.io")),
        name = RealName(first = "Toggles", middle = null, last = "Brown"),
        icon = URL("http://tabbychat.io/icons/togglesBrown.jpg")
    )

    val user2 = User(
        id = UserId.create(Realm("http://brownchat.com")),
        name = RealName(first = "Toll", middle = "the", last = "Troll"),
        icon = URL("http://brownchat.com/icons/tollTroll.jpg")
    )

    val userIds = arrayOf(user1.id, user2.id)

    val tokenData = TokenData(
        userId = user1.id,
        token = AccessToken("ABC123")
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
