package io.andrewohara.tabbychat

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import io.andrewohara.lib.DynamoUtils
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.Authorization
import io.andrewohara.tabbychat.contacts.ContactService
import io.andrewohara.tabbychat.messages.MessageService
import io.andrewohara.tabbychat.api.v1.ContactApiV1
import io.andrewohara.tabbychat.api.v1.ProviderApiV1
import io.andrewohara.tabbychat.auth.dao.DynamoTokenDao
import io.andrewohara.tabbychat.auth.dao.TokenDao
import io.andrewohara.tabbychat.contacts.dao.ContactsDao
import io.andrewohara.tabbychat.contacts.dao.DynamoContactsDao
import io.andrewohara.tabbychat.messages.dao.DynamoMessageDao
import io.andrewohara.tabbychat.messages.dao.MessageDao
import io.andrewohara.tabbychat.users.UserService
import io.andrewohara.tabbychat.users.dao.DynamoUserDao
import io.andrewohara.tabbychat.users.dao.UsersDao
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey
import org.http4k.routing.bind
import org.http4k.routing.routes
import java.time.Clock

class ServiceFactory(
    clock: Clock,
    val realm: String,
    messagesDao: MessageDao,
    contactsDao: ContactsDao,
    tokensDao: TokenDao,
    usersDao: UsersDao,
    clientBackend: HttpHandler
) {
    private val client = ContactClient(clientBackend)

    val messageService = MessageService(
        clock = clock,
        dao = messagesDao,
        contactsDao = contactsDao,
        client = client
    )

    val contactService = ContactService(
        clock = clock,
        client = client,
        contactsDao = contactsDao,
        tokensDao = tokensDao
    )

    val userService = UserService(
        realm = realm,
        dao = usersDao
    )

    private val authLookup: (String) -> Authorization? = { value ->
        val token = AccessToken(realm = realm, value = value)
        tokensDao.verify(token, clock.instant())
    }

    fun createHttp(): HttpHandler {
        val contexts = RequestContexts()
        val authLens = RequestContextKey.required<Authorization>(contexts, "authorization")

//        val bearerAuthSecurity = BearerAuthSecurity(key = authLens as RequestContextLens<Any>, lookup = authLookup)
        val authFilter = ServerFilters.BearerAuth(authLens, authLookup)

        val protocolApiV1 = ProviderApiV1(
            messageService = messageService,
            contactServices = contactService,
            authLens = authLens
        )

        val contactApiV1 = ContactApiV1(
            contactService = contactService,
            messageService = messageService,
            userService = userService,
            authLens = authLens
        )

        val routes = routes(
            authFilter.then(
                routes(
                    ContactApiV1.invitationsPath bind Method.POST to contactApiV1::completeInvitation,
                    ContactApiV1.userPath bind Method.GET to contactApiV1::lookupUser,
                    ContactApiV1.messagesPath bind Method.POST to contactApiV1::receiveMessage,
                    "/v1/contacts" bind Method.POST to protocolApiV1::createInvitation,
                    "/v1/contacts" bind Method.PUT to protocolApiV1::acceptInvitation,
                    "/v1/messages" bind Method.GET to protocolApiV1::listMessages,
                    "/v1/messages" bind Method.POST to protocolApiV1::sendMessage,
                )
            ),
            "/health" bind Method.GET to { Response(Status.OK).body("OK") }
        )

        return ServerFilters.InitialiseRequestContext(contexts)
//            .then(ServerFilters.Cors(corsPolicy))
            .then(routes)
    }

    companion object {
        fun fromEnv(env: Map<String, String>, dynamo: AmazonDynamoDB = AmazonDynamoDBClientBuilder.defaultClient()): ServiceFactory {
            val realm = env.getValue("REALM")
            val contactsTableName = env.getValue("CONTACTS_TABLE_NAME")
            val usersTableName = env.getValue("USERS_TABLE_NAME")
            val messagesTableName = env.getValue("MESSAGES_TABLE_NAME")
            val tokensTableName = env.getValue("TOKENS_TABLE_NAME")

            return ServiceFactory(
                clock = Clock.systemUTC(),
                realm = realm,
                clientBackend = JavaHttpClient(),
                contactsDao = DynamoContactsDao(DynamoUtils.mapper(contactsTableName, dynamo)),
                usersDao = DynamoUserDao(DynamoUtils.mapper(usersTableName, dynamo)),
                messagesDao = DynamoMessageDao(DynamoUtils.mapper(messagesTableName, dynamo)),
                tokensDao = DynamoTokenDao(DynamoUtils.mapper(tokensTableName, dynamo), realm)
            )
        }
    }
}