package io.andrewohara.tabbychat.contacts

import io.andrewohara.dynamokt.DynamoKtConverted
import io.andrewohara.dynamokt.DynamoKtPartitionKey
import io.andrewohara.dynamokt.DynamoKtSortKey
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.Realm
import io.andrewohara.tabbychat.lib.dao.AccessTokenConverter
import io.andrewohara.tabbychat.lib.dao.RealmConverter
import io.andrewohara.tabbychat.lib.dao.UserIdConverter
import io.andrewohara.tabbychat.users.UserId
import java.time.Instant

data class Contact(
    @DynamoKtPartitionKey
    @DynamoKtConverted(UserIdConverter::class)
    val ownerId: UserId,

    @DynamoKtSortKey
    @DynamoKtConverted(UserIdConverter::class)
    val id: UserId,

    @DynamoKtConverted(AccessTokenConverter::class)
    val tokenValue: AccessToken,

    @DynamoKtConverted(RealmConverter::class)
    val realm: Realm,

    val tokenExpires: Instant?
) {
    fun accessToken() = TokenData(
        accessToken = tokenValue,
        realm = realm,
        expires = tokenExpires
    )
}