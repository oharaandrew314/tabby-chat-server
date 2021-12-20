package io.andrewohara.tabbychat.contacts

import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.Realm
import io.andrewohara.tabbychat.users.UserId
import java.time.Instant

data class TokenData(
    val token: AccessToken,
    val userId: UserId,
    val realm: Realm,
    val expires: Instant?
)