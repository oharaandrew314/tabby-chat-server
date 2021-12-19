package io.andrewohara.tabbychat

import io.andrewohara.awsmock.dynamodb.MockDynamoDbV2
import io.andrewohara.awsmock.dynamodb.backend.MockDynamoBackend
import io.andrewohara.dynamokt.DataClassTableSchema
import io.andrewohara.tabbychat.users.UserId
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.AccessTokenGenerator
import io.andrewohara.tabbychat.auth.Realm
import io.andrewohara.tabbychat.auth.dao.DynamoToken
import io.andrewohara.tabbychat.messages.dao.DynamoMessage
import io.andrewohara.tabbychat.users.User
import io.andrewohara.tabbychat.users.dao.DynamoUser
import io.andrewohara.utils.jdk.toClock
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
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

    fun createProvider(realm: String) = createProvider(Realm(realm))
   fun createProvider(realm: Realm) = TabbyChatProvider(
       clientFactory = { externalRealm -> providers.first { it.realm == externalRealm }.http },
       clock = clock,
       realm = realm,
       messagesTable = dynamo.table("$realm-messages", DataClassTableSchema(DynamoMessage::class)).also { it.createTable() },
       usersTable = dynamo.table("$realm-users", DataClassTableSchema(DynamoUser::class)).also { it.createTable() },
       tokensTable = dynamo.table("$realm-tokens", DataClassTableSchema(DynamoToken::class)).also { it.createTable() },
       tokenGenerator = nextToken
   ).also { providers += it }

    private fun getProvider(userId: UserId) = getProvider(userId.realm)
    private fun getProvider(realm: Realm) = providers.firstOrNull { it.realm == realm }

    override fun invoke(request: Request): Response {
        val realm = Realm(request.uri.host)
        val provider = getProvider(realm) ?: return Response(Status.SERVICE_UNAVAILABLE)
        return provider(request)
    }

    fun listContactIds(userId: UserId) = getProvider(userId)!!.tokensDao.listContacts(userId).map { it.id }

    fun listMessages(user: User, since: Instant = startTime, limit: Int = 1000) = listMessages(user.id, since, limit)
    fun listMessages(userId: UserId, since: Instant = startTime, limit: Int = 1000) = getProvider(userId)!!.messagesDao.list(userId, since, limit).messages

    fun givenContacts(user1: User, user2: User) = givenContacts(user1.id, user2.id)
    fun givenContacts(user1: UserId, user2: UserId) {
        val token1 = nextToken()
        val token2 = nextToken()

        getProvider(user1)!!.tokensDao.createContact(user1, user2, accessToken = token2, contactToken = token1)
        getProvider(user2)!!.tokensDao.createContact(user2, user1, accessToken = token1, contactToken = token2)
    }
}