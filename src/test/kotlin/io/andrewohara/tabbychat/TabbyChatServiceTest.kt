package io.andrewohara.tabbychat

import dev.forkhandles.result4k.valueOrNull
import dev.mrbergin.kotest.result4k.shouldBeFailure
import dev.mrbergin.kotest.result4k.shouldBeSuccess
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.Authorization
import io.andrewohara.tabbychat.contacts.TokenData
import io.andrewohara.tabbychat.messages.toMessage
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TabbyChatServiceTest {

    private val driver = TestDriver()

    private val tabbyChat = driver.createProvider("tabby.chat")
    private val brownChat = driver.createProvider("brown.chat")

    private val tabbyUser1 = tabbyChat.createUser("tabby user 1")
    private val tabbyUser2 = tabbyChat.createUser("tabby user 2")

    private val brownUser1 = brownChat.createUser("brown user 1")

    private val content = "this is a message".toMessageContent()

    @Test
    fun `save message between two users from same realm`() {
        tabbyChat.service.saveMessage(userId = tabbyUser1.id, senderId = tabbyUser1.id, recipientId = tabbyUser2.id, "hai!".toMessageContent()).shouldBeSuccess { receipt ->
            receipt.sender shouldBe tabbyUser1.id
            receipt.recipient shouldBe tabbyUser2.id

            driver.listMessages(tabbyUser2).shouldBeEmpty()
            driver.listMessages(tabbyUser1).shouldContainExactly(receipt.toMessage("hai!".toMessageContent()))
        }
    }

    @Test
    fun `save message between two users of different realms`() {
        tabbyChat.service.saveMessage(userId = tabbyUser1.id, senderId = brownUser1.id, recipientId = tabbyUser1.id, "lolcats".toMessageContent()).shouldBeSuccess { receipt ->
            receipt.sender shouldBe brownUser1.id
            receipt.recipient shouldBe tabbyUser1.id

            driver.listMessages(tabbyUser1).shouldContainExactly(receipt.toMessage("lolcats".toMessageContent()))
            driver.listMessages(tabbyUser2).shouldBeEmpty()
        }
    }

    @Test
    fun `can save message from self`() {
        tabbyChat.service.saveMessage(userId = tabbyUser1.id, senderId = tabbyUser1.id, recipientId = tabbyUser1.id, "meow".toMessageContent()).shouldBeSuccess { receipt ->
            receipt.sender shouldBe tabbyUser1.id
            receipt.recipient shouldBe tabbyUser1.id

            driver.listMessages(tabbyUser1).shouldContainExactly(receipt.toMessage("meow"))
        }
    }

    @Test
    fun `send message to non-contact`() {
        tabbyChat.service.sendMessage(userId = tabbyUser1.id, contactId = tabbyUser2.id, "hi".toMessageContent()) shouldBeFailure TabbyChatError.NotContact
    }

    @Test
    fun `send message to contact from same realm`() {
        driver.givenContacts(tabbyUser1, tabbyUser2)

        tabbyChat.service.sendMessage(userId = tabbyUser1.id, contactId = tabbyUser2.id, content).shouldBeSuccess { receipt ->
            receipt.sender shouldBe tabbyUser1.id
            receipt.recipient shouldBe tabbyUser2.id

            driver.listMessages(tabbyUser1).shouldContainExactly(receipt.toMessage(content))
            driver.listMessages(tabbyUser2).shouldContainExactly(receipt.toMessage(content))
        }
    }

    @Test
    fun `send message to contact from different realm`() {
        driver.givenContacts(tabbyUser1, brownUser1)

        tabbyChat.service.sendMessage(userId = tabbyUser1.id, contactId = brownUser1.id, content).shouldBeSuccess { receipt ->
            receipt.sender shouldBe tabbyUser1.id
            receipt.recipient shouldBe brownUser1.id

            driver.listMessages(tabbyUser1).shouldContainExactly(receipt.toMessage(content))
            driver.listMessages(brownUser1).shouldContainExactly(receipt.toMessage(content))
        }
    }

    @Test
    fun `delete missing contact`() {
        tabbyChat.service.deleteContact(tabbyUser1.id, tabbyUser2.id) shouldBeFailure TabbyChatError.NotContact
    }

    @Test
    fun `delete contact`() {
        driver.givenContacts(tabbyUser1, tabbyUser2)

        tabbyChat.service.deleteContact(tabbyUser1.id, tabbyUser2.id) shouldBeSuccess Unit

        driver.listContactIds(tabbyUser1.id).isEmpty()
        driver.listContactIds(tabbyUser2.id).isEmpty()
    }

    @Test
    fun `create invitation - user not found`() {
        tabbyChat.service.createInvitation(brownUser1.id) shouldBeFailure TabbyChatError.NotFound
    }

    @Test
    fun `create invitation`() {
        tabbyChat.service.createInvitation(tabbyUser1.id).shouldBeSuccess { invitation ->
            invitation.userId shouldBe tabbyUser1.id

            tabbyChat.tokensDao.verify(invitation.token, driver.clock.instant()) shouldBe Authorization.Invite(
                owner = tabbyUser1.id,
                token = invitation.token
            )
        }
    }

    @Test
    fun `accept invitation - invalid token`() {
        val fakeInvitation = TokenData(AccessToken("ABC123"), tabbyUser2.id)

        tabbyChat.service.acceptInvitation(tabbyUser1.id, fakeInvitation) shouldBeFailure TabbyChatError.Forbidden
    }

    @Test
    fun `accept invitation`() {
        val invitation = tabbyChat.service.createInvitation(tabbyUser2.id).valueOrNull()!!

        tabbyChat.service.acceptInvitation(tabbyUser1.id, invitation) shouldBeSuccess tabbyUser2

        driver.listContactIds(tabbyUser1.id).shouldContainExactly(tabbyUser2.id)
        driver.listContactIds(tabbyUser2.id).shouldContainExactly(tabbyUser1.id)
    }
}