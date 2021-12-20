package io.andrewohara.tabbychat.protocol.v1.api

import dev.forkhandles.result4k.valueOrNull
import dev.mrbergin.kotest.result4k.shouldBeFailure
import dev.mrbergin.kotest.result4k.shouldBeSuccess
import io.andrewohara.tabbychat.*
import io.andrewohara.tabbychat.auth.Realm
import io.andrewohara.tabbychat.contacts.TokenData
import io.andrewohara.tabbychat.protocol.v1.client.UserClientFactoryV1
import io.andrewohara.utils.jdk.minus
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.http4k.core.Uri
import org.junit.jupiter.api.Test
import java.time.Duration

class UserApiV1Test {

    private val driver = TestDriver()

    private val provider = driver.createProvider(Realm(Uri.of("http://tabby.chat")))

    private val self = provider.createUser("self")
    private val selfToken: TokenData = provider.service.createAccessToken(self.id).valueOrNull()!!

    private val contact = provider.createUser("contact").also {
        driver.givenContacts(self, it)
    }
    private val other = provider.createUser("other")

    private val client = UserClientFactoryV1(provider)(selfToken)

    @Test
    fun `get contact`() {
        client.getContact(contact.id) shouldBeSuccess contact
    }

    @Test
    fun `list contacts`() {
        client.listContactIds().shouldBeSuccess {
            it.shouldContainExactly(contact.id)
        }
    }

    @Test
    fun `send message`() {
        client.sendMessage(contact.id, "hai".toMessageContent()).shouldBeSuccess { receipt ->
            receipt.sender shouldBe self.id
            receipt.recipient shouldBe contact.id
        }
    }

    @Test
    fun `send message - not contact`() {
        client.sendMessage(other.id, "hai".toMessageContent()) shouldBeFailure TabbyChatError.NotFound
    }

    @Test
    fun `delete contact - not contact`() {
        client.deleteContact(other.id) shouldBeFailure TabbyChatError.NotFound
    }

    @Test
    fun `delete contact`() {
        client.deleteContact(contact.id) shouldBeSuccess Unit
    }

    @Test
    fun `create invitation`() {
        client.createInvitation().shouldBeSuccess { token ->
            token.userId shouldBe self.id
        }
    }

    @Test
    fun `list messages`() {
        provider.service.saveMessage(userId = self.id, senderId = contact.id, recipientId = self.id, "hai".toMessageContent()).shouldBeSuccess()
        driver.clock += Duration.ofSeconds(10)
        provider.service.saveMessage(userId = self.id, senderId = self.id, recipientId = contact.id, "sup".toMessageContent()).shouldBeSuccess()

        client.listMessages(driver.clock - Duration.ofSeconds(30)).shouldBeSuccess { page ->
            page.nextTime.shouldBeNull()
            page.messages.shouldHaveSize(2)
        }
    }

    @Test
    fun `accept invitation`() {
        val invitation = provider.service.createInvitation(other.id).valueOrNull()!!

        client.acceptInvitation(invitation) shouldBeSuccess other
        client.sendMessage(other.id, "sup".toMessageContent()).shouldBeSuccess()
    }
}