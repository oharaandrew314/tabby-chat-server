package io.andrewohara.tabbychat.auth

data class AccessToken(
    val value: String,
    val realm: String
)