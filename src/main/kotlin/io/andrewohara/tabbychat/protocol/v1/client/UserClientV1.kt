package io.andrewohara.tabbychat.protocol.v1.client

import dev.forkhandles.result4k.Result
import dev.forkhandles.result4k.Success
import io.andrewohara.tabbychat.TabbyChatError
import io.andrewohara.tabbychat.contacts.TokenData
import io.andrewohara.tabbychat.messages.MessageContent
import io.andrewohara.tabbychat.messages.MessagePage
import io.andrewohara.tabbychat.messages.MessageReceipt
import io.andrewohara.tabbychat.protocol.v1.V1Lenses
import io.andrewohara.tabbychat.protocol.v1.api.UserApiV1
import io.andrewohara.tabbychat.protocol.v1.toError
import io.andrewohara.tabbychat.users.User
import io.andrewohara.tabbychat.users.UserId
import org.http4k.core.*
import org.http4k.filter.ClientFilters
import java.time.Instant

class UserClientFactoryV1(private val backend: HttpHandler): (TokenData) -> UserClientV1 {

    override fun invoke(tokenData: TokenData): UserClientV1 {
        val client = ClientFilters.BearerAuth(tokenData.token.value)
            .then(backend)

        return UserClientV1(client)
    }
}

class UserClientV1(private val backend: HttpHandler) {

    fun listContactIds(): Result<Array<UserId>, TabbyChatError> {
        val response = Request(Method.GET, UserApiV1.contactsPath)
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(V1Lenses.userIds(response))
    }

    fun getContact(contactId: UserId): Result<User, TabbyChatError> {
        val response = Request(Method.GET, "${UserApiV1.contactsPath}/${V1Lenses.userId}")
            .with(V1Lenses.userId of contactId)
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(V1Lenses.user(response))
    }

    fun deleteContact(contactId: UserId): Result<Unit, TabbyChatError> {
        val response = Request(Method.DELETE, "${UserApiV1.contactsPath}/${V1Lenses.userId}")
            .with(V1Lenses.userId of contactId)
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(Unit)
    }

    fun listMessages(since: Instant): Result<MessagePage, TabbyChatError> {
        val response = Request(Method.GET, UserApiV1.messagesPath)
            .with(V1Lenses.since of since)
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(V1Lenses.messagePage(response))
    }

    fun createInvitation(): Result<TokenData, TabbyChatError> {
        val response = Request(Method.GET, UserApiV1.invitationsPath)
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(V1Lenses.tokenData(response))
    }

    fun sendMessage(contactId: UserId, content: MessageContent): Result<MessageReceipt, TabbyChatError> {
        val response = Request(Method.POST, "${UserApiV1.contactsPath}/${V1Lenses.userId}/messages")
            .with(V1Lenses.userId of contactId)
            .with(V1Lenses.messageContent of content)
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(V1Lenses.messageReceipt(response))
    }

    fun acceptInvitation(tokenData: TokenData): Result<User, TabbyChatError> {
        val response = Request(Method.POST, UserApiV1.invitationsPath)
            .with(V1Lenses.tokenData of tokenData)
            .let(backend)

        if (!response.status.successful) return response.toError().err()

        return Success(V1Lenses.user(response))
    }
}