package io.andrewohara.tabbychat

import io.andrewohara.dynamokt.DataClassTableSchema
import io.andrewohara.tabbychat.protocol.v1.api.P2PApiV1
import io.andrewohara.tabbychat.protocol.v1.api.UserApiV1
import io.andrewohara.tabbychat.auth.*
import io.andrewohara.tabbychat.auth.dao.AuthorizationDao
import io.andrewohara.tabbychat.contacts.Contact
import io.andrewohara.tabbychat.contacts.ContactsDao
import io.andrewohara.tabbychat.contacts.Authorization
import io.andrewohara.tabbychat.messages.dao.DynamoMessage
import io.andrewohara.tabbychat.messages.dao.MessagesDao
import io.andrewohara.tabbychat.protocol.v1.client.P2PClientV1Factory
import io.andrewohara.tabbychat.users.User
import io.andrewohara.tabbychat.users.UserId
import io.andrewohara.tabbychat.users.dao.UsersDao
import io.andrewohara.utils.http4k.ContractUi
import org.http4k.client.JavaHttpClient
import org.http4k.contract.contract
import org.http4k.contract.openapi.ApiInfo
import org.http4k.contract.openapi.v3.OpenApi3
import org.http4k.contract.security.BearerAuthSecurity
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import java.time.Clock
import java.time.Duration

class TabbyChatProvider(
    clock: Clock,
    val realm: Realm,
    messagesTable: DynamoDbTable<DynamoMessage>,
    authTable: DynamoDbTable<Authorization>,
    usersTable: DynamoDbTable<User>,
    contactsTable: DynamoDbTable<Contact>,
    clientFactory: P2PClientV1Factory = P2PClientV1Factory { JavaHttpClient() },
    tokenGenerator: AccessTokenGenerator = AccessTokenGenerator.base36(16),
    messagePageSize: Int = 10,
    invitationDuration: Duration = Duration.ofHours(12)
): HttpHandler {

    val authDao = AuthorizationDao(authTable)
    val contactsDao = ContactsDao(contactsTable)
    val usersDao = UsersDao(usersTable)
    val messagesDao = MessagesDao(messagesTable)

    val service = TabbyChatService(
        realm = realm,
        clientFactory = clientFactory,
        auth = authDao,
        users = usersDao,
        messages = messagesDao,
        contacts = contactsDao,
        messagePageSize = messagePageSize,
        clock = clock,
        nextToken = tokenGenerator,
        invitationDuration = invitationDuration
    )

    val http: HttpHandler = let {
        val contexts = RequestContexts()

        val ownerLens = RequestContextKey.required<UserId>(contexts, "users")
        val ownerSecurity = BearerAuthSecurity(key = ownerLens, lookup = { tokenValue ->
            service.authorize(AccessToken(tokenValue))
                ?.takeIf { it.type == Authorization.Type.User }
                ?.bearer
        })

        val p2pAuthLens = RequestContextKey.required<Authorization>(contexts, "tokens")
        val p2pSecurity = BearerAuthSecurity(key = p2pAuthLens, lookup = { service.authorize(AccessToken(it)) })

        val p2pApiV1 = P2PApiV1(
            service = service,
            p2pSecurity = p2pSecurity,
            getAuth = p2pAuthLens,
        )

        val userApiV1 = UserApiV1(
            service = service,
            userSecurity = ownerSecurity,
            auth = ownerLens
        )

        val descriptionPath = "/swagger.json"
        val v1Contract = ContractUi(
            pageTitle = "TabbyChat Reference Provider",
            contract = contract {
                renderer = OpenApi3(
                    ApiInfo(
                        title = "TabbyChat Reference Provider",
                        version = "v1.0"
                    )
                )
                this.descriptionPath = descriptionPath
                routes += p2pApiV1.routes()
                routes += userApiV1.routes()
            },
            descriptionPath = descriptionPath
        )

        ServerFilters.InitialiseRequestContext(contexts)
            .then(v1Contract)
    }

    override fun invoke(request: Request) = http(request)

    companion object {
        fun fromEnv(
            env: Map<String, String>,
            dynamo: DynamoDbEnhancedClient = DynamoDbEnhancedClient.create()
        ): TabbyChatProvider {
            val realm = Realm(Uri.of(env.getValue("REALM")))
            val usersTableName = env.getValue("USERS_TABLE_NAME")
            val messagesTableName = env.getValue("MESSAGES_TABLE_NAME")
            val authTableName = env.getValue("AUTH_TABLE_NAME")
            val contactsTableName = env.getValue("CONTACTS_TABLE_NAME")

            return TabbyChatProvider(
                clock = Clock.systemUTC(),
                realm = realm,
                clientFactory = { JavaHttpClient() },
                usersTable = dynamo.table(usersTableName, DataClassTableSchema(User::class)),
                messagesTable = dynamo.table(messagesTableName, DataClassTableSchema(DynamoMessage::class)),
                authTable = dynamo.table(authTableName, DataClassTableSchema(Authorization::class)),
                contactsTable = dynamo.table(contactsTableName, DataClassTableSchema(Contact::class))
            )
        }
    }
}