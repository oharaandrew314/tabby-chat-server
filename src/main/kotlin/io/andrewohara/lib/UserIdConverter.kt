package io.andrewohara.lib

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter
import io.andrewohara.brownchat.users.UserId

class UserIdConverter: DynamoDBTypeConverter<String, UserId> {
    override fun convert(id: UserId) = "${id.realm}:${id.id}"
    override fun unconvert(serialized: String): UserId {
        val (realm, id) = serialized.split(":")
        return UserId(realm = realm, id = id)
    }
}