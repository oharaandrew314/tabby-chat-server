package io.andrewohara.tabbychat.users

data class UserId(val realm: String, val id: String) {
    override fun toString() = "$realm:$id"
}