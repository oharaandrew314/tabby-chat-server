package io.andrewohara.tabbychat.messages

import io.andrewohara.tabbychat.users.UserId
import java.time.Instant

data class Message(
    val content: MessageContent,
    val received: Instant,
    val sender: UserId,
    val recipient: UserId
)
fun Message.toReceipt() = MessageReceipt(
    sender = sender,
    recipient = recipient,
    received = received
)

data class MessagePage(
    val messages: List<Message>,
    val nextTime: Instant?
)

data class MessageReceipt(
    val sender: UserId,
    val recipient: UserId,
    val received: Instant,
)
fun MessageReceipt.toMessage(content: MessageContent) = Message(
    sender = sender,
    recipient = recipient,
    received = received,
    content = content
)