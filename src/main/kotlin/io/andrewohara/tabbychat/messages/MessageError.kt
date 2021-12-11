package io.andrewohara.tabbychat.messages

import dev.forkhandles.result4k.Failure

sealed class MessageError {
    object Rejected: MessageError()
    object NotContact: MessageError()
}

fun MessageError.err() = Failure(this)