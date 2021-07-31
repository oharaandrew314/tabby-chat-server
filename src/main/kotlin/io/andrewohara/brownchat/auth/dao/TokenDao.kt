package io.andrewohara.brownchat.auth.dao

import io.andrewohara.brownchat.auth.Authorization
import io.andrewohara.brownchat.auth.AccessToken
import io.andrewohara.brownchat.users.UserId
import java.time.Instant

interface TokenDao {

    fun generateUserToken(owner: UserId): AccessToken
    fun generateContactToken(owner: UserId, contact: UserId): AccessToken
    fun generateInvitationCode(owner: UserId, expires: Instant): AccessToken
    fun verify(token: AccessToken, time: Instant): Authorization?
    fun revoke(token: AccessToken)
}