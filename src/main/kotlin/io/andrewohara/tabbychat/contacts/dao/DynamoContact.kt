package io.andrewohara.tabbychat.contacts.dao

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBRangeKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.contacts.Contact
import io.andrewohara.tabbychat.users.UserId
import io.andrewohara.lib.UserIdConverter

@DynamoDBDocument
data class DynamoContact(
    @DynamoDBHashKey
    @DynamoDBTypeConverted(converter = UserIdConverter::class)
    var owner: UserId? = null,

    @DynamoDBRangeKey
    @DynamoDBTypeConverted(converter = UserIdConverter::class)
    var id: UserId? = null,

    var tokenRealm: String? = null,
    var tokenValue: String? = null,
) {
    constructor(owner: UserId, contact: Contact): this(
        owner = owner,
        id = contact.userId,
        tokenRealm =contact.token.realm,
        tokenValue = contact.token.value,
    )

    fun toContact() = Contact(
        userId = id!!,
        token = AccessToken(realm = tokenRealm!!, value = tokenValue!!)
    )
}