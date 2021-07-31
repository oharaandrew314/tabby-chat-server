package io.andrewohara.brownchat.users.dao

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverted
import io.andrewohara.brownchat.users.RealName
import io.andrewohara.brownchat.users.User
import io.andrewohara.brownchat.users.UserId
import io.andrewohara.lib.UserIdConverter
import java.net.URL

@DynamoDBDocument
data class DynamoUser(
    @DynamoDBHashKey
    @DynamoDBTypeConverted(converter = UserIdConverter::class)
    var id: UserId? = null,

    var firstName: String? = null,
    var middleName: String? = null,
    var lastName: String? = null,
    var photoUrl: String? = null
) {
    constructor(user: User): this(
        id = user.id,
        firstName = user.name?.first,
        middleName = user.name?.middle,
        lastName = user.name?.last,
        photoUrl = user.icon?.toString(),
    )

    fun toUser() = User(
        id = id!!,
        name = firstName?.let { RealName(it, middleName, lastName) },
        icon = photoUrl?.let { URL(it) }
    )
}