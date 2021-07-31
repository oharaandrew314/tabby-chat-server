package io.andrewohara.tabbychat.users.dao

import io.andrewohara.tabbychat.users.User
import io.andrewohara.tabbychat.users.UserId

interface UsersDao {
    operator fun get(userId: UserId): User?
    fun save(user: User)
}