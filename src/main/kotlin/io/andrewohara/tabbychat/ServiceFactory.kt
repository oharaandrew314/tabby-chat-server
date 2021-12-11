package io.andrewohara.tabbychat

import io.andrewohara.dynamokt.DataClassTableSchema
import io.andrewohara.tabbychat.auth.Authorization
import io.andrewohara.tabbychat.contacts.ContactService
import io.andrewohara.tabbychat.messages.MessageService
import io.andrewohara.tabbychat.api.v1.ContactApiV1
import io.andrewohara.tabbychat.api.v1.ProviderApiV1
import io.andrewohara.tabbychat.auth.AuthService
import io.andrewohara.tabbychat.auth.dao.DynamoToken
import io.andrewohara.tabbychat.auth.dao.DynamoTokenDao
import io.andrewohara.tabbychat.auth.dao.TokenDao
import io.andrewohara.tabbychat.contacts.dao.ContactsDao
import io.andrewohara.tabbychat.contacts.dao.DynamoContact
import io.andrewohara.tabbychat.contacts.dao.DynamoContactsDao
import io.andrewohara.tabbychat.messages.dao.DynamoMessage
import io.andrewohara.tabbychat.messages.dao.DynamoMessageDao
import io.andrewohara.tabbychat.messages.dao.MessageDao
import io.andrewohara.tabbychat.users.UserService
import io.andrewohara.tabbychat.users.dao.DynamoUser
import io.andrewohara.tabbychat.users.dao.DynamoUserDao
import io.andrewohara.tabbychat.users.dao.UsersDao
import org.http4k.client.JavaHttpClient
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey
import org.http4k.routing.bind
import org.http4k.routing.routes
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
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

    val authService = AuthService(
        tokenDao = tokensDao,
        realm = realm,
        clock = clock
    )

    fun createHttp(): HttpHandler {
        val contexts = RequestContexts()
        val authLens = RequestContextKey.required<Authorization>(contexts, "authorization")

//        val bearerAuthSecurity = BearerAuthSecurity(key = authLens as RequestContextLens<Any>, lookup = authLookup)
        val authFilter = ServerFilters.BearerAuth(authLens, authService::authorize)

        val protocolApiV1 = ProviderApiV1(
            realm = realm,
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
                    "/v1/messages" bind Method.POST to protocolApiV1::sendMessage
                )
            ),
            "/" bind Method.GET to protocolApiV1::spec,
            "/health" bind Method.GET to { Response(Status.OK).body("OK") }
        )

        return ServerFilters.InitialiseRequestContext(contexts)
//            .then(ServerFilters.Cors(corsPolicy))
            .then(routes)
    }

    companion object {
        fun fromEnv(
            env: Map<String, String>,
            dynamo: DynamoDbEnhancedClient = DynamoDbEnhancedClient.create()
        ): ServiceFactory {
            val realm = env.getValue("REALM")
            val contactsTableName = env.getValue("CONTACTS_TABLE_NAME")
            val usersTableName = env.getValue("USERS_TABLE_NAME")
            val messagesTableName = env.getValue("MESSAGES_TABLE_NAME")
            val tokensTableName = env.getValue("TOKENS_TABLE_NAME")

            return ServiceFactory(
                clock = Clock.systemUTC(),
                realm = realm,
                clientBackend = JavaHttpClient(),
                contactsDao = DynamoContactsDao(dynamo.table(contactsTableName, DataClassTableSchema(DynamoContact::class))),
                usersDao = DynamoUserDao(dynamo.table(usersTableName, DataClassTableSchema(DynamoUser::class))),
                messagesDao = DynamoMessageDao(dynamo.table(messagesTableName, DataClassTableSchema(DynamoMessage::class))),
                tokensDao = DynamoTokenDao(dynamo.table(tokensTableName, DataClassTableSchema(DynamoToken::class)), realm)
            )
        }
    }
}