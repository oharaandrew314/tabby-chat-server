package io.andrewohara.tabbychat.contacts

import io.andrewohara.dynamokt.DynamoKtConverted
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.Realm
import io.andrewohara.tabbychat.lib.dao.AccessTokenConverter
import io.andrewohara.tabbychat.lib.dao.RealmConverter
import java.time.Instant

data class TokenData(
    @DynamoKtConverted(AccessTokenConverter::class)
    val accessToken: AccessToken,

    @DynamoKtConverted(RealmConverter::class)
    val realm: Realm,

    val expires: Instant?,
)