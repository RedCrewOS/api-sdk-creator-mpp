package au.com.redcrew.apisdkcreator.httpclient

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
data class HttpRequest<T : Any>(
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
}
