package io.andrewohara.tabbychat.messages

sealed class MessageError {
    object Rejected: MessageError()
    object NotContact: MessageError()
}