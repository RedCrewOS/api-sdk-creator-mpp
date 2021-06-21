package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.*

/**
 * Helper to get the response in a {@link HttpResult}
 *
 * @param {HttpResult} result
 * @return {HttpResponse} A response
 */
// getHttpResponse :: HttpResult -> HttpResponse
val getHttpResponse: (HttpResult<*, *>) -> HttpResponse<*> = { result -> result.response }

/**
 * Helper to get the body out of a {@link HttpResponse}
 *
 * @param {HttpResponse} response
 * @return {any} The body. Maybe undefined
 */
// getHttpBody :: HttpResponse -> a
val getHttpBody: (HttpResponse<*>) -> Any? = { response -> response.body }

// extractHttpBody :: HttpResult -> a
val extractHttpBody = getHttpBody compose getHttpResponse

// parseIntValue :: a -> Either Exception Integer
val parseIntValue = { a: String -> Either.catch { Integer.parseInt(a) } }

/**
 * Tries to take a header value and parse it to an int.
 *
 * Returns an Error if no headers are given, or if the header value is NaN.
 *
 * Will ignore a missing header as not all headers are returned under all circumstances and that
 * might be OK (eg: content-length). Use `toEither` to force an error if the header is
 * missing.
 */
// parseIntHeader :: (String, HttpHeaders) -> Either Exception Option Integer
val parseIntHeader = { header: String, headers: HttpHeaders ->
    Option.fromNullable(headers[header])
        .map(parseIntValue)
        .sequenceEither()
}
