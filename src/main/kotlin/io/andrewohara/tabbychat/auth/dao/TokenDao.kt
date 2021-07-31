package io.andrewohara.tabbychat.auth.dao

import io.andrewohara.tabbychat.auth.Authorization
import io.andrewohara.tabbychat.auth.AccessToken
import io.andrewohara.tabbychat.users.UserId
import java.time.Instant

interface TokenDao {

    fun generateUserToken(owner: UserId): AccessToken
    fun generateContactToken(owner: UserId, contact: UserId): AccessToken
    fun generateInvitationCode(owner: UserId, expires: Instant): AccessToken
    fun verify(token: AccessToken, time: Instant): Authorization?
    fun revoke(token: AccessToken)
}