package io.andrewohara.tabbychat.contacts

import io.andrewohara.tabbychat.lib.dao.toKey
import io.andrewohara.tabbychat.users.UserId
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional

class ContactsDao(private val table: DynamoDbTable<Contact>) {

    operator fun get(ownerId: UserId): List<Contact> = table
        .query(QueryConditional.keyEqualTo(ownerId.toKey()))
        .items()
        .toList()

    operator fun get(ownerId: UserId, id: UserId): Contact? = table
        .query(QueryConditional.keyEqualTo(ownerId.toKey(id)))
        .items()
        .firstOrNull()

    operator fun plusAssign(contact: Contact) {
        table.putItem(contact)
    }

    operator fun minusAssign(contact: Contact) {
        table.deleteItem(contact)
    }
}