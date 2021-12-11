package io.andrewohara.tabbychat.auth.dao

import io.andrewohara.tabbychat.auth.Authorization
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.users.UserId
import io.andrewohara.utils.IdGenerator
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import java.time.Instant

class DynamoTokenDao(
    private val mapper: DynamoDbTable<DynamoToken>,
    private val realm: String
): TokenDao {

    private fun randomToken() = IdGenerator.nextBase36(16)

    override fun generateContactToken(owner: UserId, contact: UserId): AccessToken {
        val item = DynamoToken(
            value = randomToken(),
            type = "contact",
            owner = owner,
            contact = contact,
            expires = null
        )

        mapper.putItem(item)
        return AccessToken(realm = realm, value = item.value)
    }

    override fun generateInvitationCode(owner: UserId, expires: Instant): AccessToken {
        val item = DynamoToken(
            value = randomToken(),
            type = "invitation",
            owner = owner,
            contact = null,
            expires = expires.epochSecond
        )

        mapper.putItem(item)
        return AccessToken(realm = realm, value = item.value)
    }

    override fun generateUserToken(owner: UserId): AccessToken {
        val item = DynamoToken(
            value = randomToken(),
            type = "user",
            owner = owner,
            contact = null,
            expires = null
        )

        mapper.putItem(item)
        return AccessToken(realm = realm, value = item.value)
    }

    override fun verify(token: AccessToken, time: Instant): Authorization? {
        if (token.realm != realm) return null

        val item = mapper.getItem(Key.builder().partitionValue(token.value).build())
            ?: return null
        if (item.isExpired(time)) return null

        return when(item.type) {
            "contact" -> Authorization.Contact(
                owner = item.owner,
                contact = item.contact!!
            )
            "user" -> Authorization.Owner(
                owner = item.owner
            )
            "invitation" -> Authorization.Invite(
                owner = item.owner,
                invitation = token
            )
            else -> null
        }
    }

    override fun revoke(token: AccessToken) {
        val key = Key.builder().partitionValue(token.value).build()
        mapper.deleteItem(key)
    }
}