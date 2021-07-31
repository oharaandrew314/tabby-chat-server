package io.andrewohara.brownchat.auth.dao

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import io.andrewohara.brownchat.users.UserId
import io.andrewohara.lib.EpochInstantConverter
import io.andrewohara.lib.UserIdConverter
import java.time.Instant

@DynamoDBDocument
data class DynamoToken(
    @DynamoDBHashKey
    var value: String? = null,

    var type: String? = null,

    @DynamoDBTypeConverted(converter = UserIdConverter::class)
    var owner: UserId? = null,

    @DynamoDBTypeConverted(converter = UserIdConverter::class)
    var contact: UserId? = null,

    @DynamoDBTypeConverted(converter = EpochInstantConverter::class)
    var expires: Instant? = null
) {
    fun isExpired(time: Instant) = expires
        ?.let { expiry -> expiry <= time }
        ?: false
}