package io.andrewohara.tabbychat.auth.dao

import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.contacts.Authorization
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key

class AuthorizationDao(private val table: DynamoDbTable<Authorization>) {

    operator fun plusAssign(auth: Authorization) {
        table.putItem(auth)
    }

    operator fun get(token: AccessToken): Authorization? = table.getItem(token.toKey())

    private fun AccessToken.toKey() = Key.builder().partitionValue(value).build()
}