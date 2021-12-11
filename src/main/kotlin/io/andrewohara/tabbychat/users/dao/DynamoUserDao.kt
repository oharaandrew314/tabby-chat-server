package io.andrewohara.tabbychat.users.dao

import io.andrewohara.dynamokt.DynamoKtConverted
import io.andrewohara.dynamokt.DynamoKtPartitionKey
import io.andrewohara.tabbychat.lib.dao.UserIdConverter
import io.andrewohara.tabbychat.users.RealName
import io.andrewohara.tabbychat.users.User
import io.andrewohara.tabbychat.users.UserId
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import java.net.URL

class DynamoUserDao(private val mapper: DynamoDbTable<DynamoUser>): UsersDao {

    override fun get(userId: UserId): User? {
        val key = Key.builder().partitionValue(userId.toString()).build()
        val item = mapper.getItem(key) ?: return null
        return item.toUser()
    }

    override fun save(user: User) {
        mapper.putItem(user.toDynamo())
    }
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