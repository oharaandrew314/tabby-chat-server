package io.andrewohara.tabbychat.contacts

import dev.forkhandles.result4k.Failure

sealed interface ContactError {
    object InvitationRejected: ContactError
    data class InvalidContact(val id: String): ContactError
    object AlreadyContact: ContactError
}

fun ContactError.err() = Failure(this)