package io.andrewohara.tabbychat.lib.dao

import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.users.UserId
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

class UserIdConverter: AttributeConverter<UserId> {

    override fun transformFrom(id: UserId?): AttributeValue = AttributeValue.builder().let {
        if (id == null) it.nul(true) else it.s(id.value)
        it.build()
    }

    override fun transformTo(input: AttributeValue) = UserId(input.s())

    override fun type(): EnhancedType<UserId> = EnhancedType.of(UserId::class.java)

    override fun attributeValueType() = AttributeValueType.S
}

class AccessTokenConverter: AttributeConverter<AccessToken> {

    override fun transformFrom(input: AccessToken?): AttributeValue = AttributeValue.builder().let {
        if (input == null) it.nul(true) else it.s(input.value)
        it.build()
    }

    override fun transformTo(input: AttributeValue) = AccessToken(input.s())

    override fun type(): EnhancedType<AccessToken> = EnhancedType.of(AccessToken::class.java)

    override fun attributeValueType(): AttributeValueType = AttributeValueType.S
}

fun UserId.toKey(): Key = Key.builder()
    .partitionValue(value)
    .build()

fun UserId.toKey(sort: UserId): Key = Key.builder()
    .partitionValue(value)
    .sortValue(sort.value)
    .build()