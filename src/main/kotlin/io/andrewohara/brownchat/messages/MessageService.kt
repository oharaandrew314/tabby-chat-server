package io.andrewohara.brownchat.messages

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.andrewohara.brownchat.ContactClient
import io.andrewohara.brownchat.contacts.dao.ContactsDao
import io.andrewohara.brownchat.messages.dao.MessageDao
import io.andrewohara.brownchat.users.UserId
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
        val contact = contactsDao[sender, recipient] ?: return Err(MessageError.NotContact(recipient))

        val message = Message(
            content = content,
            received = clock.instant(),
            sender = sender
        )

        dao.add(sender, message)
        client.sendMessage(contact, content)?.let { return Err(it) }

        return Ok(message)
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