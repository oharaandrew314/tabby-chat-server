package io.andrewohara.tabbychat.users.dao

import io.andrewohara.dynamokt.DynamoKtConverted
import io.andrewohara.dynamokt.DynamoKtPartitionKey
import io.andrewohara.tabbychat.lib.dao.UserIdConverter
import io.andrewohara.tabbychat.users.*
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import java.net.URL

class UsersDao(private val mapper: DynamoDbTable<DynamoUser>): Iterable<User> {

    operator fun get(userId: UserId): User? {
        val key = Key.builder().partitionValue(userId.value).build()
        return mapper.getItem(key)?.toUser()
    }

    operator fun plusAssign(user: User) {
        mapper.putItem(user.toDynamo())
    }

    override fun iterator() = mapper.scan()
        .items()
        .map { it.toUser() }
        .iterator()
}

private fun DynamoUser.toUser() = User(
    id = id,
    name = firstName?.let { RealName(it, middleName, lastName) },
    icon = photoUrl?.let { URL(it) }
)

private fun User.toDynamo() = DynamoUser(
    id = id,
    firstName = name?.first,
    middleName = name?.middle,
    lastName = name?.last,
    photoUrl = icon?.toString(),
)

data class DynamoUser(
    @DynamoKtPartitionKey
    @DynamoKtConverted(UserIdConverter::class)
    val id: UserId,

    val firstName: String?,
    val middleName: String?,
    val lastName: String?,
    val photoUrl: String?
)