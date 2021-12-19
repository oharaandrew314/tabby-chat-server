package io.andrewohara.tabbychat.messages.dao

import io.andrewohara.tabbychat.messages.Message
import io.andrewohara.tabbychat.messages.MessagePage
import io.andrewohara.tabbychat.users.UserId
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest
import java.time.Instant

class MessageDao(private val mapper: DynamoDbTable<DynamoMessage>): Iterable<Message> {

    fun add(owner: UserId, message: Message) {
        val item = message.toDynamo(owner)
        mapper.putItem(item)
    }

    fun list(user: UserId, since: Instant, limit: Int): MessagePage {
        val condition = QueryConditional.sortBeginsWith(
            Key.builder().partitionValue(user.value).sortValue(since.toString()).build()
        )

        val messages = mapper.query { builder: QueryEnhancedRequest.Builder ->
            builder.scanIndexForward(true)
            builder.queryConditional(condition)
        }.items().take(limit + 1)

        return MessagePage(
            messages = messages.take(limit).map { it.toMessage() },
            nextTime = messages.lastOrNull()?.received
        )
    }

    override fun iterator() = mapper.scan()
        .items()
        .map { it.toMessage() }
        .iterator()
}

