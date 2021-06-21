package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either

/**
 * A HttpApiClient knows how to interact with an API endpoint.
 *
 * It knows how to construct a request to be suitable for interacting with the endpoint.
 *
 * It knows how to convert API data into something consumable by an application.
 *
 * It knows how to handle HTTP errors (even if that means returning them).
 *
 * @template A
 * @template B
 * @param {HttpRequest} request
 * @returns Either the {@link HttpResponse} or the error.
 */
typealias HttpApiClient<A, B> = suspend (request: HttpRequest<A>) -> Either<Exception, HttpResponse<B>>

/**
 * A HttpClient deals strictly with {@link UnstructuredData} types. To use a HttpClient with
 * structured data (JSON, XML, etc) consumers of the HttpClient will need to be able to marshal
 * and unmarshal structured data to/from unstructured data types.
 *
 * Making a request will return the response regardless of what the response represents (ie: success or failure).
 *
 * @param {HttpRequest} request
 * @returns Either the {@link HttpResult}, or the error
 *
 * Errors should only be used to indicate unrecoverable problems like a network being unavailable.
 */
typealias HttpClient = suspend (request: HttpRequest<UnstructuredData>) -> Either<Exception, HttpResult<UnstructuredData, UnstructuredData>>

/**
 * The result of sending an {@link HttpRequest} to an endpoint.
 *
 * Includes the original request sent to allow handlers to recover after a failure by modifying
 * the request and trying again.
 *
 * TODO: Consider replacing with a State monad.
 */
data class HttpResult<A, B>(
    val request: HttpRequest<A>,
    val response: HttpResponse<B>
)

/**
 * Manipulates the HttpRequest to conform to specific requirements of the endpoint
 *
 * For example, adding an access token
 *
 * Because a policy may change the body, we need two types.
 *
 * @template A
 * @template B
 * @param {HttpRequest} request
 * @returns An updated {@link HttpRequest}
 */
typealias HttpRequestPolicy<A, B> = suspend (request: HttpRequest<A>) -> Either<Exception, HttpRequest<B>>

/**
 * Examines and handles the result of calling an endpoint.
 *
 * Because a handler may change the body, we need two types.
 *
 * @template A
 * @template B
 * @template C
 * @param {HttpResult} result
 * @returns The updated {@link HttpResult}
 */
typealias HttpResultHandler<A, B, C> = suspend (result: HttpResult<A, B>) -> Either<Exception, HttpResult<A, C>>

/**
 * Handles a {@link HttpResponse}
 *
 * @template A
 * @param {HttpResponse} response
 * @returns {Async} The updated {@link HttpResponse}
 */
typealias HttpResponseHandler<A> = suspend (response: HttpResponse<A>) -> Either<Exception, HttpResponse<A>>
