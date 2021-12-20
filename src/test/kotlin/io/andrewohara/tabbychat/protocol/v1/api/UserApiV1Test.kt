package io.andrewohara.tabbychat.protocol.v1.api

import dev.forkhandles.result4k.valueOrNull
import dev.mrbergin.kotest.result4k.shouldBeFailure
import dev.mrbergin.kotest.result4k.shouldBeSuccess
import io.andrewohara.tabbychat.*
import io.andrewohara.tabbychat.auth.Realm
import io.andrewohara.tabbychat.contacts.TokenData
import io.andrewohara.tabbychat.protocol.v1.client.UserClientFactoryV1
import io.andrewohara.tabbychat.protocol.v1.toDtoV1
import io.andrewohara.utils.jdk.minus
import io.kotest.matchers.collections.shouldBeEmpty
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

    private val client = UserClientFactoryV1(provider)(selfToken.toDtoV1())

    @Test
    fun `get contact`() {
        client.getContact(contact.id.toDtoV1()) shouldBeSuccess contact.toDtoV1()
    }

    @Test
    fun `list contacts`() {
        client.listContactIds().shouldBeSuccess {
            it.shouldContainExactly(contact.id.toDtoV1())
        }
    }

    @Test
    fun `send message`() {
        client.sendMessage(contact.id.toDtoV1(), "hai".toMessageContent().toDtoV1()).shouldBeSuccess { receipt ->
            receipt.sender shouldBe self.id.toDtoV1()
            receipt.recipient shouldBe contact.id.toDtoV1()
        }
    }

    @Test
    fun `send message - not contact`() {
        client.sendMessage(other.id.toDtoV1(), "hai".toMessageContent().toDtoV1()) shouldBeFailure TabbyChatError.NotFound
    }

    @Test
    fun `delete contact - not contact`() {
        client.deleteContact(other.id.toDtoV1()) shouldBeFailure TabbyChatError.NotFound
    }

    @Test
    fun `delete contact`() {
        client.deleteContact(contact.id.toDtoV1()) shouldBeSuccess Unit

        provider.contactsDao[self.id].shouldBeEmpty()
        provider.contactsDao[contact.id].shouldBeEmpty()
    }

    @Test
    fun `create invitation`() {
        client.createInvitation().shouldBeSuccess { token ->
            token.realm shouldBe provider.realm.value
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

        client.acceptInvitation(invitation.toDtoV1()) shouldBeSuccess other.toDtoV1()
        client.sendMessage(other.id.toDtoV1(), "sup".toMessageContent().toDtoV1()).shouldBeSuccess()
    }
}