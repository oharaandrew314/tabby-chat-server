package io.andrewohara.tabbychat

import io.andrewohara.awsmock.dynamodb.MockDynamoDbV2
import io.andrewohara.awsmock.dynamodb.backend.MockDynamoBackend
import io.andrewohara.dynamokt.DataClassTableSchema
import io.andrewohara.tabbychat.auth.dao.DynamoToken
import io.andrewohara.tabbychat.auth.dao.DynamoTokenDao
import io.andrewohara.tabbychat.contacts.dao.DynamoContact
import io.andrewohara.tabbychat.contacts.dao.DynamoContactsDao
import io.andrewohara.tabbychat.messages.dao.DynamoMessage
import io.andrewohara.tabbychat.messages.dao.DynamoMessageDao
import io.andrewohara.tabbychat.users.UserId
import io.andrewohara.tabbychat.users.dao.DynamoUser
import io.andrewohara.tabbychat.users.dao.DynamoUserDao
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import java.time.Clock
import java.time.Instant

class FakeService(val realm: String, clock: Clock, clientBackend: HttpHandler): HttpHandler {

    private val dynamo = DynamoDbEnhancedClient.builder()
        .dynamoDbClient(MockDynamoDbV2(MockDynamoBackend(clock)))
        .build()

    private val services = ServiceFactory(
        clock = clock,
        realm = realm,
        messagesDao = let {
            val table = dynamo.table("messages", DataClassTableSchema(DynamoMessage::class))
            table.createTable()
            DynamoMessageDao(table)
        },
        contactsDao = let {
            val table = dynamo.table("contacts", DataClassTableSchema(DynamoContact::class))
            table.createTable()
            DynamoContactsDao(table)
        },
        tokensDao = let {
            val table = dynamo.table("tokens", DataClassTableSchema(DynamoToken::class))
            table.createTable()
            DynamoTokenDao(table, realm)
        },
        usersDao = let {
            val table = dynamo.table("users", DataClassTableSchema(DynamoUser::class))
            table.createTable()
            DynamoUserDao(table)
        },
        clientBackend = clientBackend
    )

    private val http: HttpHandler = services.createHttp()

    override fun invoke(request: Request) = http(request)
    operator fun invoke(userId: UserId) = FakeServiceClient(services, userId)

    fun createUser(id: String) = services.userService.create(id = id, name = null, photo = null)
    fun listContactIds(userId: UserId) = services.contactService.listContacts(userId).map { it.userId }
    fun listMessages(userId: UserId, start: Instant, end: Instant) = services.messageService.listMessages(userId, start, end)
}