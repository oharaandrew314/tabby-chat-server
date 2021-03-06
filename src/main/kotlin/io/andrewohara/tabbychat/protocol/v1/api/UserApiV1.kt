package io.andrewohara.tabbychat.protocol.v1.api

import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import io.andrewohara.tabbychat.TabbyChatService
import io.andrewohara.tabbychat.protocol.v1.*
import io.andrewohara.tabbychat.users.UserId
import org.http4k.contract.Tag
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

    private val contactPath = V1Paths.contactsPath / V1Lenses.userId
    private val contactMessagesPath = contactPath / "messages"

    private val tag = Tag("Client", "Used by clients to work with their own provider")

    private val listContactIds = V1Paths.contactsPath meta {
        operationId = "v1ListContactIds"
        summary = "List the ids of your contacts"
        security = userSecurity
        tags += tag
        returning(Status.OK, V1Lenses.userIds to V1Samples.userIds.toDtoV1())
    } bindContract Method.GET to { request ->
        service.listContacts(auth(request))
            .map { Response(Status.OK).with(V1Lenses.userIds of it.toDtoV1()) }
            .mapFailure { it.toResponse() }
            .get()
    }

    private val getContact = contactPath meta {
        operationId = "v1GetContact"
        summary = "Get the profile of the given contact"
        security = userSecurity
        tags += tag
        returning(Status.OK, V1Lenses.user to V1Samples.user1.toDtoV1())
    } bindContract Method.GET to { contactId ->
        { request ->
            service.getContact(userId = auth(request), contactId = contactId.toUserId())
                .map { Response(Status.OK).with(V1Lenses.user of it.toDtoV1()) }
                .mapFailure { it.toResponse() }
                .get()
        }
    }

    private val deleteContact = contactPath meta {
        operationId = "v1DeleteContact"
        summary = "Delete a contact"
        security = userSecurity
        tags += tag
    } bindContract Method.DELETE to { contactId ->
        { request ->
            service.deleteContact(ownerId = auth(request), contactId = contactId.toUserId())
                .map { Response(Status.OK) }
                .mapFailure { it.toResponse() }
                .get()
        }
    }

    private val listMessages = V1Paths.messagesPath meta {
        operationId = "v1ListMessages"
        summary = "Get a page of messages starting from the given time"
        security = userSecurity
        queries += V1Lenses.since
        tags += tag
        returning(Status.OK, V1Lenses.messagePage to V1Samples.messageList.toDtoV1())
    } bindContract Method.GET to { request ->
        service.listMessages(userId = auth(request), since = V1Lenses.since(request))
            .map { Response(Status.OK).with(V1Lenses.messagePage of it.toDtoV1()) }
            .mapFailure { it.toResponse() }
            .get()
    }

    private val sendMessage = contactMessagesPath meta {
        operationId = "v1SendMessage"
        summary = "Send a message to a contact"
        security = userSecurity
        tags += tag
        receiving(V1Lenses.messageContent)
        returning(Status.OK, V1Lenses.messageReceipt to V1Samples.messageReceipt.toDtoV1())
    } bindContract Method.POST to { contactId, _ ->
        { request ->
            service
                .sendMessage(userId = auth(request), contactId = contactId.toUserId(), content = V1Lenses.messageContent(request).toModel())
                .map { Response(Status.OK).with(V1Lenses.messageReceipt of it.toDtoV1()) }
                .mapFailure { it.toResponse() }
                .get()
        }
    }

    private val createInvitation = V1Paths.invitationsPath meta {
        operationId = "v1CreateInvitation"
        summary = "Create an invitation code to be shared"
        security = userSecurity
        tags += tag
        returning(Status.OK, V1Lenses.tokenData to V1Samples.tokenData.toDtoV1())
    } bindContract Method.GET to { request ->
        service
            .createInvitation(userId = auth(request))
            .map {  Response(Status.OK).with(V1Lenses.tokenData of it.toDtoV1()) }
            .mapFailure { it.toResponse() }
            .get()
    }

    private val acceptInvitation = V1Paths.invitationsPath meta {
        operationId = "v1AcceptInvitation"
        summary = "Accept the given invitation code, adding the user as a contact"
        security = userSecurity
        tags += tag
        receiving(V1Lenses.tokenData)
        returning(Status.OK, V1Lenses.user to V1Samples.user1.toDtoV1())
    } bindContract Method.POST to { request ->
        service
            .acceptInvitation(userId = auth(request), invitation = V1Lenses.tokenData(request).toModel())
            .map { Response(Status.OK).with(V1Lenses.user of it.toDtoV1()) }
            .mapFailure { it.toResponse() }
            .get()
    }

    fun routes() = listOf(deleteContact, listMessages, createInvitation, acceptInvitation, sendMessage, listContactIds, getContact)
}