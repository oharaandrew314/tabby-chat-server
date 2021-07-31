package io.andrewohara.brownchat.messages

import io.andrewohara.brownchat.users.UserId

sealed class MessageError {
    object Rejected: MessageError()
    data class NotContact(val userId: UserId): MessageError()
}