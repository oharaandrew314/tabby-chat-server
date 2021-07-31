package io.andrewohara.brownchat.messages.dao

import io.andrewohara.brownchat.messages.Message
import io.andrewohara.brownchat.users.UserId
import java.time.Instant

interface MessageDao {

    fun add(owner: UserId, message: Message)
    fun list(user: UserId, start: Instant, end: Instant): List<Message>
}