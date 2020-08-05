package com.crosscaster.search.cognito

import com.amazonaws.services.simplesystemsmanagement.AWSSimpleSystemsManagementClientBuilder
import com.amazonaws.services.simplesystemsmanagement.model.GetParametersRequest
import java.util.*


/**
 * Created by Vladimir Budilov
 *
 * Externalizing of app properties. Will be handy when writing unit tests and de-coupling
 * the storage of properties
 */

object CognitoProperties {

    private const val REGION = "us-east-1"
    private val parameters: Map<String, String>
    private val parameterNames = arrayListOf("bnCognitoUserPoolId")

    init {
        parameters = loadParameters()
    }

    private val COGNITO_USER_POOL_ID: String = parameters["bnCognitoUserPoolId"] ?: error("")

    val JWK_URL = "https://cognito-idp.${REGION}.amazonaws.com/$COGNITO_USER_POOL_ID/.well-known/jwks.json"
    val JWT_TOKEN_ISSUER = "https://cognito-idp.${REGION}.amazonaws.com/$COGNITO_USER_POOL_ID"

    private fun loadParameters(): Map<String, String> {
        val client = AWSSimpleSystemsManagementClientBuilder.standard().withRegion(REGION).build()

        val request = GetParametersRequest()
        request.withNames(CognitoProperties.parameterNames)

        val result = client.getParameters(request)

        val store = HashMap<String, String>()

        result.parameters.forEach { parameter ->
            if (parameter == null || parameter.value.isNullOrBlank())
                throw Exception("Couldn't load required parameters from SSM...")

            store[parameter.name] = parameter.value
        }

        return store
    }
}
