package io.andrewohara.brownchat.users

import io.andrewohara.brownchat.users.dao.UsersDao
import java.net.URL
import java.util.*

class UserService(private val realm: String, private val dao: UsersDao) {

    fun create(name: RealName?, photo: URL?, id: String = UUID.randomUUID().toString()): User {
        val userId = UserId(realm = realm, id = id)
        return User(userId, name, photo).also(dao::save)
    }

    operator fun get(id: UserId) = dao[id]
}