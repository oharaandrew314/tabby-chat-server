package io.andrewohara.brownchat.contacts

import com.github.michaelbull.result.*
import io.andrewohara.brownchat.ContactClient
import io.andrewohara.brownchat.auth.AccessToken
import io.andrewohara.brownchat.auth.dao.TokenDao
import io.andrewohara.brownchat.contacts.dao.ContactsDao
import io.andrewohara.brownchat.users.UserId
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
    fun acceptInvitation(self: UserId, invitation: AccessToken): Result<Unit?, ContactError> {
        val inviterId = client.getUser(invitation).getOrElse { return Err(it) }.id

        if (contactsDao[self, inviterId] != null) return Err(ContactError.AlreadyContact)

        val selfToken = tokensDao.generateContactToken(owner = self, contact = inviterId)
        val inviterToken = client.completeInvitation(invitation, selfToken)
            .getOrElse { return Err(it) }

        // test inviter token (and reject the one you gave if it doesn't work)
        client.getUser(inviterToken)
            .map { if (it.id == inviterId) it else Err(ContactError.InvitationRejected) }  // TODO come up with better error code
            .getOrElse {
                tokensDao.revoke(selfToken)
                return Err(it)
            }

        contactsDao.save(self, Contact(inviterId, inviterToken))

        return Ok(null)
    }

    /**
     * As the user who sent an invitation, add the accepting user as a contact, and return your own access token
     */
    fun completeInvitation(self: UserId, invitation: AccessToken, contactToken: AccessToken): Result<AccessToken, ContactError> {
        val contactId = client.getUser(contactToken)
            .getOrElse { return Err(it) }
            .id

        contactsDao.save(self, Contact(userId = contactId, token = contactToken))
        tokensDao.revoke(invitation)

        // allow contact to send messages to self
        val token = tokensDao.generateContactToken(owner = self, contact = contactId)
        return Ok(token)
    }

    fun listContacts(owner: UserId): List<Contact> {
        return contactsDao.list(owner)
    }

    fun deleteContact(owner: UserId, contact: UserId) {
        contactsDao.delete(owner, contact)
    }
}