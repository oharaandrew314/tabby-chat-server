package io.andrewohara.tabbychat

import com.github.michaelbull.result.Result
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.contacts.ContactError
import io.andrewohara.tabbychat.messages.Message
import io.andrewohara.tabbychat.messages.MessageContent
import io.andrewohara.tabbychat.messages.MessageError
import io.andrewohara.tabbychat.users.UserId

class FakeServiceClient(private val services: ServiceFactory, private val user: UserId) {

    fun createInvitation(): AccessToken {
        return services.contactService.createInvitation(user)
    }

    fun acceptInvitation(invitation: AccessToken): Result<Unit?, ContactError> {
        return services.contactService.acceptInvitation(user, invitation)
    }

    fun sendMessage(recipient: UserId, text: String? = null): Result<Message, MessageError> {
        val content = MessageContent(text = text)
        return services.messageService.send(sender = user, recipient = recipient, content = content)
    }
}