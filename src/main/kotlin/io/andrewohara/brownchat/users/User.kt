package io.andrewohara.brownchat.users

import java.net.URL

data class User(
    val id: UserId,
    val name: RealName?,
    val icon: URL?
)