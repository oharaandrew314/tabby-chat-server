package io.andrewohara.tabbychat.contacts.dao

import io.andrewohara.dynamokt.DynamoKtConverted
import io.andrewohara.dynamokt.DynamoKtPartitionKey
import io.andrewohara.dynamokt.DynamoKtSortKey
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.contacts.Contact
import io.andrewohara.tabbychat.lib.dao.UserIdConverter
import io.andrewohara.tabbychat.users.UserId

data class DynamoContact(
    @DynamoKtPartitionKey
    @DynamoKtConverted(converter = UserIdConverter::class)
    val owner: UserId,

    @DynamoKtSortKey
    @DynamoKtConverted(converter = UserIdConverter::class)
    val id: UserId,

    val tokenRealm: String,
    val tokenValue: String,
)

fun DynamoContact.toContact() = Contact(
    userId = id,
    token = AccessToken(realm = tokenRealm, value = tokenValue)
)

fun Contact.toDynamo(owner: UserId) = DynamoContact(
    owner = owner,
    id = userId,
    tokenRealm =token.realm,
    tokenValue = token.value,
)