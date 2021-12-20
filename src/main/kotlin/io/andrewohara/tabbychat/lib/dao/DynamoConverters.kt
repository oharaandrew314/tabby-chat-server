package io.andrewohara.tabbychat.lib.dao

import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.auth.Realm
import io.andrewohara.tabbychat.users.UserId
import org.http4k.core.Uri
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.services.dynamodb.model.AttributeValue
import java.time.Instant

class UserIdConverter: AttributeConverter<UserId> {

    override fun transformFrom(id: UserId?): AttributeValue = AttributeValue.builder().let {
        if (id == null) it.nul(true) else it.s(id.value)
        it.build()
    }

    override fun transformTo(input: AttributeValue) = UserId(input.s())

    override fun type(): EnhancedType<UserId> = EnhancedType.of(UserId::class.java)

    override fun attributeValueType() = AttributeValueType.S
}

class RealmConverter: AttributeConverter<Realm> {

    override fun transformFrom(id: Realm?): AttributeValue = AttributeValue.builder().let {
        if (id == null) it.nul(true) else it.s(id.value.toString())
        it.build()
    }

    override fun transformTo(input: AttributeValue) = Realm(Uri.of(input.s()))

    override fun type(): EnhancedType<Realm> = EnhancedType.of(Realm::class.java)

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

class InstantAsLongConverter: AttributeConverter<Instant> {

    override fun transformFrom(input: Instant?): AttributeValue = AttributeValue.builder().let {
        if (input == null) it.nul(true) else it.n(input.epochSecond.toString())
        it.build()
    }

    override fun transformTo(input: AttributeValue): Instant = Instant.ofEpochSecond(input.n().toLong())

    override fun type(): EnhancedType<Instant> = EnhancedType.of(Instant::class.java)

    override fun attributeValueType(): AttributeValueType = AttributeValueType.N
}

fun UserId.toKey(): Key = Key.builder()
    .partitionValue(value)
    .build()

fun UserId.toKey(sort: UserId): Key = Key.builder()
    .partitionValue(value)
    .sortValue(sort.value)
    .build()