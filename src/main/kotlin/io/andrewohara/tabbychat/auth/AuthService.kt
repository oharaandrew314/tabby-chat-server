package io.andrewohara.tabbychat.auth

import io.andrewohara.tabbychat.auth.dao.TokensDao
import io.andrewohara.tabbychat.users.UserId
import java.time.Clock

class AuthService(
    private val tokensDao: TokensDao,
    private val tokenGenerator: AccessTokenGenerator,
    private val clock: Clock
) {

    fun authorize(token: String): Authorization? {
        return tokensDao.verify(AccessToken(token), clock.instant())
    }

    fun createUserToken(userId: UserId): AccessToken {
        val token = tokenGenerator()
        tokensDao.saveUserToken(userId, token)
        return token
    }
}