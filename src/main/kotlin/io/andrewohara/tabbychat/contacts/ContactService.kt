package io.andrewohara.tabbychat.contacts

import dev.forkhandles.result4k.*
import io.andrewohara.tabbychat.ContactClient
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.dao.TokenDao
import io.andrewohara.tabbychat.contacts.dao.ContactsDao
import io.andrewohara.tabbychat.users.UserId
import java.time.Clock
import java.time.Duration

class ContactService(
    private val contactsDao: ContactsDao,
    private val tokensDao: TokenDao,
    private val client: ContactClient,
    private val clock: Clock
) {

    companion object {
        private val invitationCodeExpiry = Duration.ofHours(1)
    }

    fun createInvitation(owner: UserId): AccessToken {
        return tokensDao.generateInvitationCode(owner, clock.instant() + invitationCodeExpiry)
    }

    /**
     * As the user who received an invitation, accept it and add the inviter as a contact.
     */
    fun acceptInvitation(self: UserId, invitation: AccessToken): Result<Unit, ContactError> {
        val inviter = client.getUser(invitation)
            .onFailure { return it }

        if (contactsDao[self, inviter.id] != null) return ContactError.AlreadyContact.err()

        val selfToken = tokensDao.generateContactToken(owner = self, contact = inviter.id)
        val inviterToken = client.completeInvitation(invitation, selfToken)
            .onFailure { return it }

        // test inviter token (and reject the one you gave if it doesn't work)
        client.getUser(inviterToken)
            .map { if (it.id == inviter.id) it else ContactError.InvitationRejected.err() }  // TODO come up with better error code
            .onFailure {
                tokensDao.revoke(selfToken)
                return it
            }

        contactsDao.save(self, Contact(inviter.id, inviterToken))

        return Success(Unit)
    }

    /**
     * As the user who sent an invitation, add the accepting user as a contact, and return your own access token
     */
    fun completeInvitation(self: UserId, invitation: AccessToken, contactToken: AccessToken): Result<AccessToken, ContactError> {
        val contact = client.getUser(contactToken)
            .onFailure { return it }

        contactsDao.save(self, Contact(userId = contact.id, token = contactToken))
        tokensDao.revoke(invitation)

        // allow contact to send messages to self
        val token = tokensDao.generateContactToken(owner = self, contact = contact.id)

        return Success(token)
    }

    fun listContacts(owner: UserId): List<Contact> {
        return contactsDao.list(owner)
    }

    fun deleteContact(owner: UserId, contact: UserId) {
        contactsDao.delete(owner, contact)
    }
}