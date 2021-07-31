package io.andrewohara.tabbychat

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import io.andrewohara.tabbychat.contacts.ContactError
import io.andrewohara.tabbychat.messages.MessageContent
import org.assertj.core.api.Assertions.assertThat
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

        driver(tabbyUser2).acceptInvitation(invitation).also { result ->
            assertThat(result).isEqualTo(Ok(null))
            assertThat(driver.listContactIds(tabbyUser)).containsExactly(tabbyUser2)
            assertThat(driver.listContactIds(tabbyUser2)).containsExactly(tabbyUser)
        }

        // invited user can send messages to inviter
        driver(tabbyUser2).sendMessage(tabbyUser, "hai!").also { result ->
            assertThat(result.getError()).isNull()
            val message = result.get()!!
            assertThat(message.sender).isEqualTo(tabbyUser2)
            assertThat(message.content).isEqualTo(MessageContent(text = "hai!"))
        }

        // inviter can send messages to invited
        driver(tabbyUser).sendMessage(tabbyUser2, "sup").also { result ->
            assertThat(result.getError()).isNull()
            val message = result.get()!!
            assertThat(message.sender).isEqualTo(tabbyUser)
            assertThat(message.content).isEqualTo(MessageContent(text = "sup"))
        }
    }

    @Test
    fun `send messages between two users of different services`() {
        val invitation = tabbyChat(tabbyUser).createInvitation()

        driver(brownUser).acceptInvitation(invitation).also { result ->
            assertThat(result).isEqualTo(Ok(null))
            assertThat(driver.listContactIds(brownUser)).containsExactly(tabbyUser)
            assertThat(driver.listContactIds(tabbyUser)).containsExactly(brownUser)
        }

        // invited user can send messages to inviter
        driver(brownUser).sendMessage(tabbyUser, "hai!").also { result ->
            assertThat(result.getError()).isNull()
            val message = result.get()!!
            assertThat(message.sender).isEqualTo(brownUser)
            assertThat(message.content).isEqualTo(MessageContent(text = "hai!"))
        }

        // inviter can send messages to invited
        driver(tabbyUser).sendMessage(brownUser, "sup").also { result ->
            assertThat(result.getError()).isNull()
            val message = result.get()!!
            assertThat(message.sender).isEqualTo(tabbyUser)
            assertThat(message.content).isEqualTo(MessageContent(text = "sup"))
        }
    }

    @Test
    fun `send message between two users - both have copies of all messages`() {
        driver.givenContacts(brownUser, tabbyUser)

        driver(brownUser).sendMessage(tabbyUser, "hai").also { result ->
            assertThat(result.getError()).isNull()
        }
        driver(tabbyUser).sendMessage(brownUser, "sup").also { result ->
            assertThat(result.getError()).isNull()
        }

        assertThat(driver.listMessages(brownUser).map { it.sender to it.content.text }).containsExactly(
            brownUser to "hai",
            tabbyUser to "sup"
        )
        assertThat(driver.listMessages(tabbyUser).map { it.sender to it.content.text }).containsExactly(
            brownUser to "hai",
            tabbyUser to "sup"
        )
    }

    @Test
    fun `cannot accept same invitation by multiple users`() {
        val invitation = tabbyChat(tabbyUser).createInvitation()

        driver(brownUser).acceptInvitation(invitation).also { result ->
            assertThat(result).isEqualTo(Ok(null))
        }
        driver(brownUser2).acceptInvitation(invitation).also { result ->
            assertThat(result).isEqualTo(Err(ContactError.InvitationRejected))
        }
    }

    @Test
    fun `cannot accept two invitations from same user twice`() {
        val invite1 = tabbyChat(tabbyUser).createInvitation()
        val invite2 = tabbyChat(tabbyUser).createInvitation()

        driver(brownUser).acceptInvitation(invite1).also { result ->
            assertThat(result).isEqualTo(Ok(null))
        }
        driver(brownUser).acceptInvitation(invite2).also { result ->
            assertThat(result).isEqualTo(Err(ContactError.AlreadyContact))
        }
    }
}