package io.andrewohara.tabbychat.messages

import dev.forkhandles.result4k.Failure
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.andrewohara.tabbychat.ContactClient
import io.andrewohara.tabbychat.contacts.dao.ContactsDao
import io.andrewohara.tabbychat.messages.dao.MessageDao
import io.andrewohara.tabbychat.users.UserId
import java.time.Clock
import java.time.Instant

class MessageService(
    private val dao: MessageDao,
    private val contactsDao: ContactsDao,
    private val client: ContactClient,
    private val clock: Clock
    ) {

    /**
     * As the sender, send a message, saving a copy for yourself
     */
    fun send(sender: UserId, recipient: UserId, content: MessageContent): Result<Message, MessageError> {
        if (sender != recipient) {
            val contact = contactsDao[sender, recipient] ?: return MessageError.NotContact.err()
            client.sendMessage(contact, content)?.let { return Failure(it) }
        }

        val message = Message(
            content = content,
            received = clock.instant(),
            sender = sender
        )
        dao.add(sender, message)


        return Success(message)
    }

    /**
     * As a recipient, receive a message and save it
     */
    fun receive(sender: UserId, recipient: UserId, content: MessageContent) {
        val message = Message(
            content = content,
            received = clock.instant(),
            sender = sender
        )

        dao.add(recipient, message)
    }

    /**
     * As a registered user, get your messages
     */
    fun listMessages(user: UserId, start: Instant, end: Instant): List<Message> {
        return dao.list(user, start, end)
    }
}