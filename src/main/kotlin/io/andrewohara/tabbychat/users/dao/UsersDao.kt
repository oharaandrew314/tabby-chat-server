package io.andrewohara.tabbychat.users.dao

import io.andrewohara.tabbychat.users.*
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key

class UsersDao(private val mapper: DynamoDbTable<User>): Iterable<User> {

    operator fun get(userId: UserId): User? {
        val key = Key.builder().partitionValue(userId.value).build()
        return mapper.getItem(key)
    }

    operator fun plusAssign(user: User) {
        mapper.putItem(user)
    }

    override fun iterator() = mapper.scan()
        .items()
        .iterator()
}