package io.andrewohara.tabbychat.contacts

sealed interface ContactError {
    object InvitationRejected: ContactError
    data class InvalidContact(val id: String): ContactError
    object AlreadyContact: ContactError
}