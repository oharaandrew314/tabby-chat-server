package io.andrewohara.tabbychat.auth.dao

import io.andrewohara.tabbychat.TestDriver
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.Realm
import io.andrewohara.tabbychat.contacts.Authorization
import io.andrewohara.tabbychat.createUser
import io.kotest.matchers.shouldBe
import org.http4k.core.Uri
import org.junit.jupiter.api.Test

class AuthorizationDaoTest {

    private val driver = TestDriver()
    private val provider = driver.createProvider(Realm(Uri.of("http://tabby.chat")))

    private val testObj = provider.authDao

    private val user1 = provider.createUser("user1")
    private val user2 = provider.createUser("user2")

    @Test
    fun `create and get contact authorization`() {
        val authorization = Authorization(
            type = Authorization.Type.Contact,
            principal = user1.id,
            bearer = user2.id,
            value = AccessToken("token2"),
            expires = null
        )

        testObj += authorization

        testObj[authorization.value] shouldBe authorization
    }
}