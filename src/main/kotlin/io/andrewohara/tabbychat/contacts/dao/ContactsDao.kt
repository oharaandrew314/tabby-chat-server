package io.andrewohara.tabbychat.contacts.dao

import io.andrewohara.tabbychat.contacts.Contact
import io.andrewohara.tabbychat.users.UserId

interface ContactsDao {
    fun save(owner: UserId, contact: Contact)
    fun list(owner: UserId): List<Contact>
    operator fun get(owner: UserId, contact: UserId): Contact?
    fun delete(owner: UserId, contact: UserId)
}