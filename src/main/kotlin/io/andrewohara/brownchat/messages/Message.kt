package io.andrewohara.brownchat.messages

import io.andrewohara.brownchat.users.UserId
import java.time.Instant

data class Message(
    val content: MessageContent,
    val received: Instant,
    val sender: UserId
)