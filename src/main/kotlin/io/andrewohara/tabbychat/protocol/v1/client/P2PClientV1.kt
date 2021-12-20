package io.andrewohara.tabbychat.protocol.v1.client

import dev.forkhandles.result4k.Failure
import io.andrewohara.tabbychat.users.User
import org.http4k.core.*
import org.http4k.filter.ClientFilters
import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.andrewohara.tabbychat.TabbyChatError
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.Realm
import io.andrewohara.tabbychat.contacts.TokenData
import io.andrewohara.tabbychat.messages.*
import io.andrewohara.tabbychat.protocol.v1.V1Lenses
import io.andrewohara.tabbychat.protocol.v1.api.P2PApiV1
import io.andrewohara.tabbychat.protocol.v1.toDtoV1
import io.andrewohara.tabbychat.protocol.v1.toError
import io.andrewohara.tabbychat.protocol.v1.toModel

fun interface P2PClientV1Factory: (Realm, AccessToken) -> P2PClientV1 {

    fun backend(realm: Realm): HttpHandler

    override fun invoke(realm: Realm, token: AccessToken): P2PClientV1 {
        val client = ClientFilters.BearerAuth(token.value)
            .then(backend(realm))

        return P2PClientV1(client)
    }

    operator fun invoke(token: TokenData): P2PClientV1 = invoke(token.realm, token.accessToken)
}

class P2PClientV1 internal constructor(private val backend: HttpHandler) {

    fun sendMessage(content: MessageContent): Result<MessageReceipt, TabbyChatError> {
        val response = Request(Method.POST, P2PApiV1.messagesPath)
            .with(V1Lenses.messageContent of content.toDtoV1())
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(V1Lenses.messageReceipt(response).toModel())
    }

    fun getUser(): Result<User, TabbyChatError> {
        val response = Request(Method.GET, P2PApiV1.userPath)
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(V1Lenses.user(response).toModel())
    }

    fun revokeContact(): Result<Unit, TabbyChatError> {
        val response = Request(Method.DELETE, P2PApiV1.invitationsPath)
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(Unit)
    }

    fun addContact(tokenData: TokenData): Result<TokenData, TabbyChatError> {
        val response = Request(Method.POST, P2PApiV1.contactsPath)
            .with(V1Lenses.tokenData of tokenData.toDtoV1())
            .let(backend)

        if (!response.status.successful) return Failure(response.toError())

        return Success(V1Lenses.tokenData(response).toModel())
    }
}