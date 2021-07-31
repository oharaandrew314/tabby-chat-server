package io.andrewohara.brownchat.auth.dao

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTableMapper
import io.andrewohara.brownchat.auth.Authorization
import io.andrewohara.brownchat.auth.AccessToken
import io.andrewohara.brownchat.users.UserId
import java.math.BigInteger
import java.security.SecureRandom
import java.time.Instant

class DynamoTokenDao(private val mapper: DynamoDBTableMapper<DynamoToken, String, Unit>, private val realm: String): TokenDao {

    private object RandomToken {
        private const val BASE32_RADIX = 32
        private val random = SecureRandom()

        operator fun invoke(): String = BigInteger(130, random).toString(BASE32_RADIX)
    }

    override fun generateContactToken(owner: UserId, contact: UserId): AccessToken {
        val item = DynamoToken(
            value = RandomToken(),
            type = "contact",
            owner = owner,
            contact = contact,
            expires = null
        )

        mapper.save(item)
        return AccessToken(realm = realm, value = item.value!!)
    }

    override fun generateInvitationCode(owner: UserId, expires: Instant): AccessToken {
        val item = DynamoToken(
            value = RandomToken(),
            type = "invitation",
            owner = owner,
            contact = null,
            expires = expires
        )

        mapper.save(item)
        return AccessToken(realm = realm, value = item.value!!)
    }

    override fun generateUserToken(owner: UserId): AccessToken {
        val item = DynamoToken(
            value = RandomToken(),
            type = "user",
            owner = owner,
            contact = null,
            expires = null
        )

        mapper.save(item)
        return AccessToken(realm = realm, value = item.value!!)
    }

    override fun verify(token: AccessToken, time: Instant): Authorization? {
        if (token.realm != realm) return null

        val item = mapper.load(token.value) ?: return null
        if (item.isExpired(time)) return null

        return when(item.type) {
            "contact" -> Authorization.Contact(
                owner = item.owner!!,
                contact = item.contact!!
            )
            "user" -> Authorization.Owner(
                owner = item.owner!!
            )
            "invitation" -> Authorization.Invite(
                owner = item.owner!!,
                invitation = token
            )
            else -> null
        }
    }

    override fun revoke(token: AccessToken) {
        mapper.delete(DynamoToken(value = token.value))
    }
}