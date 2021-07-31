package io.andrewohara.brownchat

import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
import io.andrewohara.awsmock.dynamodb.MockAmazonDynamoDB
import io.andrewohara.brownchat.auth.dao.DynamoToken
import io.andrewohara.brownchat.auth.dao.DynamoTokenDao
import io.andrewohara.brownchat.contacts.dao.DynamoContact
import io.andrewohara.brownchat.contacts.dao.DynamoContactsDao
import io.andrewohara.brownchat.messages.dao.DynamoMessage
import io.andrewohara.brownchat.messages.dao.DynamoMessageDao
import io.andrewohara.brownchat.users.UserId
import io.andrewohara.brownchat.users.dao.DynamoUser
import io.andrewohara.brownchat.users.dao.DynamoUserDao
import io.andrewohara.lib.DynamoUtils
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import java.time.Clock
import java.time.Instant

class FakeService(val realm: String, clock: Clock, clientBackend: HttpHandler): HttpHandler {

    private val dynamo = MockAmazonDynamoDB()

    private val services = ServiceFactory(
        clock = clock,
        realm = realm,
        messagesDao = DynamoUtils.mapper<DynamoMessage, String, Long>("messages", dynamo).let {
            it.createTable(ProvisionedThroughput(1, 1))
            DynamoMessageDao(it)
        },
        contactsDao = DynamoUtils.mapper<DynamoContact, UserId, UserId>("contacts", dynamo).let {
            it.createTable(ProvisionedThroughput(1, 1))
            DynamoContactsDao(it)
        },
        tokensDao = DynamoUtils.mapper<DynamoToken, String, Unit>("tokens", dynamo).let {
            it.createTable(ProvisionedThroughput(1, 1))
            DynamoTokenDao(it, realm)
        },
        usersDao = DynamoUtils.mapper<DynamoUser, UserId, Unit>("users", dynamo).let {
            it.createTable(ProvisionedThroughput(1, 1))
            DynamoUserDao(it)
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