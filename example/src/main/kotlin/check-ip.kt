@file:JvmName("CheckIp")

import arrow.core.Either
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
 * into do work for our API calls.
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
 * consequence of using a statically typed language like Kotlin where we have to defer some parts of the pipe to
 * when specific types are known, while still trying to use a single pipeline definition as much as possible.
 */
suspend fun apiClient(client: HttpClient): suspend (HttpRequest<*>) -> Either<Exception, HttpResult<*, UnstructuredData>> {
    /*
     * Create the function that will add default headers to every HTTP request.
     *
     * If this results in an Either.Left(Exception), when the HTTP request is piped through the API definition the
     * Exception would be returned.
     *
     * Alternative in an SDK, this type of factory would be invoked near SDK initialisation, which would give the
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
     */
    return defaultHeaders pipeK jsonMarshaller()(marshaller) pipeK client
}

/*
 * This mimics an SDK specific operation. Here we want to check the IP of the computer we're running on.
 *
 * `checkIp` takes a partial API client and an JSON Unmarshaller to compose the final API pipeline
 */
suspend fun checkIp(
    client: suspend (HttpRequest<*>) -> Either<Exception, HttpResult<*, UnstructuredData>>,
    jsonUnmarshaller: GenericJsonResultHandler
): Either<Exception, IpData?> {
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
     * The final pipeline to send a request to the server is now complete
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
     */
    return result.map(::extractHttpBody)
}

fun main() {
    /*
     * Because the functions in `api-sdk-creator` are suspending functions, to use an SDK operation, they need to be
     * used in a Coroutine Scope.
     *
     * Because we want to wait here until we get the HTTP response from the server, we'll just block the execution
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
     */
    result.fold(
        { e -> throw e },
        { data -> print(data) }
    )
}
