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

    fun createContact(owner: UserId, accessToken: AccessToken, contactToken: TokenData): Contact {
        val item = DynamoToken(
            value = accessToken,
            type = DynamoToken.Type.Contact,
            owner = owner,
            contact = contactToken.userId,
            expires = contactToken.expires?.epochSecond,
            contactToken = contactToken.token,
            contactRealm = contactToken.realm
        )

        mapper.putItem(item)
        return Contact(
            ownerId = owner,
            accessToken = accessToken,
            contactToken = contactToken
        )
    }

    fun saveInvitation(token: TokenData) {
        val item = DynamoToken(
            value = token.token,
            type = DynamoToken.Type.Invitation,
            owner = token.userId,
            contact = null,
            expires = token.expires?.epochSecond,
            contactRealm = null,
            contactToken = null
        )

        mapper.putItem(item)
    }

    fun saveUserToken(owner: UserId, accessToken: AccessToken) {
        val item = DynamoToken(
            value = accessToken,
            type = DynamoToken.Type.User,
            owner = owner,
            contact = null,
            expires = null,
            contactRealm = null,
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
            .map { token -> token.toContact() }
            .toList()
    }

    fun getContact(owner: UserId, contact: UserId): Contact? {
        return mapper
            .index(DynamoToken.contactsIndexName)
            .query(QueryConditional.keyEqualTo(owner.toKey(contact)))
            .asSequence()
            .flatMap { it.items() }
            .filter { it.type == DynamoToken.Type.Contact }
            .map { token -> token.toContact() }
            .firstOrNull()
    }

    private fun DynamoToken.toContact() = Contact(
        ownerId = owner,
        accessToken = value,
        contactToken = TokenData(
            userId = contact!!,
            realm = contactRealm!!,
            token = contactToken!!,
            expires = expires?.let { Instant.ofEpochSecond(it) }
        )
    )

    private fun AccessToken.toKey() = Key.builder().partitionValue(value).build()
}