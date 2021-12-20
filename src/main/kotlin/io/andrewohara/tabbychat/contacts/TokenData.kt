package io.andrewohara.tabbychat.contacts

import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.Realm
import java.time.Instant

data class TokenData(
    val accessToken: AccessToken,
    val realm: Realm,
    val expires: Instant?,
)