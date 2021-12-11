package io.andrewohara.tabbychat.api.v1

import dev.forkhandles.result4k.get
import dev.forkhandles.result4k.map
import dev.forkhandles.result4k.mapFailure
import io.andrewohara.tabbychat.auth.Authorization
import io.andrewohara.tabbychat.contacts.ContactService
import io.andrewohara.tabbychat.messages.MessageService
import io.andrewohara.tabbychat.api.v1.ErrorMapper.toResponse
import org.http4k.core.*
import org.http4k.core.Status.Companion.OK
import org.http4k.lens.Query
import org.http4k.lens.RequestContextLens
import org.http4k.lens.instant

/**
 * Handles requests from registered users
 */
class ProviderApiV1(
    private val realm: String,
    private val authLens: RequestContextLens<Authorization>,
    private val messageService: MessageService,
    private val contactServices: ContactService
) {
    companion object {
        val startQueryLens = Query.instant().required("start")
        val endQueryLens = Query.instant().required("end")

        private val wrongAuthType = Response(Status.FORBIDDEN).body("wrong authorization type")
    }

    private val mapper = V1DtoMapper()

    fun createInvitation(request: Request): Response {
        val auth = authLens(request) as? Authorization.Owner ?: return wrongAuthType

        val invitation = contactServices.createInvitation(auth.owner)

        return Response(OK)
            .with(V1Lenses.accessToken of mapper(invitation))
    }

    fun acceptInvitation(request: Request): Response {
        val auth = authLens(request) as? Authorization.Owner ?: return wrongAuthType
        val invitation = mapper(V1Lenses.accessToken(request))

        return contactServices.acceptInvitation(auth.owner, invitation)
            .map { Response(OK) }
            .mapFailure { it.toResponse() }
            .get()
    }

    fun listMessages(request: Request): Response {
        val auth = authLens(request) as? Authorization.Owner ?: return wrongAuthType
        val start = startQueryLens(request)
        val end = endQueryLens(request)

        val messageList = messageService.listMessages(auth.owner, start, end)

        return Response(OK)
            .with(V1Lenses.messageList of messageList.map { mapper(it) }.toTypedArray())
    }

    fun sendMessage(request: Request): Response {
        val auth = authLens(request) as? Authorization.Owner ?: return wrongAuthType
        val (recipient, content) = mapper(V1Lenses.sendMessageRequest(request))

        return messageService.send(auth.owner, recipient, content)
            .map { Response(OK) }
            .mapFailure { TODO() }
            .get()
    }

    fun spec(request: Request): Response {
        val spec = SpecDtoV1(
            realm = realm,
            version = 1
        )

        return Response(OK).with(V1Lenses.spec of spec)
    }
}