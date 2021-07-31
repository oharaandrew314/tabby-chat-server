package io.andrewohara.tabbychat.messages.dao

import io.andrewohara.tabbychat.messages.Message
import io.andrewohara.tabbychat.users.UserId
import java.time.Instant

interface MessageDao {

    fun add(owner: UserId, message: Message)
    fun list(user: UserId, start: Instant, end: Instant): List<Message>
}