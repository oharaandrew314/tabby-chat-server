package io.andrewohara.tabbychat.auth.dao

import io.andrewohara.dynamokt.DynamoKtConverted
import io.andrewohara.dynamokt.DynamoKtPartitionKey
import io.andrewohara.tabbychat.lib.dao.UserIdConverter
import io.andrewohara.tabbychat.users.UserId
import java.time.Instant

data class DynamoToken(
    @DynamoKtPartitionKey
    val value: String,

    val type: String,

    @DynamoKtConverted(UserIdConverter::class)
    val owner: UserId,

    @DynamoKtConverted(UserIdConverter::class)
    val contact: UserId?,

    val expires: Long?
)

fun DynamoToken.isExpired(time: Instant) = if (expires == null) false else {
    expires <= time.epochSecond
}