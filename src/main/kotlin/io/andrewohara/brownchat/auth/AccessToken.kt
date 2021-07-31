package io.andrewohara.brownchat.auth

data class AccessToken(
    val value: String,
    val realm: String
)