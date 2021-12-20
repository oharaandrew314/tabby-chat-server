package io.andrewohara.tabbychat.protocol.v1.api

import dev.forkhandles.result4k.*
import io.andrewohara.tabbychat.TabbyChatError
import io.andrewohara.tabbychat.TabbyChatService
import io.andrewohara.tabbychat.contacts.Authorization
import io.andrewohara.tabbychat.protocol.v1.*
import org.http4k.contract.Tag
import org.http4k.contract.meta
import org.http4k.contract.security.Security
import org.http4k.core.*
import org.http4k.core.Status.Companion.OK
import org.http4k.lens.*

class P2PApiV1(
    private val getAuth: RequestContextLens<Authorization>,
    private val p2pSecurity: Security,
    private val service: TabbyChatService
) {
    private val tag = Tag("P2P", "Used by service providers for P2P communication")

    companion object {
        const val userPath = "/p2p/v1/user"
        const val messagesPath = "/p2p/v1/messages"
        const val contactsPath = "/p2p/v1/contacts"
        const val invitationsPath = "/p2p/v1/invitations"
    }

    private val addContact = contactsPath meta {
        operationId = "v1AddContact"
        summary = "Exchange your invitation token to add the inviting user as a contact"
        security = p2pSecurity
        tags += tag
        receiving(V1Lenses.tokenData to V1Samples.tokenData.toDtoV1())
        returning(OK, V1Lenses.tokenData to V1Samples.tokenData.toDtoV1())
    } bindContract Method.POST to handler@{ request ->
        getAuth(request).asInvited()
            .flatMap { auth -> service.createContact(auth.principal, contactToken = V1Lenses.tokenData(request).toModel()) }
            .map { Response(OK).with(V1Lenses.tokenData of it.toDtoV1()) }
            .mapFailure { it.toResponse() }
            .get()
    }

    private val receiveMessage = messagesPath meta {
        operationId = "v1ReceiveMessage"
        summary = "Receive a message to be saved"
        security = p2pSecurity
        tags += tag
        receiving(V1Lenses.messageContent to V1Samples.message1.content.toDtoV1())
        returning(OK, V1Lenses.messageReceipt to V1Samples.messageReceipt.toDtoV1())
    } bindContract Method.POST to { request ->
        getAuth(request).asContact()
            .flatMap { auth ->
                service.saveMessage(
                    userId = auth.principal,
                    senderId = auth.bearer!!,
                    recipientId = auth.principal,
                    content = V1Lenses.messageContent(request).toModel()
                )
            }
            .map { Response(OK).with(V1Lenses.messageReceipt of it.toDtoV1()) }
            .mapFailure { it.toResponse() }
            .get()
    }

    private val getUser = userPath meta {
        operationId = "v1GetUser"
        summary = "Get the user your token belongs to"
        security = p2pSecurity
        tags += tag
        returning(OK, V1Lenses.user to V1Samples.user1.toDtoV1())
    } bindContract Method.GET to { request ->
        Success(getAuth(request))
            .flatMap { auth -> service.getUser(userId = auth.principal) }
            .map { Response(OK).with(V1Lenses.user of it.toDtoV1()) }
            .mapFailure { it.toResponse() }
            .get()
    }

    private val revokeContact = invitationsPath meta {
        operationId = "v1RevokeContact"
        summary = "Revoke the contact's permission to contact"
        security = p2pSecurity
        tags += tag
    } bindContract Method.DELETE to { request ->
        getAuth(request)
            .asContact()
            .flatMap { auth -> service.deleteContact(ownerId = auth.principal, contactId = auth.bearer!!) }
            .map { Response(OK) }
            .mapFailure { it.toResponse() }
            .get()
    }

    fun routes() = listOf(receiveMessage, getUser, revokeContact, addContact)

    private fun Authorization.asContact(): Result<Authorization, TabbyChatError> = if (type == Authorization.Type.Contact) {
        Success(this)
    } else {
        TabbyChatError.Forbidden.err()
    }
    private fun Authorization.asInvited(): Result<Authorization, TabbyChatError> = if (type == Authorization.Type.Invite) {
        Success(this)
    } else {
        TabbyChatError.Forbidden.err()
    }
}