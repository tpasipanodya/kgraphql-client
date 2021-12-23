package io.taff.kgraphql.client

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Request
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.result.Result

object Config {

	/** Called before a request is executed */
	var onRequest: (request: Request) -> Unit = { _ -> }

	/** Called after a request has been executed */
	var onResponse: (request: Response, response: Result<String, FuelError>) -> Unit = { _, _ -> }


	/** used for serializing objects for logging as well as deserializing json into lists and maps. */
	var objectMapper = jacksonObjectMapper().apply {
		configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
		registerModule(JavaTimeModule())
	}
}

/**
 * Configure the client, e.g:
 *
 * ```
 * val configured = configure { logGraphqlClientRequests = true }
 * ```
 */
fun configure(fxn: Config.() -> Unit) = Config.apply(fxn)
