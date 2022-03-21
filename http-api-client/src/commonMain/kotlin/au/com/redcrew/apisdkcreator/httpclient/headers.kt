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

// toAuthorisationHeader :: String -> String? -> HttpHeaders
private fun toAuthorisationHeader(value: String, headerName: String = "authorization"): HttpHeaders = mapOf(headerName to value)

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
 *
 * @param tokenFactory How to obtain a token to insert into a request
 * @param headerName The name of the "authorisation" header. Some servers are (incorrectly) case-sensitive.
 */
// bearerToken :: (() -> Either SdkError String) -> String? -> RequestHeaderFactory
fun bearerToken(
    tokenFactory: suspend () -> Either<SdkError, String>,
    headerName: String = "authorization"
): RequestHeaderFactory =
    { _ ->
        either {
            toAuthorisationHeader(toBearerToken(tokenFactory().bind()), headerName)
        }
    }

/**
 * Adds the given header to a [HttpRequest]
 */
// constantHeaders :: HttpHeaders -> RequestHeaderFactory
fun constantHeaders(headers: HttpHeaders): RequestHeaderFactory = { headers.right() }
