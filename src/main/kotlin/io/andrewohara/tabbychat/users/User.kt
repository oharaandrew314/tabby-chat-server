package io.andrewohara.tabbychat.users

import io.andrewohara.dynamokt.DynamoKtConverted
import io.andrewohara.dynamokt.DynamoKtPartitionKey
import io.andrewohara.tabbychat.lib.dao.UserIdConverter

data class User(
    @DynamoKtConverted(UserIdConverter::class)
    @DynamoKtPartitionKey
    val id: UserId,
    val name: RealName,
    val icon: String?
)