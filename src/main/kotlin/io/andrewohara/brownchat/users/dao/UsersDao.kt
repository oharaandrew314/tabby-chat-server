package io.andrewohara.brownchat.users.dao

import io.andrewohara.brownchat.users.User
import io.andrewohara.brownchat.users.UserId

interface UsersDao {
    operator fun get(userId: UserId): User?
    fun save(user: User)
}