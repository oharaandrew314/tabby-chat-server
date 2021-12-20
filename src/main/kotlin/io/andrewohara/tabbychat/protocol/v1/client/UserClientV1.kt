package io.andrewohara.tabbychat.protocol.v1.client

import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.andrewohara.tabbychat.TabbyChatError
import io.andrewohara.tabbychat.protocol.v1.*
import io.andrewohara.tabbychat.protocol.v1.api.UserApiV1
import org.http4k.core.*
import org.http4k.filter.ClientFilters
import java.time.Instant

class UserClientFactoryV1(private val backend: HttpHandler): (TokenDataDtoV1) -> UserClientV1 {

    override fun invoke(token: TokenDataDtoV1): UserClientV1 {
        val client = ClientFilters.BearerAuth(token.accessToken)
            .then(ClientFilters.SetHostFrom(token.realm))
            .then(backend)

        return UserClientV1(client)
    }
}

class UserClientV1(private val backend: HttpHandler) {

    fun listContactIds(): Result<Array<String>, TabbyChatError> {
        val response = Request(Method.GET, UserApiV1.contactsPath)
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(V1Lenses.userIds(response))
    }

    fun getContact(contactId: String): Result<UserDtoV1, TabbyChatError> {
        val response = Request(Method.GET, "${UserApiV1.contactsPath}/${V1Lenses.userId}")
            .with(V1Lenses.userId of contactId)
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(V1Lenses.user(response))
    }

    fun deleteContact(contactId: String): Result<Unit, TabbyChatError> {
        val response = Request(Method.DELETE, "${UserApiV1.contactsPath}/${V1Lenses.userId}")
            .with(V1Lenses.userId of contactId)
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(Unit)
    }

    fun listMessages(since: Instant): Result<MessagePageDtoV1, TabbyChatError> {
        val response = Request(Method.GET, UserApiV1.messagesPath)
            .with(V1Lenses.since of since)
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(V1Lenses.messagePage(response))
    }

    fun createInvitation(): Result<TokenDataDtoV1, TabbyChatError> {
        val response = Request(Method.GET, UserApiV1.invitationsPath)
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(V1Lenses.tokenData(response))
    }

    fun sendMessage(contactId: String, content: MessageContentDtoV1): Result<MessageReceiptDtoV1, TabbyChatError> {
        val response = Request(Method.POST, "${UserApiV1.contactsPath}/${V1Lenses.userId}/messages")
            .with(V1Lenses.userId of contactId)
            .with(V1Lenses.messageContent of content)
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(V1Lenses.messageReceipt(response))
    }

    fun acceptInvitation(tokenData: TokenDataDtoV1): Result<UserDtoV1, TabbyChatError> {
        val response = Request(Method.POST, UserApiV1.invitationsPath)
            .with(V1Lenses.tokenData of tokenData)
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(V1Lenses.user(response))
    }
}