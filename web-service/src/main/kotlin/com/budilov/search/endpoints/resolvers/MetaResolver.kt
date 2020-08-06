package com.budilov.search.endpoints.resolvers

import com.budilov.search.AppProperties
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.slf4j.LoggerFactory

object MetaResolver {
    private val log = LoggerFactory.getLogger("ShowResolvers")

    val showRoute = "/meta" bind routes(
            "/ecr" bind Method.GET to { req ->
                Response(Status.ACCEPTED).body("ECR -> ${AppProperties.ECR_LOCATION}")
            }
    )
}
