package io.andrewohara.tabbychat.contacts

import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.users.UserId

data class Contact(
    val ownerId: UserId,
    val id: UserId,
    val accessToken: AccessToken, // access to this provider
    val contactToken: AccessToken // access to contact's provider
)