package io.andrewohara.tabbychat.users.dao

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTableMapper
import io.andrewohara.tabbychat.users.User
import io.andrewohara.tabbychat.users.UserId

class DynamoUserDao(private val mapper: DynamoDBTableMapper<DynamoUser, UserId, Unit>): UsersDao {

    override fun get(userId: UserId): User? {
        val item = mapper.load(userId) ?: return null
        return item.toUser()
    }

    override fun save(user: User) {
        val item = DynamoUser(user)
        mapper.save(item)
    }
}