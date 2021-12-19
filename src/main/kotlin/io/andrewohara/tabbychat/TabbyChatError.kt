package io.andrewohara.tabbychat

import dev.forkhandles.result4k.Failure

enum class TabbyChatError {
    NotContact,
    NotFound,
    Forbidden;

    fun err() = Failure(this)
}