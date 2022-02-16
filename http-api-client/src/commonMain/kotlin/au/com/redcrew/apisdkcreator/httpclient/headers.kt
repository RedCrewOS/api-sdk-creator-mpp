package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.right

typealias HttpHeaders = Map<String, String>

// concatHeaders :: RequestHeaderFactory -> HttpHeaders -> Either HttpHeaders
private fun concatHeaders(factory: RequestHeaderFactory): suspend (HttpHeaders) -> Either<SdkError, HttpHeaders> =
    { headers -> factory(headers).map { headers + it }
}

// toBearerToken :: String -> String
private fun toBearerToken(token: String): String = "Bearer $token"

// toAuthorisationHeader :: String -> HttpHeaders
private fun toAuthorisationHeader(value: String): HttpHeaders = mapOf("authorization" to value)

/**
 * Creates a {@link RequestHeadersFactory} using {@link RequestHeaderFactory}s
 */
// createHeaders :: List RequestHeaderFactory -> RequestHeadersFactory
fun createHeaders(factories: List<RequestHeaderFactory>): RequestHeadersFactory =
    {
        factories.fold(Either.Right(emptyMap())) {
            headers: Either<SdkError, HttpHeaders>, factory: RequestHeaderFactory ->
                either {
                    concatHeaders(factory)(headers.bind()).bind()
                }
        }
    }

fun createHeaders(vararg fns: RequestHeaderFactory): RequestHeadersFactory =
    createHeaders(fns.asList())

/**
 * Adds a bearer token to request headers
 */
// bearerToken :: (() -> Either SdkError String) -> RequestHeaderFactory
fun bearerToken(tokenFactory: suspend () -> Either<SdkError, String>): RequestHeaderFactory =
    { _ ->
        either {
            toAuthorisationHeader(toBearerToken(tokenFactory().bind()))
        }
    }

/**
 * Adds the given header to a {@link HttpRequest}
 */
// constantHeaders :: HttpHeaders -> RequestHeaderFactory
fun constantHeaders(headers: HttpHeaders): RequestHeaderFactory = { headers.right() }
