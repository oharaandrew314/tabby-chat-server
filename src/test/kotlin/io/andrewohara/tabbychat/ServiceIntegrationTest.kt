package io.andrewohara.tabbychat

import dev.mrbergin.kotest.result4k.shouldBeFailure
import dev.mrbergin.kotest.result4k.shouldBeSuccess
import io.andrewohara.tabbychat.contacts.ContactError
import io.andrewohara.tabbychat.messages.MessageContent
import io.andrewohara.tabbychat.messages.MessageError
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ServiceIntegrationTest {

    private val driver = TestDriver()

    private val tabbyChat = driver.createService("tabby.chat")
    private val brownChat = driver.createService("brown.chat")

    private val tabbyUser = tabbyChat.createUser("tabbyUser").id
    private val tabbyUser2 = tabbyChat.createUser("tabbyUser2").id

    private val brownUser = brownChat.createUser("brownUser").id
    private val brownUser2 = brownChat.createUser("brownUser2").id

    @Test
    fun `send messages between two users of same service`() {
        val invitation = tabbyChat(tabbyUser).createInvitation()

        driver(tabbyUser2).acceptInvitation(invitation) shouldBeSuccess Unit
        driver.listContactIds(tabbyUser).shouldContainExactly(tabbyUser2)
        driver.listContactIds(tabbyUser2).shouldContainExactly(tabbyUser)

        // invited user can send messages to inviter

        driver(tabbyUser2).sendMessage(tabbyUser, "hai!").shouldBeSuccess { message ->
            message.sender shouldBe tabbyUser2
            message.content shouldBe MessageContent(text = "hai!")
        }

        // inviter can send messages to invited
        driver(tabbyUser).sendMessage(tabbyUser2, "sup").shouldBeSuccess { message ->
            message.sender shouldBe tabbyUser
            message.content shouldBe MessageContent(text = "sup")
        }
    }

    @Test
    fun `send messages between two users of different services`() {
        val invitation = tabbyChat(tabbyUser).createInvitation()

        driver(brownUser).acceptInvitation(invitation) shouldBeSuccess Unit
        driver.listContactIds(brownUser).shouldContainExactly(tabbyUser)
        driver.listContactIds(tabbyUser).shouldContainExactly(brownUser)

        // invited user can send messages to inviter
        driver(brownUser).sendMessage(tabbyUser, "hai!").shouldBeSuccess { message ->
            message.sender shouldBe  brownUser
            message.content shouldBe MessageContent(text = "hai!")
        }

        // inviter can send messages to invited
        driver(tabbyUser).sendMessage(brownUser, "sup").shouldBeSuccess { message ->
            message.sender shouldBe tabbyUser
            message.content shouldBe MessageContent(text = "sup")
        }
    }

    @Test
    fun `send message between two users - both have copies of all messages`() {
        driver.givenContacts(brownUser, tabbyUser)

        driver(brownUser).sendMessage(tabbyUser, "hai").shouldBeSuccess()
        driver(tabbyUser).sendMessage(brownUser, "sup").shouldBeSuccess()

        driver.listMessages(brownUser).map { it.sender to it.content.text }.shouldContainExactly(
            brownUser to "hai",
            tabbyUser to "sup"
        )
        driver.listMessages(tabbyUser).map { it.sender to it.content.text }.shouldContainExactly(
            brownUser to "hai",
            tabbyUser to "sup"
        )
    }

    @Test
    fun `cannot accept same invitation by multiple users`() {
        val invitation = tabbyChat(tabbyUser).createInvitation()

        driver(brownUser).acceptInvitation(invitation) shouldBeSuccess Unit
        driver(brownUser2).acceptInvitation(invitation) shouldBeFailure ContactError.InvitationRejected
    }

    @Test
    fun `cannot accept two invitations from same user twice`() {
        val invite1 = tabbyChat(tabbyUser).createInvitation()
        val invite2 = tabbyChat(tabbyUser).createInvitation()

        driver(brownUser).acceptInvitation(invite1) shouldBeSuccess Unit
        driver(brownUser).acceptInvitation(invite2) shouldBeFailure ContactError.AlreadyContact
    }

    @Test
    fun `can send messages to self`() {
        driver(tabbyUser).sendMessage(tabbyUser, "hai").shouldBeSuccess()
    }

    @Test
    fun `cannot send messages to user that is not a contact`() {
        driver(tabbyUser).sendMessage(brownUser, "hai") shouldBeFailure MessageError.NotContact
    }
}