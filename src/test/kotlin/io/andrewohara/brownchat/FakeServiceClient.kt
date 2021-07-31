package io.andrewohara.brownchat

import com.github.michaelbull.result.Result
import io.andrewohara.brownchat.auth.AccessToken
import io.andrewohara.brownchat.contacts.ContactError
import io.andrewohara.brownchat.messages.Message
import io.andrewohara.brownchat.messages.MessageContent
import io.andrewohara.brownchat.messages.MessageError
import io.andrewohara.brownchat.users.UserId

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