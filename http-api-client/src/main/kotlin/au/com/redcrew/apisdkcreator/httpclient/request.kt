package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either
import arrow.core.computations.either

/**
 * Defines the various HTTP request methods (verbs)
 */
enum class HttpRequestMethod {
    GET,
    HEAD,
    POST,
    PUT,
    DELETE,
    CONNECT,
    OPTIONS,
    TRACE,
    PATCH
}

sealed class HttpRequestUrl {
    data class URL(val url: java.net.URL) : HttpRequestUrl()
    data class String(val url: kotlin.String) : HttpRequestUrl()
}

/**
 * In order to be as generic as possible properties on the request try to align with the
 * underlying structure of an HTTP request.
 *
 * Adapters will have to map properties to the request object structure used by the underlying
 * HTTP client library.
 */
data class HttpRequest<T>(
    val method: HttpRequestMethod,
    val url: HttpRequestUrl,
    val headers: HttpHeaders,

    /** Used to replace path parameters/slugs in the request url */
    val pathParams: Map<String, String>?,

    val queryParams: Map<String, String>?,

    val body: T?
) {
    constructor(
        method: HttpRequestMethod,
        url: HttpRequestUrl,
        headers: HttpHeaders
    ) : this(method, url, headers, null, null, null)

    constructor(
        method: HttpRequestMethod,
        url: HttpRequestUrl
    ) : this(method, url, emptyMap(), null, null, null)
}

/**
 * Factory to create request headers.
 *
 * Creating some headers (eg: Authorization) may require async work to be done (ie: fetching an access token).
 *
 * @param headers To be used if one header value depends on another.
 * @returns A new set of headers.
 */
typealias RequestHeaderFactory = suspend (headers: HttpHeaders) -> Either<Exception, HttpHeaders>

/**
 * Factory definition to create a set of HTTP headers.
 *
 * Creating some headers (eg: Authorization) may require async work to be done (ie: fetching an access token).
 *
 * @typedef {function} RequestHeadersFactory
 * @returns A new set of headers.
 */
typealias RequestHeadersFactory = suspend () -> Either<Exception, HttpHeaders>

/**
 * Creates a {@link HttpRequestPolicy} to add headers to a request
 */
// addHeaders :: RequestHeadersFactory -> HttpRequestPolicy
val addHeaders: suspend (RequestHeadersFactory) -> HttpRequestPolicy<*, *> = { factory -> { request ->
    either {
        val headers: HttpHeaders = factory().bind()

        request.copy(headers = request.headers + headers)
    }
} }

@Suppress("ThrowableNotThrown")
val resolveUrl: suspend (String) -> HttpRequestPolicy<*, *> = { base -> { request ->
    either {
        val url = when(request.url) {
            is HttpRequestUrl.String -> Either.Right("${base}${request.url.url}")
            else -> Either.Left(IllegalArgumentException("Can't resolve a URL"))
        }.bind()

        request.copy(url = HttpRequestUrl.String(url))
    }
}}
