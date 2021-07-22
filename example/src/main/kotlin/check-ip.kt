@file:JvmName("CheckIp")

import arrow.core.Either
import arrow.core.leftIfNull
import au.com.redcrew.apisdkcreator.httpclient.*
import au.com.redcrew.apisdkcreator.httpclient.arrow.pipeK
import au.com.redcrew.apisdkcreator.httpclient.gson.gsonMarshaller
import au.com.redcrew.apisdkcreator.httpclient.gson.gsonUnmarshaller
import au.com.redcrew.apisdkcreator.httpclient.okhttp.okHttpClient
import com.google.gson.Gson
import kotlinx.coroutines.runBlocking

/*
 * Example using api-sdk-creator to check your IP address.
 *
 * This sends a HTTP GET request for some JSON data and prints the results.
 */

/*
 * A data class modelling the data we want to receive.
 */
data class IpData(
    val ip: String,
    val country: String
)

/*
 * Create a Marshaller and Unmarshaller that uses an existing data conversion library
 * to do the work for our API calls.
 *
 * In a real SDK this would be done at application start time with the resulting functions
 * being passed to SDK specific operations.
 *
 * Partial application of functions is how you do Dependency Injection with functions.
 */
val gson = Gson()
val marshaller = gsonMarshaller(gson)
val unmarshaller = gsonUnmarshaller(gson)

/*
 * A factory function to define a pipeline of work for sending API requests to a server.
 *
 * This factory would use the result of any SDK configuration or any other data to create the initial pipeline.
 *
 * A partial API client is returned because the function/pipeline needs to be composed with another function
 * that knows how to handle the result data/what type we're unmarshalling the JSON response data into. This is a
 * consequence of using a statically typed language like Kotlin where we have to defer some parts of the pipeline to
 * when specific types are known, while still trying to use a single pipeline definition as much as possible.
 */
suspend fun apiClient(client: HttpClient): suspend (HttpRequest<*>) -> Either<Exception, HttpResult<*, UnstructuredData>> {
    /*
     * Create the function that will add default headers to every HTTP request.
     *
     * If this results in an Either.Left(Exception), when the HTTP request is piped through the API definition the
     * Exception would be returned.
     *
     * Alternatively in an SDK, this type of factory would be invoked near SDK initialisation, which would give the
     * opportunity to fail fast and not even continue the rest of the SDK instantiation.
     */
    val defaultHeaders = addHeaders(createHeaders(constantHeaders(mapOf("x-client-name" to "api-sdk-creator-kotlin"))))

    /*
     * Define a pipeline of work that
     * 1. Adds the default headers to every HTTP request
     * 2. Converts any request body to JSON using the default JSON content type and JSON library wrapper we defined above.
     * 3. Sends the request to a server by using a HttpClient function which wraps an existing HTTP client library.
     *
     * The library specific pieces have been abstracted away behind function types allowing for injection of any
     * compatible function and thus any compatible library.
     *
     * When the pipeline is run with a request, if any of these functions return a Either.Left(Exception), the overall
     * result of the pipeline will be an Exception which SDK specific operations will need to pass back to client
     * applications, or handle internally.
     *
     * The use of the `pipeK` operator is used to pipe the result of each expression to the next expression in the
     * sequence. We take advantage of Kleisli arrows (hence the K) to compose (left to right) monad returning functions
     * together.
     *
     * For a discussion of Kleisli see the `pipeK` documentation.
     */
    return defaultHeaders pipeK jsonMarshaller()(marshaller) pipeK client
}

/*
 * This mimics an SDK specific operation. Here we want to check the IP of the computer we're running on.
 *
 * `checkIp` takes a partial API client and a JSON Unmarshaller to compose the final API pipeline
 */
suspend fun checkIp(
    client: suspend (HttpRequest<*>) -> Either<Exception, HttpResult<*, UnstructuredData>>,
    jsonUnmarshaller: GenericJsonResultHandler
): Either<Exception, IpData> {
    /*
     * The operation specific components of the request.
     */
    val request = HttpRequest<Unit>(
        HttpRequestMethod.GET,
        HttpRequestUrl.String("http://ifconfig.co/json")
    )

    /*
     * Because the response type (`IpData`) is now known we can configure the JSON Unmarshalling to
     * convert JSON response data into an instance of the type.
     *
     * The final pipeline to send a request to the server is now complete.
     *
     * SDK developers should use partial application or closures to do this once and have the
     * pipeline available to all SDK operation invocations.
     */
    val pipeline = client pipeK jsonUnmarshaller(IpData::class)

    /*
     * So let's sent the request to the server
     */
     val result = pipeline(request)

    /*
     * Finally extract the response body from the HttpResult and return it to the SDK caller.
     *
     * If the result is an Either.Left, then the map() won't happen. This is the beauty of the Either monad in
     * action as we don't even have to think about error handling thanks to the polymorphic behaviour of monads.
     *
     * If the extraction process fails because there is no JSON data then the contract has been violated.
     * We therefore convert the result to an Either.Left with an exception so that callers can handle the
     * violation appropriately. Because `leftIfNull` is lazy, it wont create the exception until it's needed.
     */
    return result.map(::extractHttpBody).leftIfNull { IllegalStateException("Didn't get any JSON data") }
}

fun main() {
    /*
     * Because the functions in `api-sdk-creator` are suspending functions, the SDK operation is suspending as well.
     * To use an SDK operation, we need to launch a Coroutine.
     *
     * We want to wait here until we get the HTTP response from the server, we'll just block the execution
     * of the program.
     *
     * In a real application (eg: an Android app) the Coroutine Scope would need to be assigned to a Dispatcher to be
     * executed on a non UI thread (eg: by using `withContext`)
     *
     * See:
     *  - https://kotlinlang.org/docs/coroutines-basics.html
     *  - https://developer.android.com/kotlin/coroutines
     */
    val result = runBlocking {
        checkIp(apiClient(okHttpClient()), jsonUnmarshaller(unmarshaller))
    }

    /*
     * Because the outcome of the API call could either be a success, or an error the result of the API call
     * is an instance of Arrow's Either Monad.
     *
     * To get at the wrapped value we have to "unwrap the box" or "fold out the value".
     *
     * In this case we throw the error if we have one, or print the IpData.
     *
     * In a real application we could transform the error/data and update a UI model (eg: LiveData) to show the
     * result to the user in an appropriate way.
     */
    result.fold(
        { e -> throw e },
        { data -> print(data) }
    )
}
