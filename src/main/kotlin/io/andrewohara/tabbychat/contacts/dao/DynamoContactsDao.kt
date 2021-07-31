package io.andrewohara.tabbychat.contacts.dao

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTableMapper
import io.andrewohara.tabbychat.contacts.Contact
import io.andrewohara.tabbychat.users.UserId

class DynamoContactsDao(private val mapper: DynamoDBTableMapper<DynamoContact, UserId, UserId>): ContactsDao {

    override fun save(owner: UserId, contact: Contact) {
        val item = DynamoContact(owner, contact)
        mapper.save(item)
    }

    override fun list(owner: UserId): List<Contact> {
        val query = DynamoDBQueryExpression<DynamoContact>()
            .withHashKeyValues(DynamoContact(owner = owner))

        return mapper.query(query).map { it.toContact() }
    }

    override fun get(owner: UserId, contact: UserId): Contact? {
        return mapper.load(owner, contact)?.toContact()
    }

    override fun delete(owner: UserId, contact: UserId) {
        mapper.delete(DynamoContact(owner = owner, id = contact))
    }
}