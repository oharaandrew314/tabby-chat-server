package io.andrewohara.tabbychat

import io.andrewohara.tabbychat.messages.Message
import io.andrewohara.tabbychat.messages.MessageContent
import io.andrewohara.tabbychat.messages.MessageReceipt
import io.andrewohara.tabbychat.messages.toMessage
import io.andrewohara.tabbychat.users.RealName
import io.andrewohara.tabbychat.users.User
import io.andrewohara.tabbychat.users.UserId
import org.http4k.core.Uri

fun TabbyChatProvider.createUser(name: String, icon: Uri? = null) = User(
    id = UserId(name),
    name = RealName(name, null, null),
    icon = icon
).also { usersDao += it }

fun String.toMessageContent() = MessageContent(text = this)
fun MessageReceipt.toMessage(text: String): Message = toMessage(content = text.toMessageContent())