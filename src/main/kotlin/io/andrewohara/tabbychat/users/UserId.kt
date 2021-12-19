package io.andrewohara.tabbychat.users

import io.andrewohara.tabbychat.auth.Realm
import java.util.*

@JvmInline value class UserId(val value: String) {

    private fun parts() = value.split(":")

    val realm: Realm
        get() = Realm(parts().first())

    val id: String
        get() = parts().last()

    companion object {
        fun create(realm: Realm, id: String = UUID.randomUUID().toString()) = UserId("${realm.value}:$id")
    }
}