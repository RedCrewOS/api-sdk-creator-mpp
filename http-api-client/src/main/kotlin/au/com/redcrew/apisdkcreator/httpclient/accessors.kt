package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.*
import java.lang.Exception

/**
 * Helper to get the response in a {@link HttpResult}
 *
 * @param {HttpResult} result
 * @return {HttpResponse} A response
 */
// getHttpResponse :: HttpResult -> HttpResponse
fun <T> getHttpResponse(result: HttpResult<*, T>): HttpResponse<T> = result.response

/**
 * Helper to get the body out of a {@link HttpResponse}
 *
 * @param {HttpResponse} response
 * @return {any} The body. Maybe undefined
 */
// getHttpBody :: HttpResponse -> a
fun <T> getHttpBody(response: HttpResponse<T>): T? = response.body

// extractHttpBody :: HttpResult -> a
fun <T> extractHttpBody(result: HttpResult<*, T>): T? = getHttpBody(getHttpResponse(result))

// parseIntValue :: a -> Either Exception Integer
fun parseIntValue(a: String): Either<Exception, Int> = Either.catch({ t -> t as Exception }, { Integer.parseInt(a) })

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
fun parseIntHeader(header: String, headers: HttpHeaders): Either<Exception, Option<Int>> =
    Option.fromNullable(headers[header])
        .map(::parseIntValue)
        .sequenceEither()
