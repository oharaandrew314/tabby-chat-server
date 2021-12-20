package io.andrewohara.tabbychat

import dev.forkhandles.result4k.*
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.AccessTokenGenerator
import io.andrewohara.tabbychat.auth.Realm
import io.andrewohara.tabbychat.auth.dao.AuthorizationDao
import io.andrewohara.tabbychat.contacts.Contact
import io.andrewohara.tabbychat.contacts.ContactsDao
import io.andrewohara.tabbychat.contacts.Authorization
import io.andrewohara.tabbychat.contacts.TokenData
import io.andrewohara.tabbychat.messages.*
import io.andrewohara.tabbychat.messages.dao.MessagesDao
import io.andrewohara.tabbychat.protocol.v1.client.P2PClientV1Factory
import io.andrewohara.tabbychat.users.RealName
import io.andrewohara.tabbychat.users.User
import io.andrewohara.tabbychat.users.UserId
import io.andrewohara.tabbychat.users.dao.UsersDao
import java.net.URL
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.*

class TabbyChatService(
    private val realm: Realm,
    private val messages: MessagesDao,
    private val auth: AuthorizationDao,
    private val users: UsersDao,
    private val contacts: ContactsDao,
    private val clientFactory: P2PClientV1Factory,
    private val clock: Clock,
    private val messagePageSize: Int,
    private val invitationDuration: Duration,
    private val nextToken: AccessTokenGenerator
) {
    fun saveMessage(userId: UserId, senderId: UserId, recipientId: UserId, content: MessageContent): Result<MessageReceipt, TabbyChatError> {
        val user = users[userId] ?: return TabbyChatError.NotFound.err()

        val message = Message(
            content = content,
            received = clock.instant(),
            sender = senderId,
            recipient = recipientId
        )
        messages.add(user.id, message)
        return Success(message.toReceipt())
    }

    fun sendMessage(userId: UserId, contactId: UserId, content: MessageContent): Result<MessageReceipt, TabbyChatError> {
        if (contactId == userId) return saveMessage(userId, userId, userId, content)

        val contact = contacts[userId, contactId] ?: return TabbyChatError.NotContact.err()

        val receipt = clientFactory(contact.accessToken()).sendMessage(content).onFailure { return it }
        messages.add(userId, receipt.toMessage(content))
        return Success(receipt)
    }

    fun listMessages(userId: UserId, since: Instant, limit: Int = messagePageSize): Result<MessagePage, TabbyChatError> {
        val results = messages.list(userId, since, limit)
        return Success(results)
    }

    fun listContacts(userId: UserId): Result<List<UserId>, TabbyChatError> {
        val results = contacts[userId].map { it.id }
        return Success(results)
    }

    fun deleteContact(ownerId: UserId, contactId: UserId): Result<Unit, TabbyChatError> {
        val contact = contacts[ownerId, contactId] ?: return TabbyChatError.NotContact.err()

        contacts -= contact
        clientFactory(contact.accessToken()).revokeContact()

        return Success(Unit)
    }

    fun createInvitation(userId: UserId): Result<TokenData, TabbyChatError> {
        val tokenData = TokenData(
            realm = realm,
            accessToken = nextToken(),
            expires = clock.instant() + invitationDuration
        )

        auth += Authorization(
            bearer = null,
            principal = userId,
            type = Authorization.Type.Invite,
            value = tokenData.accessToken,
            expires = tokenData.expires
        )

        return Success(tokenData)
    }

    fun createAccessToken(userId: UserId): Result<TokenData, TabbyChatError> {
        val tokenData = TokenData(
            accessToken = nextToken(),
            realm = realm,
            expires = null
        )

        auth += Authorization(
            type = Authorization.Type.User,
            bearer = userId,
            principal = userId,
            value = tokenData.accessToken,
            expires = tokenData.expires
        )

        return Success(tokenData)
    }

    fun acceptInvitation(userId: UserId, invitation: TokenData): Result<User, TabbyChatError> {
        val inviter = clientFactory(invitation).getUser()
            .onFailure { return it }

        val incomingToken = TokenData(
            accessToken = nextToken(),
            realm = realm,
            expires = null
        )
        auth += Authorization(
            type = Authorization.Type.Contact,
            bearer = inviter.id,
            principal = userId,
            value = incomingToken.accessToken,
            expires = incomingToken.expires
        )

        val outgoingToken = clientFactory(invitation).addContact(incomingToken)
            .onFailure { return it }

        contacts += Contact(
            ownerId = userId,
            id = inviter.id,
            tokenValue = outgoingToken.accessToken,
            realm = outgoingToken.realm,
            tokenExpires = outgoingToken.expires
        )

        return Success(inviter)
    }

    fun createContact(userId: UserId, contactToken: TokenData): Result<TokenData, TabbyChatError> {
        val contact = clientFactory(contactToken).getUser().onFailure { return it }

        val incoming = TokenData(
            accessToken = nextToken(),
            realm = realm,
            expires = null
        )

        contacts += Contact(
            ownerId = userId,
            id = contact.id,
            tokenValue = contactToken.accessToken,
            realm = contactToken.realm,
            tokenExpires = contactToken.expires
        )
        auth += Authorization(
            value = incoming.accessToken,
            type = Authorization.Type.Contact,
            principal = userId,
            bearer = contact.id,
            expires = incoming.expires
        )

        return Success(incoming)
    }

    fun createUser(name: RealName?, photo: URL?): User {
        val userId = UserId(UUID.randomUUID().toString())
        return User(userId, name, photo).also(users::plusAssign)
    }

    fun getUser(userId: UserId): Result<User, TabbyChatError> {
        val user = users[userId] ?: return TabbyChatError.NotFound.err()
        return Success(user)
    }

    fun getContact(userId: UserId, contactId: UserId): Result<User, TabbyChatError> {
        val contact = contacts[userId, contactId] ?: return TabbyChatError.NotFound.err()

        return clientFactory(contact.accessToken()).getUser()
    }

    fun authorize(accessToken: AccessToken): Authorization? {
        val auth = auth[accessToken] ?: return null
        if (auth.expires == null) return auth
        if (auth.expires < clock.instant()) return null

        users[auth.principal] ?: return null

        if (auth.type == Authorization.Type.Contact) {
            if (auth.bearer == null) return null
            contacts[auth.principal, auth.bearer] ?: return null
        }

        return auth
    }
}