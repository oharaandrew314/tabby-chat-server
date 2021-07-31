package io.andrewohara.brownchat.api.v1

import io.andrewohara.brownchat.contacts.ContactError
import org.http4k.core.Response
import org.http4k.core.Status

object ErrorMapper {

    fun ContactError.toResponse() = when(this) {
        is ContactError.InvitationRejected -> Response(Status.UNAUTHORIZED).body("inviter has rejected your acceptance")
        is ContactError.InvalidContact -> Response(Status.BAD_REQUEST).body("invalid user id")
        is ContactError.AlreadyContact -> Response(Status.CONFLICT).body("contact already exists")
    }
}