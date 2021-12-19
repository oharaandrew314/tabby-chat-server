package io.andrewohara.tabbychat.protocol.v1.api

import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import io.andrewohara.tabbychat.TabbyChatService
import io.andrewohara.tabbychat.protocol.v1.V1Lenses
import io.andrewohara.tabbychat.protocol.v1.V1Samples
import io.andrewohara.tabbychat.protocol.v1.toResponse
import io.andrewohara.tabbychat.users.UserId
import org.http4k.contract.div
import org.http4k.contract.meta
import org.http4k.contract.security.Security
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.with
import org.http4k.lens.RequestContextLens

class UserApiV1(
    private val userSecurity: Security,
    private val service: TabbyChatService,
    private val auth: RequestContextLens<UserId>
    ) {

    companion object {
        const val contactsPath = "/client/v1/contacts"
        val contactPath = contactsPath / V1Lenses.userId
        val contactMessagesPath = contactPath / "messages"
        const val messagesPath = "/client//v1/messages"
        const val invitationsPath = "/client/v1/invitations"
    }

    private val listContactIds = contactsPath meta {
        operationId = "v1ListContactIds"
        security = userSecurity
        returning(Status.OK, V1Lenses.userIds to V1Samples.userIds)
    } bindContract Method.GET to { request ->
        service.listContacts(auth(request))
            .map { Response(Status.OK).with(V1Lenses.userIds of it.toTypedArray()) }
            .mapFailure { it.toResponse() }
            .get()
    }

    private val getContact = contactPath meta {
        operationId = "v1GetContact"
        security = userSecurity
        returning(Status.OK, V1Lenses.user to V1Samples.user1)
    } bindContract Method.GET to { contactId ->
        { request ->
            service.getContact(userId = auth(request), contactId = contactId)
                .map { Response(Status.OK).with(V1Lenses.user of it) }
                .mapFailure { it.toResponse() }
                .get()
        }
    }

    private val deleteContact = contactPath meta {
        operationId = "v1DeleteContact"
        summary = "Delete a contact"
        security = userSecurity
    } bindContract Method.DELETE to { contactId ->
        { request ->
            service.deleteContact(ownerId = auth(request), contactId = contactId)
                .map { Response(Status.OK) }
                .mapFailure { it.toResponse() }
                .get()
        }
    }

    private val listMessages = messagesPath meta {
        operationId = "v1ListMessages"
        security = userSecurity
        queries += V1Lenses.since
        returning(Status.OK, V1Lenses.messagePage to V1Samples.messageList)
    } bindContract Method.GET to { request ->
        service.listMessages(userId = auth(request), since = V1Lenses.since(request))
            .map { Response(Status.OK).with(V1Lenses.messagePage of it) }
            .mapFailure { it.toResponse() }
            .get()
    }

    private val sendMessage = contactMessagesPath meta {
        operationId = "v1SendMessage"
        security = userSecurity
        receiving(V1Lenses.messageContent)
        returning(Status.OK, V1Lenses.messageReceipt to V1Samples.messageReceipt)
    } bindContract Method.POST to { contactId, _ ->
        { request ->
            service
                .sendMessage(userId = auth(request), contactId = contactId, content = V1Lenses.messageContent(request))
                .map { Response(Status.OK).with(V1Lenses.messageReceipt of it) }
                .mapFailure { it.toResponse() }
                .get()
        }
    }

    private val createInvitation = invitationsPath meta {
        operationId = "v1CreateInvitation"
        security = userSecurity
        returning(Status.OK, V1Lenses.tokenData to V1Samples.tokenData)
    } bindContract Method.GET to { request ->
        service
            .createInvitation(userId = auth(request))
            .map {  Response(Status.OK).with(V1Lenses.tokenData of it) }
            .mapFailure { it.toResponse() }
            .get()
    }

    private val acceptInvitation = invitationsPath meta {
        operationId = "v1AcceptInvitation"
        security = userSecurity
        receiving(V1Lenses.tokenData)
        returning(Status.OK, V1Lenses.user to V1Samples.user1)
    } bindContract Method.POST to { request ->
        service
            .acceptInvitation(userId = auth(request), invitation = V1Lenses.tokenData(request))
            .map { Response(Status.OK).with(V1Lenses.user of it) }
            .mapFailure { it.toResponse() }
            .get()
    }

    fun routes() = listOf(deleteContact, listMessages, createInvitation, acceptInvitation, sendMessage, listContactIds, getContact)
}