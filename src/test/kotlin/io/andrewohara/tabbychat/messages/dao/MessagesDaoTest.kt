package io.andrewohara.tabbychat.messages.dao

import io.andrewohara.tabbychat.TestDriver
import io.andrewohara.tabbychat.auth.Realm
import io.andrewohara.tabbychat.createUser
import io.andrewohara.tabbychat.messages.Message
import io.andrewohara.tabbychat.messages.MessagePage
import io.andrewohara.tabbychat.toMessageContent
import io.kotest.matchers.shouldBe
import org.http4k.core.Uri
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class MessagesDaoTest {

    private val driver = TestDriver()
    private val provider = driver.createProvider(Realm(Uri.of("http://tabby.chat")))
    private val testObj = provider.messagesDao

    private val user = provider.createUser("user")
    private val contact = provider.createUser("contact")
    private val t0 = Instant.parse("2021-01-01T12:00:00Z")

    private val message0 = Message("hai".toMessageContent(), received = t0, sender = user.id, recipient = contact.id)
    private val message1 = Message("sup".toMessageContent(), received = t0 + Duration.ofMinutes(2), sender = contact.id, recipient = user.id)
    private val message2 = Message("nm u?".toMessageContent(), received = t0 + Duration.ofMinutes(4), sender = user.id, recipient = contact.id)
    private val message3 = Message("stuff".toMessageContent(), received = t0 + Duration.ofMinutes(6), sender = contact.id, recipient = user.id)

    @Test
    fun `list empty messages`() {
        testObj.list(user.id, t0, 1000) shouldBe MessagePage(
            messages = emptyList(),
            nextTime = null
        )
    }

    @Test
    fun `list all messages`() {
        testObj.add(user.id, message0)
        testObj.add(user.id, message1)
        testObj.add(user.id, message2)
        testObj.add(user.id, message3)

        testObj.list(user.id, t0, 1000) shouldBe MessagePage(
            messages = listOf(message0, message1, message2, message3),
            nextTime = null
        )
    }

//    @Test FIXME mock dynamo doesn't support condition expressions
//    fun `list some messages`() {
//        testObj.list(user.id, t0, 2) shouldBe MessagePage(
//            messages = listOf(message0, message1, message2, message3),
//            nextTime = message2.received
//        )
//    }
}