package io.andrewohara.tabbychat

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder
import io.andrewohara.lib.DynamoUtils
import io.andrewohara.tabbychat.auth.dao.DynamoTokenDao
import io.andrewohara.tabbychat.contacts.dao.DynamoContactsDao
import io.andrewohara.tabbychat.messages.dao.DynamoMessageDao
import io.andrewohara.tabbychat.users.dao.DynamoUserDao
import org.http4k.client.JavaHttpClient
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import java.time.Clock

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8000
    val realm = System.getenv("REALM")
    val contactsTableName = System.getenv("CONTACTS_TABLE_NAME")
    val usersTableName = System.getenv("USERS_TABLE_NAME")
    val messagesTableName = System.getenv("MESSAGES_TABLE_NAME")
    val tokensTableName = System.getenv("TOKENS_TABLE_NAME")


    val dynamo = AmazonDynamoDBClientBuilder.defaultClient()

    val service = ServiceFactory(
        clock = Clock.systemUTC(),
        realm = realm,
        clientBackend = JavaHttpClient(),
        contactsDao = DynamoContactsDao(DynamoUtils.mapper(contactsTableName, dynamo)),
        usersDao = DynamoUserDao(DynamoUtils.mapper(usersTableName, dynamo)),
        messagesDao = DynamoMessageDao(DynamoUtils.mapper(messagesTableName, dynamo)),
        tokensDao = DynamoTokenDao(DynamoUtils.mapper(tokensTableName, dynamo), realm)
    )

    val server = service.createHttp()
        .asServer(SunHttp(port))
        .start()
    println("Running $realm provider on port $port")
    server.block()
}