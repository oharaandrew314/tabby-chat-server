package io.andrewohara.brownchat.messages.dao

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTableMapper
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.ComparisonOperator
import com.amazonaws.services.dynamodbv2.model.Condition
import io.andrewohara.brownchat.messages.Message
import io.andrewohara.brownchat.users.UserId
import java.time.Instant

class DynamoMessageDao(private val mapper: DynamoDBTableMapper<DynamoMessage, String, Long>): MessageDao {

    override fun add(owner: UserId, message: Message) {
        val item = DynamoMessage(owner, message)
        mapper.save(item)
    }

    override fun list(user: UserId, start: Instant, end: Instant): List<Message> {
        val between = Condition()
            .withComparisonOperator(ComparisonOperator.BETWEEN)
            .withAttributeValueList(
                AttributeValue().withN(start.toEpochMilli().toString()),
                AttributeValue().withN(end.toEpochMilli().toString())
            )

        val expression = DynamoDBQueryExpression<DynamoMessage>()
            .withHashKeyValues(DynamoMessage(owner = user))
            .withScanIndexForward(true)
            .withRangeKeyCondition("id", between)

        return mapper.query(expression).map { it.toMessage() }
    }
}

