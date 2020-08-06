package com.budilov.search

import com.amazonaws.regions.Regions
import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest
import org.slf4j.LoggerFactory
import java.util.*

object AppProperties {
    private val log = LoggerFactory.getLogger("AppProperties")

    private val parameters: Map<String, String>
    private val parameterNames = arrayListOf("webServiceECR")

    init {
        parameters = loadParametersFromParameterStore()
    }

    // Environment variables
    val MY_SERVICE_PORT: Int = try {
        System.getenv("MY_SERVICE_PORT").toInt()
    } catch (e: Exception) {
        throw Exception("Couldn't retrieve the service port...can't function without it")
    }

    val ECR_LOCATION = parameters["webServiceECR"]

    /**
     * Load the parameters from the parameter store.
     *
     * Note: defaults to us-east-1
     */
    private fun loadParametersFromParameterStore(): Map<String, String> {
        val client = AWSSimpleSystemsManagementClientBuilder.standard()
                .withRegion(Regions.US_EAST_1)
                .build()
        val request = GetParametersRequest()
        request.withNames(parameterNames).isWithDecryption = true
        log.info("Calling client")

        val result = client.getParameters(request)

        val store = HashMap<String, String>()

        result?.parameters?.forEach { parameter ->

            if (parameter?.value.isNullOrBlank())
                throw Exception("Couldn't load the parameter ${parameter?.name}. Won't work without it.")

            log.info("Retrieved ${parameter.name}")
            store[parameter.name] = parameter.value
        }

        return store
    }


}
