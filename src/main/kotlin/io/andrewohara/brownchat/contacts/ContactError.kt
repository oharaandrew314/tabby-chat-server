package io.andrewohara.brownchat.contacts

sealed interface ContactError {
    object InvitationRejected: ContactError
    data class InvalidContact(val id: String): ContactError
    object AlreadyContact: ContactError
}