package io.andrewohara.brownchat.auth

import io.andrewohara.brownchat.users.UserId

sealed interface Authorization {
    data class Owner(val owner: UserId): Authorization
    data class Invite(val owner: UserId, val invitation: AccessToken): Authorization
    data class Contact(val owner: UserId, val contact: UserId): Authorization
}