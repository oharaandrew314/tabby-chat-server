package io.andrewohara.tabbychat.contacts

import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.users.UserId

data class Contact(
    val ownerId: UserId,
    val accessToken: AccessToken, // access to this provider
    val contactToken: TokenData // access to contact's provider
) {
    val id = contactToken.userId
}