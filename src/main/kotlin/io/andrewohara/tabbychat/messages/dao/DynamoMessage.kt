package io.andrewohara.tabbychat.messages.dao

import io.andrewohara.dynamokt.DynamoKtConverted
import io.andrewohara.dynamokt.DynamoKtPartitionKey
import io.andrewohara.dynamokt.DynamoKtSortKey
import io.andrewohara.tabbychat.lib.dao.UserIdConverter
import io.andrewohara.tabbychat.messages.Message
import io.andrewohara.tabbychat.messages.MessageContent
import io.andrewohara.tabbychat.users.UserId
import java.time.Instant

data class DynamoMessage(
    @DynamoKtPartitionKey
    @DynamoKtConverted(converter = UserIdConverter::class)
    val owner: UserId,

    @DynamoKtSortKey
    val id: Long,

    val textContent: String? = null,

    val received: Instant,

    @DynamoKtConverted(converter = UserIdConverter::class)
    val sender: UserId
)

fun DynamoMessage.toMessage() = Message(
    sender = sender,
    received = received,
    content = MessageContent(
        text = textContent
    )
)

fun Message.toDynamo(owner: UserId) = DynamoMessage(
    owner = owner,
    id = received.toEpochMilli(),
    textContent = content.text,
    received = received,
    sender = sender
)