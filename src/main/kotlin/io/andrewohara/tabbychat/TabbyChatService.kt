package io.andrewohara.tabbychat

import dev.forkhandles.result4k.*
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.AccessTokenGenerator
import io.andrewohara.tabbychat.auth.Realm
import io.andrewohara.tabbychat.auth.dao.TokensDao
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
    private val tokens: TokensDao,
    private val users: UsersDao,
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
        val user = users[userId] ?: return TabbyChatError.NotFound.err()
        if (contactId == userId) return saveMessage(userId, userId, userId, content)

        val contact = tokens.getContact(user.id, contactId) ?: return TabbyChatError.NotContact.err()

        val receipt = clientFactory(contact.contactToken).sendMessage(content).onFailure { return it }
        messages.add(userId, receipt.toMessage(content))
        return Success(receipt)
    }

    fun listMessages(userId: UserId, since: Instant): Result<MessagePage, TabbyChatError> {
        val user = users[userId] ?: return TabbyChatError.NotFound.err()

        val results = messages.list(user.id, since, messagePageSize)
        return Success(results)
    }

    fun listContacts(userId: UserId): Result<List<UserId>, TabbyChatError> {
        val user = users[userId] ?: return TabbyChatError.NotFound.err()

        val results = tokens.listContacts(user.id).map { it.id }
        return Success(results)
    }

    fun deleteContact(ownerId: UserId, contactId: UserId): Result<Unit, TabbyChatError> {
        val contact = tokens.getContact(ownerId, contactId) ?: return TabbyChatError.NotContact.err()

        val deleted = tokens.revoke(contact.accessToken)
        if (deleted) clientFactory(contact.contactToken).revokeContact()

        return Success(Unit)
    }

    fun createInvitation(userId: UserId): Result<TokenData, TabbyChatError> {
        val user = users[userId] ?: return TabbyChatError.NotFound.err()

        val tokenData = TokenData(
            userId = user.id,
            realm = realm,
            token = nextToken(),
            expires = clock.instant() + invitationDuration
        )

        tokens.saveInvitation(tokenData)

        return Success(tokenData)
    }

    fun createAccessToken(userId: UserId): Result<TokenData, TabbyChatError> {
        val user = users[userId] ?: return TabbyChatError.NotFound.err()

        val token = nextToken()
        tokens.saveUserToken(user.id, token)
        return Success(TokenData(token, user.id, realm, null))
    }

    fun acceptInvitation(userId: UserId, invitation: TokenData): Result<User, TabbyChatError> {
        val accessToken = TokenData(nextToken(), userId, realm, null)

        val contactToken = clientFactory(invitation).exchangeTokens(accessToken)
            .onFailure { return it }

        tokens.createContact(owner = userId, accessToken = accessToken.token, contactToken = contactToken)

        return clientFactory(contactToken).getUser()
    }

    fun exchangeToken(userId: UserId, existingToken: AccessToken, incomingToken: TokenData): Result<TokenData, TabbyChatError> {
        tokens.revoke(existingToken)

        val contact = tokens.createContact(
            owner = userId,
            accessToken =  nextToken(),
            contactToken = incomingToken
        )

        return Success(TokenData(contact.accessToken, userId, realm, null))
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
        val user = users[userId] ?: return TabbyChatError.NotFound.err()
        val contact = tokens.getContact(user.id, contactId) ?: return TabbyChatError.NotFound.err()

        return clientFactory(contact.contactToken).getUser()
    }
}