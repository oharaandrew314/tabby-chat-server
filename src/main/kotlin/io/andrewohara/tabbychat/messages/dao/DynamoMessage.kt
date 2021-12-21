package io.andrewohara.tabbychat.messages.dao

import io.andrewohara.dynamokt.DynamoKtConverted
import io.andrewohara.dynamokt.DynamoKtPartitionKey
import io.andrewohara.dynamokt.DynamoKtSortKey
import io.andrewohara.tabbychat.lib.dao.UserIdConverter
import io.andrewohara.tabbychat.messages.Message
import io.andrewohara.tabbychat.messages.MessageContent
import io.andrewohara.tabbychat.users.UserId
import io.andrewohara.utils.IdGenerator
import java.time.Instant

data class DynamoMessage(
    @DynamoKtPartitionKey
    @DynamoKtConverted(UserIdConverter::class)
    val ownerId: UserId,

    @DynamoKtSortKey
    val id: String,

    val textContent: String? = null,
    val received: Instant,

    @DynamoKtConverted(UserIdConverter::class)
    val sender: UserId,

    @DynamoKtConverted(UserIdConverter::class)
    val recipient: UserId
)

fun DynamoMessage.toMessage() = Message(
    sender = sender,
    recipient = recipient,
    received = received,
    content = MessageContent(
        text = textContent
    )
)

fun Message.toDynamo(owner: UserId) = DynamoMessage(
    ownerId = owner,
    recipient = recipient,
    id = "$received-${IdGenerator.nextBase36(4)}",
    textContent = content.text,
    received = received,
    sender = sender
)