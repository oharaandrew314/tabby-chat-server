package io.andrewohara.tabbychat.protocol.v1.api

import dev.forkhandles.result4k.*
import io.andrewohara.tabbychat.TabbyChatError
import io.andrewohara.tabbychat.TabbyChatService
import io.andrewohara.tabbychat.auth.Authorization
import io.andrewohara.tabbychat.protocol.v1.V1Lenses
import io.andrewohara.tabbychat.protocol.v1.V1Samples
import io.andrewohara.tabbychat.protocol.v1.toResponse
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
        const val tokensPath = "/p2p/v1/tokens"
        const val invitationsPath = "/p2p/v1/invitations"
    }

    private val exchangeTokens = tokensPath meta {
        operationId = "v1ExchangeTokens"
        summary = "Exchange access tokens; save the one given and return a new one"
        security = p2pSecurity
        tags += tag
        receiving(V1Lenses.tokenData to V1Samples.tokenData)
        returning(OK, V1Lenses.tokenData to V1Samples.tokenData)
    } bindContract Method.POST to handler@{ request ->
        val incomingToken = V1Lenses.tokenData(request)

        when(val auth = getAuth(request)) {
            is Authorization.Contact -> {
                service.exchangeToken(
                    userId = auth.owner,
                    contactId = auth.contact,
                    existingToken = auth.token,
                    incomingToken = incomingToken.token
                )
            }
            is Authorization.Invite -> {
                service.exchangeToken(
                    userId = auth.owner,
                    contactId = incomingToken.userId,
                    existingToken = auth.token,
                    incomingToken = incomingToken.token
                )
            }
            is Authorization.Owner -> return@handler Response(Status.FORBIDDEN)
        }
            .map { token ->  Response(OK).with(V1Lenses.tokenData of token) }
            .mapFailure { it.toResponse() }
            .get()
    }

    private val receiveMessage = messagesPath meta {
        operationId = "v1ReceiveMessage"
        summary = "Receive a message to be saved"
        security = p2pSecurity
        tags += tag
        receiving(V1Lenses.messageContent to V1Samples.message1.content)
        returning(OK, V1Lenses.messageReceipt to V1Samples.messageReceipt)
    } bindContract Method.POST to { request ->
        getAuth(request).asContact()
            .flatMap { contact ->
                service.saveMessage(userId = contact.owner,
                    senderId = contact.contact,
                    recipientId = contact.owner,
                    content = V1Lenses.messageContent(request)
                )
            }
            .map { Response(OK).with(V1Lenses.messageReceipt of it) }
            .mapFailure { it.toResponse() }
            .get()
    }

    private val getUser = userPath meta {
        operationId = "v1GetUser"
        summary = "Get the user your token belongs to"
        security = p2pSecurity
        tags += tag
        returning(OK, V1Lenses.user to V1Samples.user1)
    } bindContract Method.GET to { request ->
        getAuth(request)
            .asContact()
            .flatMap { contact -> service.getUser(userId = contact.owner) }
            .map { Response(OK).with(V1Lenses.user of it) }
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
            .flatMap { contact -> service.deleteContact(ownerId = contact.owner, contactId = contact.contact) }
            .map { Response(OK) }
            .mapFailure { it.toResponse() }
            .get()
    }

    fun routes() = listOf(receiveMessage, getUser, revokeContact, exchangeTokens)

    private fun Authorization.asContact(): Result<Authorization.Contact, TabbyChatError> = if (this is Authorization.Contact) {
        Success(this)
    } else {
        TabbyChatError.Forbidden.err()
    }
}