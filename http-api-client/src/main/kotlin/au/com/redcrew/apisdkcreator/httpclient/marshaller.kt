package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.*
import arrow.core.computations.either
import arrow.core.computations.option

typealias Marshaller = suspend (Any) -> Either<Exception, UnstructuredData>
typealias Unmarshaller<T> = suspend (UnstructuredData) -> Either<Exception, T>

// split :: String -> String -> List String
private fun split(delimiter: String): (String) -> List<String> = { str -> str.split(delimiter) }

// takeFirst :: List a -> a
private fun <T> takeFirst(lst: List<T>): T = lst.first()

// trim :: String -> String
private fun trim(str: String) = str.trim()

private fun isSame(a: Any): (b: Any) -> Boolean = { b -> a == b }

// hasContentType :: String -> HttpResult -> Boolean
private fun hasContentType(contentType: String, response: HttpResponse<*>): Boolean =
    Option.fromNullable(response.headers["content-type"])
       .map(split(";") andThen ::takeFirst andThen ::trim andThen isSame(contentType))
       .getOrElse { false }

private fun <T> unsupportedContentType(response: HttpResponse<UnstructuredData>): (HttpResponse<T>) -> Either<Exception, HttpResponse<T>> =
    { unmarshalledResponse ->
        Either.conditionally(
            response.body == null || unmarshalledResponse.body != null,
            { IllegalStateException("Unrecognised content type '${response.headers["content-type"]}'") },
            { unmarshalledResponse }
        )
    }

/**
 * Most applications/SDKs accessing an API will want to use the same content type, so by having a curried function
 * an instance of the marshaller can be configured for the correct content type.
 *
 * The abstraction of how to actually convert between a type and an Unstructured data type is so that users can wrap the
 * library of their choice.
 *
 * If an application/SDK wishes to support multiple marshallers to take advantage of content negotiation the
 * application will need to orchestrate the selection process.
 */
fun marshallerFor(contentType: String): (Marshaller) -> HttpRequestPolicy<*, UnstructuredData> =
    { marshaller ->
        { request ->
            val result = option {
                marshaller(Option.fromNullable(request.body).bind()).map {
                    data -> request.copyWithBody(headers = mapOf("content-type" to contentType), body = data)
                }.bind()
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
 * The abstraction of how to actually convert between Unstructured data and another type is so that users can wrap the
 * library of their choice.
 *
 * We can't guarantee the content type of the response is something an unmarshaller can handle, therefore it might
 * not be able to process the content type and therefore has to return nothing. This allows for content negotiation
 * where a chain of unmarshallers can be composed to handle different response types. It also caters for the scenarios,
 * most often in corporate networks, where a misconfigured gateway/endpoint returns a different content type due to it
 * being misconfigured. Often this is HTML, where as an application/SDK might be expecting JSON/XML in the response.
 */
fun <T> unmarshallerFor(contentType: String): (Unmarshaller<T>) -> HttpResponseHandler<UnstructuredData, T> =
    { unmarshaller ->
        { response ->
            either {
                val body: T? =
                    if (hasContentType(contentType, response)) { response.body?.let { unmarshaller(it).bind() } }
                    else { null }

                response.copyWithBody(body = body)
            }
        }
    }

/**
 * An unmarshaller takes a sequence of functions that know how to unmarshall specific content types.
 *
 * Each function tries to unmarshall the UnstructuredData into some other (structured) type.
 * If no function succeeds, then an "Unsupported content type" error is returned.
 */
fun <T> unmarshaller(vararg unmarshallers: HttpResponseHandler<UnstructuredData, T>): HttpResultHandler<*, UnstructuredData, T> =
    { result ->
        val response = result.response
        val initial = HttpResponse<T>(
            response.statusCode,
            response.statusMessage,
            response.headers
        )

        /*
         * Recurse through the unmarshallers trying to unmarshall the UnstructuredData until one unmarshaller
         * succeeds, or no unmarshaller succeeds so we have an "unsupported content type error"
         */
        unmarshallers
            .fold(Either.Right(initial) as Either<java.lang.Exception, HttpResponse<T>>) { acc, unmarshaller ->
                either {
                    val resp = acc.bind()
                    resp.body?.let { resp } ?: unmarshaller(response).bind()
                }
            }
            .flatMap(unsupportedContentType(response))
            .bimap(::identity) { resp ->
                HttpResult(
                    request = result.request,
                    response = resp
                )
            }
    }
