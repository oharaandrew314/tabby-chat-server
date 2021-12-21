package io.andrewohara.tabbychat.users

import org.http4k.core.Uri

data class User(
    val id: UserId,
    val name: RealName?,
    val icon: Uri?
)