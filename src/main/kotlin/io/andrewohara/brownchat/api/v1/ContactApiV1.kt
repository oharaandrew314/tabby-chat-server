package io.andrewohara.brownchat.api.v1

import com.github.michaelbull.result.mapBoth
import io.andrewohara.brownchat.auth.Authorization
import io.andrewohara.brownchat.contacts.ContactService
import io.andrewohara.brownchat.messages.MessageService
import io.andrewohara.brownchat.api.v1.ErrorMapper.toResponse
import io.andrewohara.brownchat.users.UserService
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

/**
 * Handles requests from contacts
 */
class ContactApiV1(
    private val messageService: MessageService,
    private val contactService: ContactService,
    private val userService: UserService,
    private val authLens: RequestContextLens<Authorization>
) {
    private val mapper = V1DtoMapper()

    companion object {
        const val messagesPath = "/v1/contacts/messages"
        const val invitationsPath = "/v1/contacts/invitations"
        const val userPath = "/v1/contacts/user"

        private val wrongAuthType = Response(Status.FORBIDDEN).body("wrong authorization type")
    }

    fun completeInvitation(request: Request): Response {
        val auth = authLens(request) as? Authorization.Invite ?: return wrongAuthType
        val accessToken = mapper(V1Lenses.accessToken(request))

        return contactService.completeInvitation(auth.owner, auth.invitation, accessToken).mapBoth(
            success = { Response(Status.OK).with(V1Lenses.accessToken of mapper(it)) },
            failure = { it.toResponse() }
        )
    }

    fun receiveMessage(request: Request): Response {
        val auth = authLens(request) as? Authorization.Contact ?: return wrongAuthType
        val content = mapper(V1Lenses.messageContent(request))

        messageService.receive(sender = auth.contact, recipient = auth.owner, content = content)

        return Response(Status.OK)
    }

    fun lookupUser(request: Request): Response {
        val userId = when(val auth = authLens(request)) {
            is Authorization.Contact -> auth.owner
            is Authorization.Invite -> auth.owner
            is Authorization.Owner -> auth.owner
        }

        val user = userService[userId] ?: return Response(Status.NOT_FOUND)

        return Response(Status.OK)
            .with(V1Lenses.userLens of mapper(user))
    }
}