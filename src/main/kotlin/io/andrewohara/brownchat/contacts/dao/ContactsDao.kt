package io.andrewohara.brownchat.contacts.dao

import io.andrewohara.brownchat.contacts.Contact
import io.andrewohara.brownchat.users.UserId

interface ContactsDao {
    fun save(owner: UserId, contact: Contact)
    fun list(owner: UserId): List<Contact>
    operator fun get(owner: UserId, contact: UserId): Contact?
    fun delete(owner: UserId, contact: UserId)
}