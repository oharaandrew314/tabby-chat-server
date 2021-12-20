package io.andrewohara.tabbychat

import dev.forkhandles.result4k.valueOrNull
import io.andrewohara.awsmock.dynamodb.MockDynamoDbV2
import io.andrewohara.awsmock.dynamodb.backend.MockDynamoBackend
import io.andrewohara.dynamokt.DataClassTableSchema
import io.andrewohara.tabbychat.users.UserId
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.AccessTokenGenerator
import io.andrewohara.tabbychat.auth.Realm
import io.andrewohara.tabbychat.contacts.Authorization
import io.andrewohara.tabbychat.contacts.Contact
import io.andrewohara.tabbychat.messages.dao.DynamoMessage
import io.andrewohara.tabbychat.users.User
import io.andrewohara.tabbychat.users.dao.DynamoUser
import io.andrewohara.utils.jdk.toClock
import org.http4k.core.*
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import java.time.Instant

class TestDriver: HttpHandler {

    val clock = Instant.parse("2021-12-12T12:00:00Z").toClock()
    private val startTime = clock.instant()

    private val providers = mutableListOf<TabbyChatProvider>()

    private val dynamo = DynamoDbEnhancedClient.builder()
        .dynamoDbClient(MockDynamoDbV2(MockDynamoBackend(clock)))
        .build()

    val nextToken: AccessTokenGenerator = let {
        var nextToken = 1
        AccessTokenGenerator { AccessToken("token${nextToken++}") }
    }

   fun createProvider(realm: Realm) = TabbyChatProvider(
       clientFactory = { externalRealm -> providers.first { it.realm == externalRealm }.http },
       clock = clock,
       realm = realm,
       messagesTable = dynamo.table("$realm-messages", DataClassTableSchema(DynamoMessage::class)).also { it.createTable() },
       usersTable = dynamo.table("$realm-users", DataClassTableSchema(DynamoUser::class)).also { it.createTable() },
       authTable = dynamo.table("$realm-auth", DataClassTableSchema(Authorization::class)).also { it.createTable() },
       contactsTable = dynamo.table("$realm-contacts", DataClassTableSchema(Contact::class)).also { it.createTable() },
       tokenGenerator = nextToken
   ).also { providers += it }

    private fun getProvider(realm: Realm) = providers.first { it.realm == realm }
    private fun getProvider(userId: UserId) = providers.first { provider -> provider.usersDao.any { it.id == userId } }

    override fun invoke(request: Request): Response {
        val realm = Realm(Uri.of("${request.uri.scheme}://${request.uri.host}"))
        val provider = getProvider(realm)
        return provider(request)
    }

    fun listContactIds(userId: UserId): List<UserId> = getProvider(userId).service.listContacts(userId).valueOrNull()!!

    fun listMessages(user: User, since: Instant = startTime, limit: Int = 1000) = listMessages(user.id, since, limit)
    fun listMessages(userId: UserId, since: Instant = startTime, limit: Int = 1000) = getProvider(userId).service.listMessages(userId, since, limit).valueOrNull()!!.messages

    fun givenContacts(user1: User, user2: User) = givenContacts(user1.id, user2.id)
    fun givenContacts(user1: UserId, user2: UserId) {
        val token1 = nextToken()
        val token2 = nextToken()

        getProvider(user1).contactsDao += Contact(
            ownerId = user1,
            id = user2,
            tokenValue = token2,
            realm = user2.realm(),
            tokenExpires = null
        )
        getProvider(user1).authDao += Authorization(
            value = token1,
            bearer = user2,
            principal = user1,
            expires = null,
            type = Authorization.Type.Contact
        )

        getProvider(user2).contactsDao += Contact(
            ownerId = user2,
            id = user1,
            tokenValue = token1,
            realm = user1.realm(),
            tokenExpires = null
        )
        getProvider(user2).authDao += Authorization(
            value = token2,
            bearer = user1,
            principal = user2,
            expires = null,
            type = Authorization.Type.Contact
        )
    }

    private fun UserId.realm() = getProvider(this).realm
}