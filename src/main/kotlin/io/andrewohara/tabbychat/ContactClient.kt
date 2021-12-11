package io.andrewohara.tabbychat

import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.contacts.ContactError
import io.andrewohara.tabbychat.contacts.Contact
import io.andrewohara.tabbychat.messages.MessageContent
import io.andrewohara.tabbychat.messages.MessageError
import io.andrewohara.tabbychat.api.v1.ContactApiV1
import io.andrewohara.tabbychat.api.v1.V1Lenses
import io.andrewohara.tabbychat.api.v1.V1DtoMapper
import io.andrewohara.tabbychat.users.User
import org.http4k.core.*
import org.http4k.filter.ClientFilters
import java.io.IOException
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.andrewohara.tabbychat.contacts.err

class ContactClient(private val backend: HttpHandler) {

    private val mapper = V1DtoMapper()

    private fun withProvider(token: AccessToken) = ClientFilters.SetHostFrom(Uri.of("https://${token.realm}"))
        .then(ClientFilters.BearerAuth(token.value))
        .then(backend)

    /**
     * Accept an invitation by sending your own access token, and receive a token in return
     */
    fun completeInvitation(invitation: AccessToken, token: AccessToken): Result<AccessToken, ContactError> {
        val request = Request(Method.POST, ContactApiV1.invitationsPath)
            .with(V1Lenses.accessToken of mapper(token))

        val response = withProvider(invitation)(request)
        return when(response.status) {
            Status.OK -> Success(mapper(V1Lenses.accessToken(response)))
            Status.CONFLICT -> ContactError.AlreadyContact.err()
            Status.UNAUTHORIZED -> ContactError.InvitationRejected.err()
            Status.BAD_REQUEST -> throw IOException("invalid acceptance")
            else -> throw IOException("Error accepting invitation: ")
        }
    }

    fun sendMessage(contact: Contact, content: MessageContent): MessageError? {
        val request = Request(Method.POST, ContactApiV1.messagesPath)
            .with(V1Lenses.messageContent of mapper(content))

        val response = withProvider(contact.token)(request)
        return when(response.status) {
            Status.OK -> null
            Status.FORBIDDEN -> return MessageError.Rejected
            else -> throw IOException("Error sending message to ${contact.userId}: ${response.status}")
        }
    }

    fun getUser(token: AccessToken): Result<User, ContactError> {
        val request = Request(Method.GET, ContactApiV1.userPath)

        val response = withProvider(token)(request)
        return when(response.status) {
            Status.OK -> Success(mapper(V1Lenses.user(response)))
            Status.UNAUTHORIZED -> ContactError.InvitationRejected.err()  // TODO proper error?
            Status.FORBIDDEN -> ContactError.InvitationRejected.err()  // TODO proper error?
            else -> throw IOException("Error looking up user for $token: ${response.status}")
        }
    }
}