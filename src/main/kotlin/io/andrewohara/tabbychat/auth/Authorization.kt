package io.andrewohara.tabbychat.auth

import io.andrewohara.tabbychat.contacts.TokenData
import io.andrewohara.tabbychat.users.UserId

sealed interface Authorization {

    data class Owner(val owner: UserId): Authorization

    data class Invite(val owner: UserId, val token: AccessToken): Authorization

    data class Contact(val owner: UserId, val contact: UserId, val token: AccessToken): Authorization
}

fun Authorization.Invite.toInvitation() = TokenData(userId = owner, token = token)