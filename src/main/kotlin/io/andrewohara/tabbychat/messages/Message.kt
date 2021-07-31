package io.andrewohara.tabbychat.messages

import io.andrewohara.tabbychat.users.UserId
import java.time.Instant

data class Message(
    val content: MessageContent,
    val received: Instant,
    val sender: UserId
)