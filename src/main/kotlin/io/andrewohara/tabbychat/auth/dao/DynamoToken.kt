package io.andrewohara.tabbychat.auth.dao

import io.andrewohara.dynamokt.DynamoKtConverted
import io.andrewohara.dynamokt.DynamoKtPartitionKey
import io.andrewohara.dynamokt.DynamoKtSecondaryPartitionKey
import io.andrewohara.dynamokt.DynamoKtSecondarySortKey
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.Realm
import io.andrewohara.tabbychat.lib.dao.AccessTokenConverter
import io.andrewohara.tabbychat.lib.dao.RealmConverter
import io.andrewohara.tabbychat.lib.dao.UserIdConverter
import io.andrewohara.tabbychat.users.UserId
import java.time.Instant

data class DynamoToken(
    @DynamoKtPartitionKey
    @DynamoKtConverted(AccessTokenConverter::class)
    val value: AccessToken,

    val type: Type,
    val expires: Long?,

    @DynamoKtSecondaryPartitionKey([contactsIndexName])
    @DynamoKtConverted(UserIdConverter::class)
    val owner: UserId,
    @DynamoKtSecondarySortKey([contactsIndexName])
    @DynamoKtConverted(UserIdConverter::class)
    val contact: UserId?,

    @DynamoKtConverted(RealmConverter::class)
    val contactRealm: Realm?,
    @DynamoKtConverted(AccessTokenConverter::class)
    val contactToken: AccessToken?,
) {
    companion object {
        const val contactsIndexName = "contacts"
    }

    enum class Type { Contact, User, Invitation }
}

fun DynamoToken.isExpired(time: Instant) = if (expires == null) false else {
    expires <= time.epochSecond
}