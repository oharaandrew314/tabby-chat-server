package io.andrewohara.tabbychat.messages.dao

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import io.andrewohara.tabbychat.messages.Message
import io.andrewohara.tabbychat.messages.MessageContent
import io.andrewohara.tabbychat.users.UserId
import io.andrewohara.lib.IsoInstantConverter
import io.andrewohara.lib.UserIdConverter
import java.time.Instant

@DynamoDBDocument
class DynamoMessage(
    @DynamoDBHashKey
    @DynamoDBTypeConverted(converter = UserIdConverter::class)
    var owner: UserId? = null,

    @DynamoDBRangeKey
    var id: Long? = null,

    var textContent: String? = null,

    @DynamoDBTypeConverted(converter = IsoInstantConverter::class)
    var received: Instant? = null,

    @DynamoDBTypeConverted(converter = UserIdConverter::class)
    var sender: UserId? = null
) {
    fun toMessage() = Message(
        sender = sender!!,
        received = received!!,
        content = MessageContent(
            text = textContent
        )
    )

    constructor(owner: UserId, message: Message): this(
        owner = owner,
        id = message.received.toEpochMilli(),
        textContent = message.content.text,
        received = message.received,
        sender = message.sender
    )
}