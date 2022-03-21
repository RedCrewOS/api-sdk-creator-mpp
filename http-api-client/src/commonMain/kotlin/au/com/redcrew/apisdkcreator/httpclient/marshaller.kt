package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.*
import arrow.core.computations.either
import arrow.core.computations.option
import au.com.redcrew.apisdkcreator.httpclient.kotlin.GenericTypeCurriedFunction

/**
 * A Marshaller is responsible converting a structured data type (eg: a data class) into [UnstructuredData] (eg: a String).
 */
typealias Marshaller = suspend (Any) -> Either<SdkError, UnstructuredData>

/**
 * An Unmarshaller is responsible for converting [UnstructuredData] to a type eg: a data class
 */
typealias Unmarshaller<T> = suspend (UnstructuredData) -> Either<SdkError, T>

/**
 * Transforms a [HttpResponse] of [UnstructuredData] to a HttpResponse of some type.
 */
typealias ResponseUnmarshaller<T> = suspend (HttpResponse<UnstructuredData>) ->
    Either<
        SdkError,
        Either<HttpResponse<UnstructuredData>, HttpResponse<T>>
    >

private fun <T> unsupportedContentType(
    response: Either<HttpResponse<UnstructuredData>, HttpResponse<T>>
): Either<SdkError, HttpResponse<T>> =
    // if we have a Left(HttpResponse<UnstructuredData>) then we need to convert that to an error.
    response.mapLeft { SdkError(UNMARSHALLING_ERROR_TYPE, "Unrecognised content type '${it.headers["content-type"]}'") }

/**
 * Most applications/SDKs accessing an API will want to use the same content type, so by having a curried function
 * an instance of the marshaller can be configured for the correct content type.
 *
 * The abstraction of how to actually convert between a type and [UnstructuredData] is so that users can wrap the
 * library of their choice.
 *
 * If an application/SDK wishes to support multiple marshallers to take advantage of content negotiation the
 * application will need to orchestrate the selection process.
 *
 * The result is a [HttpRequestPolicy] that will transform the body of the request (if present)
 * and add the Content-Type request header.
 *
 * @param contentType What content type are we creating the marshaller for?
 */
// marshallerFor :: String -> Marshaller -> HttpRequestPolicy<a, UnstructuredData>
fun marshallerFor(contentType: String): (Marshaller) -> HttpRequestPolicy<*, UnstructuredData> =
    { marshaller ->
        { request ->
            val result = option {
                val body = Option.fromNullable(request.body).bind()

                marshaller(body).map {
                    request.copyWithBody(
                        // FIXME: Merge headers
                        headers = mapOf("content-type" to contentType),
                        body = it
                    )
                }
            }

            result.fold(
                { Either.Right(request.copyWithBody(body = null)) },
                ::identity
            )
        }
    }

/**
 * Most applications/SDKs accessing an API will want to use the same content type, so by having a curried function
 * an instance of the unmarshaller can be configured for the correct content type.
 *
 * The abstraction of how to actually convert between [UnstructuredData] and another type is so that users can wrap the
 * library of their choice.
 *
 * We can't guarantee the content type of a response is something an unmarshaller can process, therefore the result is
 * either the original response unchanged (left), or the response with the body unmarshalled (right), or an error from
 * trying to unmarshall the response body.
 *
 * This allows for content negotiation where a chain of unmarshallers can be composed to handle different response
 * content types. It also caters for the scenarios, most often in corporate networks, where a misconfigured
 * gateway/endpoint returns a different content type due to it being misconfigured. Often this is HTML, whereas an
 * application/SDK might be expecting JSON/XML in the response.
 *
 * @param contentType What content type are we creating the unmarshaller for?
 */
// unmarshallerFor :: String -> Unmarshaller<T> -> ResponseUnmarshaller<T>
fun unmarshallerFor(contentType: String): TypedResponseUnmarshaller = TypedResponseUnmarshaller(contentType)

/**
 * An unmarshaller takes a sequence of functions that know how to unmarshall specific content types.
 *
 * Each function tries to unmarshall the UnstructuredData into some other (structured) type.
 * If no function succeeds, then an "Unsupported content type" error is returned.
 */
// unmarshaller :: List ResponseUnmarshaller<T> -> HttpResultHandler
fun <T> unmarshaller(unmarshallers: List<ResponseUnmarshaller<T>>): HttpResultHandler<*, UnstructuredData, T> =
    { result ->
        val response = result.response
        val initial: Either<SdkError, Either<HttpResponse<UnstructuredData>, HttpResponse<T>>> =
            Either.Right(Either.Left(response))

        /*
         * Recurse through the unmarshallers trying to unmarshall the UnstructuredData until one unmarshaller
         * succeeds, or no unmarshaller succeeds in which case we have an "unsupported content type error" result
         */
        unmarshallers
            .fold(initial) { acc, unmarshaller ->
                acc.flatMap { resp: Either<HttpResponse<UnstructuredData>, HttpResponse<T>> ->
                    /*
                     * If the response hasn't been unmarshalled (left) then try,
                     * else the response has (right) so just return it.
                     */
                    resp.fold(
                        { unmarshaller(it) },
                        { it.right().right() }
                    )
                }
            }
            .flatMap(::unsupportedContentType)
            .bimap(::identity) { resp -> result.copyWithResponse(resp) }
    }

fun <T> unmarshaller(vararg fns: ResponseUnmarshaller<T>): HttpResultHandler<*, UnstructuredData, T> =
    unmarshaller(fns.asList())

/**
 * Used to create a function that, given an [Unmarshaller], will return an [ResponseUnmarshaller] to try and unmarshall
 * the [HttpResponse] body.
 */
// TypedResponseUnmarshaller :: (Unmarshaller<T>) -> ResponseUnmarshaller<T>
class TypedResponseUnmarshaller(
    private val contentType: String
) : GenericTypeCurriedFunction {
    operator fun <T : Any> invoke(p1: Unmarshaller<T>): ResponseUnmarshaller<T> =
        { response: HttpResponse<UnstructuredData> ->
            either {
                when(val body = response.body) {
                    /*
                     * If we don't have a body to try to unmarshall, then we should consider the unmarshalling
                     * successful.
                     */
                    null -> response.copyWithBody<T>(body = null).right()
                    else -> when {
                        hasContentType(contentType, response) -> response.copyWithBody(body = p1(body).bind()).right()
                        else -> response.left()
                    }
                }
            }
        }
}
