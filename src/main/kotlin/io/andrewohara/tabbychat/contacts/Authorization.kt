package io.andrewohara.tabbychat.contacts

import io.andrewohara.dynamokt.DynamoKtConverted
import io.andrewohara.dynamokt.DynamoKtPartitionKey
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.lib.dao.AccessTokenConverter
import io.andrewohara.tabbychat.lib.dao.InstantAsLongConverter
import io.andrewohara.tabbychat.lib.dao.UserIdConverter
import io.andrewohara.tabbychat.users.UserId
import java.time.Instant

data class Authorization(
    @DynamoKtPartitionKey
    @DynamoKtConverted(AccessTokenConverter::class)
    val value: AccessToken,

    val type: Type,

    @DynamoKtConverted(UserIdConverter::class)
    val bearer: UserId?,

    @DynamoKtConverted(UserIdConverter::class)
    val principal: UserId,

    @DynamoKtConverted(InstantAsLongConverter::class)
    val expires: Instant?
) {
    enum class Type { Contact, User, Invite, Login }
}