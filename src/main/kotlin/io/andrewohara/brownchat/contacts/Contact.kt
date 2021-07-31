package io.andrewohara.brownchat.contacts

import io.andrewohara.brownchat.auth.AccessToken
import io.andrewohara.brownchat.users.UserId

data class Contact(
    val userId: UserId,
    val token: AccessToken
)