package io.andrewohara.tabbychat.users

import io.andrewohara.tabbychat.users.dao.UsersDao
import java.net.URL

class UserService(private val realm: String, private val dao: UsersDao) {

    fun create(name: RealName?, photo: URL?, id: String): User {
        val userId = UserId(realm = realm, id = id)
        return User(userId, name, photo).also(dao::save)
    }

    operator fun get(id: UserId) = dao[id]
}