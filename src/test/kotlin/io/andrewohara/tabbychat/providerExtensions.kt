package io.andrewohara.tabbychat

import io.andrewohara.tabbychat.auth.Authorization
import io.andrewohara.tabbychat.contacts.Contact
import io.andrewohara.tabbychat.contacts.TokenData
import io.andrewohara.tabbychat.messages.Message
import io.andrewohara.tabbychat.messages.MessageContent
import io.andrewohara.tabbychat.messages.MessageReceipt
import io.andrewohara.tabbychat.messages.toMessage
import io.andrewohara.tabbychat.users.RealName
import io.andrewohara.tabbychat.users.User
import io.andrewohara.tabbychat.users.UserId
import java.net.URL

fun TabbyChatProvider.createUser(name: String, icon: URL? = null) = User(
    id = UserId.create(realm, name),
    name = RealName(name, null, null),
    icon = icon
).also { usersDao += it }

fun User.toAuthorization() = Authorization.Owner(owner = id)
fun TokenData.toAuthorization() = Authorization.Invite(owner = userId, token = token)
fun Contact.toAuthorization() = Authorization.Contact(owner = ownerId, contact = id, token = accessToken)
fun String.toMessageContent() = MessageContent(text = this)
fun MessageReceipt.toMessage(text: String): Message = toMessage(content = text.toMessageContent())