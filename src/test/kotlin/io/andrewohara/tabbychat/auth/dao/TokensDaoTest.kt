package io.andrewohara.tabbychat.auth.dao

import io.andrewohara.tabbychat.TestDriver
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.Authorization
import io.andrewohara.tabbychat.auth.Realm
import io.andrewohara.tabbychat.contacts.Contact
import io.andrewohara.tabbychat.contacts.TokenData
import io.andrewohara.tabbychat.createUser
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import org.http4k.core.Uri
import org.junit.jupiter.api.Test

class TokensDaoTest {

    private val driver = TestDriver()
    private val provider = driver.createProvider(Realm(Uri.of("http://tabby.chat")))

    private val testObj = provider.tokensDao

    private val user1 = provider.createUser("user1")
    private val user2 = provider.createUser("user2")
    private val user3 = provider.createUser("user3")

    private val token1 = AccessToken("token1")
    private val token2 = AccessToken("token2")
    private val token3 = AccessToken("token3")
    private val token4 = AccessToken("token4")

    @Test
    fun `create contact`() {
        val contactToken = TokenData(token2, user2.id, provider.realm, null)
        testObj.createContact(user1.id, token1, contactToken) shouldBe Contact(
            ownerId = user1.id,
            accessToken = token1,
            contactToken = contactToken
        )
    }

    @Test
    fun `get contact`() {
        val contactToken = TokenData(token2, user2.id, provider.realm, null)
        testObj.createContact(user1.id, token1, contactToken)

        testObj.getContact(user1.id, user2.id) shouldBe Contact(
            ownerId = user1.id,
            accessToken = token1,
            contactToken = contactToken
        )
    }

    @Test
    fun `list contacts`() {
        val contactToken = TokenData(token2, user2.id, provider.realm, null)
        testObj.createContact(user1.id, token1, contactToken)

        val contactToken2 = TokenData(token4, user3.id, provider.realm, null)
        testObj.createContact(user1.id, token3, contactToken2)

        testObj.listContacts(user1.id).shouldContainExactlyInAnyOrder(
            Contact(ownerId = user1.id, accessToken = token1, contactToken = contactToken),
            Contact(ownerId = user1.id, accessToken = token3, contactToken = contactToken2)
        )
    }

    @Test
    fun `verify contact token`() {
        val contactToken = TokenData(token2, user2.id, provider.realm, null)
        testObj.createContact(user1.id, token1, contactToken)

        testObj.verify(token1, driver.clock.instant()) shouldBe Authorization.Contact(owner = user1.id, contact = user2.id, token = token1)
    }
}