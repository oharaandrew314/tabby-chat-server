package io.andrewohara.brownchat

import com.github.michaelbull.result.*
import io.andrewohara.brownchat.users.UserId
import io.andrewohara.lib.IncrementOnGetClock
import org.http4k.core.HttpHandler
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import java.lang.RuntimeException

class TestDriver: HttpHandler {

    private val clock = IncrementOnGetClock()
    private val startTime = clock.instant()

    private val services = mutableListOf<FakeService>()

    private val serviceLookup: (Request) -> FakeService? = { request ->
        services.firstOrNull { it.realm == request.uri.host }
    }

   fun createService(realm: String): FakeService {
       val service = FakeService(realm, clock, ::invoke)
       services += service
       return service
   }

    operator fun invoke(userId: UserId) = getServiceClient(userId)

    private fun getService(userId: UserId) = services.first { it.realm == userId.realm }
    private fun getServiceClient(userId: UserId) = getService(userId)(userId)

    override fun invoke(request: Request): Response {
        val service = serviceLookup(request) ?: return Response(Status.SERVICE_UNAVAILABLE)
        return service(request)
    }

    fun listContactIds(userId: UserId) = getService(userId).listContactIds(userId)
    fun listMessages(userId: UserId) = getService(userId).listMessages(userId, startTime, clock.instant())

    fun givenContacts(user1: UserId, user2: UserId) {
        val invitation = invoke(user1).createInvitation()
        val result = invoke(user2).acceptInvitation(invitation)
        result.getError()?.let { error ->
            throw RuntimeException(error.toString())
        }
    }
}