package io.andrewohara.tabbychat.contacts

import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.users.UserId

data class TokenData(
    val token: AccessToken,
    val userId: UserId
)