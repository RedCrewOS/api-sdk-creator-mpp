package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either
import arrow.core.computations.either

typealias HttpHeaders = Map<String, String>

// concatHeaders :: RequestHeaderFactory -> HttpHeaders -> Either HttpHeaders
private val concatHeaders: suspend (RequestHeaderFactory) -> suspend (HttpHeaders) -> Either<Exception, HttpHeaders> =
    { factory -> { headers -> factory(headers).map { headers + it } }
}

// toBearerToken :: String -> String
private val toBearerToken: (String) -> String = { token -> "Bearer $token" }

// toAuthorisationHeader :: String -> HttpHeaders
private val toAuthorisationHeader: (String) -> HttpHeaders = { value -> mapOf("authorization" to value) }

/**
 * Creates a {@link RequestHeadersFactory} using {@link RequestHeaderFactory}s
 */
// createHeaders :: List RequestHeaderFactory -> RequestHeadersFactory
val createHeaders: suspend (List<RequestHeaderFactory>) -> RequestHeadersFactory = { factories: List<RequestHeaderFactory> ->
    {
        factories.fold(Either.Right(emptyMap())) {
            headers: Either<Exception, HttpHeaders>, factory: RequestHeaderFactory ->
                either {
                    concatHeaders(factory)(headers.bind()).bind()
                }
        }
    }
}

/**
 * Adds a bearer token to request headers
 */
// bearerToken :: (() -> Either Exception String) -> RequestHeaderFactory
val bearerToken: suspend (suspend () -> Either<Exception, String>) -> RequestHeaderFactory = { tokenFactory ->
    { _ ->
        either {
            toAuthorisationHeader(toBearerToken(tokenFactory().bind()))
        }
    }
}

/**
 * Adds the given header to a {@link HttpRequest}
 */
// constantHeaders :: HttpHeaders -> RequestHeaderFactory
val constantHeaders: suspend (HttpHeaders) -> RequestHeaderFactory = { headers -> { Either.Right(headers) } }
