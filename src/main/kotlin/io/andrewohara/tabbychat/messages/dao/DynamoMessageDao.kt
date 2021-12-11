package io.andrewohara.tabbychat.messages.dao

import io.andrewohara.tabbychat.messages.Message
import io.andrewohara.tabbychat.users.UserId
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest
import java.time.Instant

class DynamoMessageDao(private val mapper: DynamoDbTable<DynamoMessage>): MessageDao {

    override fun add(owner: UserId, message: Message) {
        val item = message.toDynamo(owner)
        mapper.putItem(item)
    }

    override fun list(user: UserId, start: Instant, end: Instant): List<Message> {
        val condition = QueryConditional.sortBetween(
            Key.builder().partitionValue(user.toString()).sortValue(start.toString()).build(),
            Key.builder().partitionValue(user.toString()).sortValue(end.toString()).build()
        )

        return mapper.query { builder: QueryEnhancedRequest.Builder ->
            builder.scanIndexForward(true)
            builder.queryConditional(condition)
        }.items()
            .map { it.toMessage() }
    }
}

