package io.andrewohara.tabbychat.auth

import io.andrewohara.utils.IdGenerator

fun interface AccessTokenGenerator: () -> AccessToken {
    companion object {
        fun base36(length: Int) = AccessTokenGenerator { AccessToken(IdGenerator.nextBase36(length)) }
    }
}