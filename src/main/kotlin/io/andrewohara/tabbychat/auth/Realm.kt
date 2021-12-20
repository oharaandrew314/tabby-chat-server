package io.andrewohara.tabbychat.auth

import org.http4k.core.Uri
import org.http4k.core.queries

@JvmInline value class Realm(val value: Uri) {
    init {
        require(value.scheme.isNotEmpty()) { "realm must contain a URI scheme" }
        require(value.host.isNotEmpty()) { "realm must contain a hostname" }
        require(value.path.isEmpty()) { "realm must not have a path "}
        require(value.queries().isEmpty()) { "realm must have no query args" }
    }
}