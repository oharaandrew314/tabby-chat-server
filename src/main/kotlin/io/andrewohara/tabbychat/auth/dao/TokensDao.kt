package io.andrewohara.tabbychat.auth.dao

import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.Authorization
import io.andrewohara.tabbychat.contacts.Contact
import io.andrewohara.tabbychat.contacts.TokenData
import io.andrewohara.tabbychat.lib.dao.toKey
import io.andrewohara.tabbychat.users.UserId
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import java.time.Instant

class TokensDao(private val mapper: DynamoDbTable<DynamoToken>) {

    fun createContact(incoming: TokenData, outgoing: TokenData) = createContact(
        owner = incoming.userId,
        contact = outgoing.userId,
        accessToken = incoming.token,
        contactToken = outgoing.token
    )

    fun createContact(owner: UserId, contact: UserId, accessToken: AccessToken, contactToken: AccessToken): Contact {
        val item = DynamoToken(
            value = accessToken,
            type = DynamoToken.Type.Contact,
            owner = owner,
            contact = contact,
            expires = null,
            contactToken = contactToken
        )

        mapper.putItem(item)
        return Contact(
            ownerId = owner,
            id = contact,
            accessToken = accessToken,
            contactToken = contactToken
        )
    }

    fun saveInvitation(owner: UserId, expires: Instant, accessToken: AccessToken): TokenData {
        val item = DynamoToken(
            value = accessToken,
            type = DynamoToken.Type.Invitation,
            owner = owner,
            contact = null,
            expires = expires.epochSecond,
            contactToken = null
        )

        mapper.putItem(item)

        return TokenData(
            token = accessToken,
            userId = owner
        )
    }

    fun saveUserToken(owner: UserId, accessToken: AccessToken) {
        val item = DynamoToken(
            value = accessToken,
            type = DynamoToken.Type.User,
            owner = owner,
            contact = null,
            expires = null,
            contactToken = null
        )

        mapper.putItem(item)
    }

    fun verify(token: AccessToken, time: Instant): Authorization? {
        val item = mapper.getItem(token.toKey()) ?: return null
        if (item.isExpired(time)) return null

        return when(item.type) {
            DynamoToken.Type.Contact -> {
                Authorization.Contact(
                    owner = item.owner,
                    contact = item.contact!!,
                    token = item.value
                )
            }
            DynamoToken.Type.User -> Authorization.Owner(owner = item.owner)
            DynamoToken.Type.Invitation -> Authorization.Invite(
                owner = item.owner,
                token = token
            )
        }
    }

    fun revoke(token: AccessToken): Boolean {
        return mapper.deleteItem(token.toKey()) != null
    }

    fun listContacts(owner: UserId): List<Contact> {
        return mapper
            .index(DynamoToken.contactsIndexName)
            .query(QueryConditional.keyEqualTo(owner.toKey()))
            .asSequence()
            .flatMap { it.items() }
            .filter { it.type == DynamoToken.Type.Contact }
            .map { token -> Contact(
                ownerId = owner,
                id = token.contact!!,
                accessToken = token.value,
                contactToken = token.contactToken!!
            ) }
            .toList()
    }

    fun getContact(owner: UserId, contact: UserId): Contact? {
        return mapper
            .index(DynamoToken.contactsIndexName)
            .query(QueryConditional.keyEqualTo(owner.toKey(contact)))
            .asSequence()
            .flatMap { it.items() }
            .filter { it.type == DynamoToken.Type.Contact }
            .map { token -> Contact(
                ownerId = owner,
                id = token.contact!!,
                accessToken = token.value,
                contactToken = token.contactToken!!
            ) }
            .firstOrNull()
    }

    private fun AccessToken.toKey() = Key.builder().partitionValue(value).build()
}