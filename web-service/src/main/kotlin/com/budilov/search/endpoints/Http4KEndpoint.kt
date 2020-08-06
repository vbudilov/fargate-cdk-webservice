package com.budilov.search.endpoints

import com.budilov.search.AppProperties
import com.budilov.search.cognito.CognitoGateway
import com.budilov.search.endpoints.resolvers.*
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.OK
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.slf4j.LoggerFactory

val log = LoggerFactory.getLogger("Main")

fun main() {


    val pingPongHandler: HttpHandler = { _ -> Response(OK).body("pong!") }
    val ping = "/ping" bind GET to pingPongHandler

    /**
     * Auth filter...performs the pre-processing on every request to make sure that the passed-in JWT is valid
     *
     */
    val authFilter = Filter { next: HttpHandler ->
        { req: Request ->
            log.debug("in filter")

            var response = Response(FORBIDDEN)

            // Get the auth token (cognito)
            val authToken = req.header("Authorization")

            // Check if it's valid...if not then fail
            if (authToken != null && CognitoGateway.isTokenValid(authToken)) {
                log.debug("token is valid..proceed")
                response = next(req)
            } else {
                log.warn("Token isn't provided or isn't valid")
                response.body("Check your JWT tokens - you might need to re-authorize.")
            }
            response
        }
    }

    val routes: HttpHandler = routes(
            ping,
            MetaResolver.showRoute
    )

    // Start the process
    ServerFilters.Cors(CorsPolicy(headers = listOf("content-type", "query", "X-Request-Id"),
            methods = listOf(Method.GET, Method.POST, Method.PUT, Method.DELETE, Method.OPTIONS,
                    Method.TRACE, Method.PATCH, Method.PURGE, Method.HEAD),
            origins = listOf("*")))
            .then(routes)
            .asServer(Jetty(AppProperties.MY_SERVICE_PORT))
            .start()

}
