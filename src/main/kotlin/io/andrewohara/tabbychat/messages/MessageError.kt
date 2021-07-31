package io.andrewohara.tabbychat.messages

import io.andrewohara.tabbychat.users.UserId

sealed class MessageError {
    object Rejected: MessageError()
    data class NotContact(val userId: UserId): MessageError()
}