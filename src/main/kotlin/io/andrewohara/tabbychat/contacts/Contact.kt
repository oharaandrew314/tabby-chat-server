package io.andrewohara.tabbychat.contacts

import io.andrewohara.dynamokt.DynamoKtConverted
import io.andrewohara.dynamokt.DynamoKtPartitionKey
import io.andrewohara.dynamokt.DynamoKtSortKey
import io.andrewohara.tabbychat.lib.dao.UserIdConverter
import io.andrewohara.tabbychat.users.UserId

data class Contact(
    @DynamoKtPartitionKey
    @DynamoKtConverted(UserIdConverter::class)
    val ownerId: UserId,

    @DynamoKtSortKey
    @DynamoKtConverted(UserIdConverter::class)
    val id: UserId,

    val tokenData: TokenData,
)