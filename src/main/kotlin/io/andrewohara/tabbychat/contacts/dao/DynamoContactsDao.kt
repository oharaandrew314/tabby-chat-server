package io.andrewohara.tabbychat.contacts.dao

import io.andrewohara.tabbychat.contacts.Contact
import io.andrewohara.tabbychat.lib.dao.toKey
import io.andrewohara.tabbychat.users.UserId
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional

class DynamoContactsDao(private val mapper: DynamoDbTable<DynamoContact>): ContactsDao {

    override fun save(owner: UserId, contact: Contact) {
        val item = contact.toDynamo(owner)
        mapper.putItem(item)
    }

    override fun list(owner: UserId): List<Contact> {
        return mapper.query(QueryConditional.keyEqualTo(owner.toKey()))
            .items()
            .map { it.toContact() }
    }

    override fun get(owner: UserId, contact: UserId): Contact? {
        return mapper.getItem(owner.toKey(contact))?.toContact()
    }

    override fun delete(owner: UserId, contact: UserId) {
        mapper.deleteItem(owner.toKey(contact))
    }
}