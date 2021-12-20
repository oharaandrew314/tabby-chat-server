package io.andrewohara.tabbychat.protocol.v1

import org.http4k.core.Body
import org.http4k.format.Moshi.auto
import org.http4k.lens.*

object V1Lenses {
    val userId = Path.string().of("user_id")
    val accessToken = Path.nonEmptyString().of("access_token")

    val since = Query.instant().required("since")

    val messageContent = Body.auto<MessageContentDtoV1>().toLens()
    val messagePage = Body.auto<MessagePageDtoV1>().toLens()
    val tokenData = Body.auto<TokenDataDtoV1>().toLens()
    val user = Body.auto<UserDtoV1>().toLens()
    val messageReceipt = Body.auto<MessageReceiptDtoV1>().toLens()
    val userIds = Body.auto<Array<String>>().toLens()
}