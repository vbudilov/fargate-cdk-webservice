package com.budilov.search.cognito


import com.auth0.jwk.GuavaCachedJwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.slf4j.LoggerFactory
import java.net.URL
import java.security.interfaces.RSAKey


/**
 * Created by Vladimir Budilov
 */

object CognitoGateway {

    private val logger = LoggerFactory.getLogger("CognitoService")

    /**
     * Requires:
     * https://github.com/auth0/jwks-rsa-java
     *
     * Another option is to use this one:
     * https://github.com/jwtk/jjwt
     *
     */
    fun isTokenValid(token: String): Boolean {

        // Decode the key and set the kid
        val decodedJwtToken = JWT.decode(token)
        val kid = decodedJwtToken.keyId

        val http = UrlJwkProvider(URL(CognitoProperties.JWK_URL))
        // Let's cache the result from Cognito for the default of 10 hours
        val provider = GuavaCachedJwkProvider(http)
        val jwk = provider.get(kid)

        val algorithm = Algorithm.RSA256(jwk.publicKey as RSAKey)
        val verifier = JWT.require(algorithm)
                .withIssuer(CognitoProperties.JWT_TOKEN_ISSUER)
                .build() //Reusable verifier instance
        val jwtTokenIsValid = try {
            verifier.verify(token)
            true
        } catch (e: Exception) {
            false
        }

        logger.debug("Returning $jwtTokenIsValid")
        return jwtTokenIsValid
    }

    /**
     * Retrieve the cognito sub value from the id token
     */
    fun getCognitoSubId(idToken: String?): String {
        return JWT.decode(idToken).subject
    }
}

