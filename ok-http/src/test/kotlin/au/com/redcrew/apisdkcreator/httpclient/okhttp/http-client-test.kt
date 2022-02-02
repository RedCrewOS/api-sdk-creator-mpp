package au.com.redcrew.apisdkcreator.httpclient.okhttp

import arrow.core.identity
import au.com.redcrew.apisdkcreator.httpclient.*
import au.com.redcrew.apisdkcreator.test.FunctionSource
import au.com.redcrew.apisdkcreator.test.throwException
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.containsSubstring
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.greaterThanOrEqualTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import okhttp3.Headers.Companion.toHeaders
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.jupiter.api.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

@Suppress("BlockingMethodInNonBlockingContext")
@ExperimentalCoroutinesApi
@DisplayName("OkHttp HttpClient")
class OkHttpHttpClientTest {
    companion object {
        fun methods(): Stream<Arguments> =
            HttpRequestMethod.values().map { Arguments.of(it) }.stream()
    }

    private lateinit var mockWebServer: MockWebServer
    private lateinit var url: String
    private lateinit var request: HttpRequest<UnstructuredData>

    @BeforeEach
    fun beforeEach() {
        /*
         * When shutting down the server, the "is started" flag isn't cleared, so the server can't be reused.
         */
        mockWebServer = MockWebServer()
        mockWebServer.start()

        url = "http://127.0.0.1:${mockWebServer.port}"

        request = HttpRequest(
            method = HttpRequestMethod.GET,
            url = HttpRequestUrl.String(url),
            headers = emptyMap(),
            pathParams = emptyMap(),
            queryParams = emptyMap(),
            body = null
        )
    }

    @AfterEach
    fun afterEach() {
        mockWebServer.shutdown()
    }

    @Nested
    @DisplayName("request")
    inner class RequestTest {
        @Nested
        @DisplayName("method")
        inner class MethodTest {
            @ParameterizedTest(name = "should use {0} method")
            @FunctionSource("methods")
            fun `should set method`(method: HttpRequestMethod) = runBlocking {
                val request = request.copy(
                    headers = request.headers + mapOf("content-type" to "text/plain"),
                    body = UnstructuredData.String("Hello World")
                )

                mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))

                okHttpClient()(request.copy(method = method))

                val result = recordedRequest()

                assertThat(result != null, equalTo(true))
                assertThat(result?.method, equalTo(method.toString()))
            }
        }

        @Nested
        @DisplayName("url")
        inner class UrlTest {
            @BeforeEach
            fun beforeEach() {
                mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))
            }

            @Test
            fun `should use URL from URL`() = runBlocking {
                val request = request.copy(url = HttpRequestUrl.URL(URL(this@OkHttpHttpClientTest.url)))

                okHttpClient()(request)

                val result = recordedRequest()

                assertThat(result != null, equalTo(true))
            }

            @Test
            fun `should use URL from string`() = runBlocking {
                okHttpClient()(request)

                val result = recordedRequest()

                assertThat(result != null, equalTo(true))
            }
        }

        @Nested
        @DisplayName("headers")
        inner class HeadersTest {
            @BeforeEach
            fun beforeEach() {
                mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))
            }

            @Test
            fun `should copy headers`() = runBlocking {
                val name = "x-header-name"
                val value = "value"
                val request = request.copy(headers = request.headers + mapOf(name to value))

                okHttpClient()(request)

                val result = recordedRequest()

                assertThat(result?.headers!!.size, greaterThanOrEqualTo(2))
                assertThat(result.headers[name], equalTo(value))
            }

            @Test
            fun `should filter out content type header when no body`() = runBlocking {
                val request = request.copy(body = null)

                okHttpClient()(request)

                val result = recordedRequest()

                assertThat(result?.headers?.get("content-type"), equalTo(null))
            }
        }

        @Nested
        @DisplayName("path params")
        inner class PathParamsTest {
            private lateinit var url: String

            @BeforeEach
            fun beforeEach() {
                mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))

                url = this@OkHttpHttpClientTest.url + "/:id"
            }

            @Test
            fun `should replace slugs with values`() = runBlocking {
                val request = request.copy(url = HttpRequestUrl.String(url), pathParams = mapOf("id" to "123"))

                okHttpClient()(request)

                val result = recordedRequest()

                assertThat(result?.path!!, containsSubstring("/123"))
            }

            @Test
            fun `should return error if no value for slug`() = runBlocking {
                val request = request.copy(url = HttpRequestUrl.String(url), pathParams = null)

                val result = okHttpClient()(request).fold(::identity, ::throwException)

                assertThat(result.type, equalTo(ILLEGAL_ARGUMENT_ERROR_TYPE))
            }
        }

        @Nested
        @DisplayName("query params")
        inner class QueryParamsTest {
            @BeforeEach
            fun beforeEach() {
                mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))

                request = request.copy(queryParams = mapOf("type" to "xml"))
            }

            @Test
            fun `should append query parameters`() = runBlocking {
                okHttpClient()(request)

                val result = recordedRequest()

                assertThat(result?.requestUrl?.query, equalTo("type=xml"))
            }
        }

        @Nested
        @DisplayName("body")
        inner class BodyTest {
            @BeforeEach
            fun beforeEach() {
                mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))

                request = request.copy(
                    method = HttpRequestMethod.POST,
                    headers = request.headers + mapOf("content-type" to "text/plain"),
                    body = UnstructuredData.String("Hello World")
                )
            }

            @Test
            fun `should write string into request`()  = runBlocking {
                val body = "This is some data"
                val request = request.copy(body = UnstructuredData.String(body))

                okHttpClient()(request)

                val result = recordedRequest()
                assertThat(result?.body?.readUtf8(), equalTo(body))
            }

            @Test
            fun `should return error if body present but no content type`() = runBlocking {
                val request = request.copy(headers = emptyMap())

                val result = okHttpClient()(request).fold(::identity, ::throwException)

                assertThat(result.message, equalTo("Missing content-type"))
            }

            @Test
            fun `should ignore body when body should not be sent`() = runBlocking {
                val request = request.copy(method = HttpRequestMethod.GET)

                okHttpClient()(request)

                val result = recordedRequest()
                assertThat(result?.body?.size, equalTo(0))
            }
        }
    }

    @Nested
    @DisplayName("response")
    inner class ResponseTest {
        @Nested
        @DisplayName("errors")
        inner class ErrorsTest {
            @Test
            fun `should return error when non HTTP error occurs`() = runBlocking {
                val request = request.copy(url = HttpRequestUrl.String("http://127.0.0.1:8080"))

                okHttpClient()(request).fold(::identity, ::throwException)

                Unit
            }
        }

        @Nested
        @DisplayName("status")
        inner class StatusTest {
            @Test
            fun `should return success status code`() = runBlocking{
                mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))

                val result = okHttpClient()(request).fold(::throwException, ::identity) as HttpResult<*, *>

                assertThat(result.response.statusCode, equalTo(200))
            }

            /*
             * HttpClient libraries often treat errors differently from success eg: throw rather than return.
             */
            @Test
            fun `should return error status code`() = runBlocking {
                mockWebServer.enqueue(MockResponse().setResponseCode(500))

                val result = okHttpClient()(request).fold(::throwException, ::identity) as HttpResult<*, *>

                assertThat(result.response.statusCode, equalTo(500))
            }

            @Test
            fun `should return status message`() = runBlocking {
                mockWebServer.enqueue(MockResponse().setResponseCode(404))
                val result = okHttpClient()(request).fold(::throwException, ::identity) as HttpResult<*, *>

                assertThat(result.response.statusMessage, equalTo("Client Error"))
            }
        }

        @Nested
        @DisplayName("headers")
        inner class HeadersTest {
            @Test
            fun `should return headers`() = runBlocking {
                val headers = mapOf(
                    "x-my-header" to "value",
                    "x-my-other-header" to "other value",

                    // MockWebserver puts this in automatically
                    "content-length" to "0"
                )

                mockWebServer.enqueue(MockResponse()
                    .setResponseCode(200)
                    .setHeaders(headers.toHeaders())
                    .setBody("")
                )

                val result = okHttpClient()(request).fold(::throwException, ::identity) as HttpResult<*, *>

                assertThat(result.response.headers, equalTo(headers))
            }

            @Test
            fun `should lowercase all headers`() = runBlocking {
                val headers = mapOf("x-MY-Header" to "value")

                mockWebServer.enqueue(MockResponse()
                    .setResponseCode(200)
                    .setHeaders(headers.toHeaders())
                    .setBody("")
                )

                val result = okHttpClient()(request).fold(::throwException, ::identity) as HttpResult<*, *>

                assertThat(result.response.headers["x-my-header"], equalTo("value"))
            }
        }

        @Nested
        @DisplayName("body")
        inner class BodyTest {
            @Test
            fun `should return string`() = runBlocking {
                val body = "This is some data"

                mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(body))

                val result = okHttpClient()(request).fold(::throwException, ::identity) as HttpResult<*, *>

                assertThat(result.response.body, equalTo(UnstructuredData.String(body)))
            }

            @Test
            fun `should return null when no content returned from server`() = runBlocking {
                mockWebServer.enqueue(MockResponse().setResponseCode(200))

                val result = okHttpClient()(request).fold(::throwException, ::identity) as HttpResult<*, *>

                assertThat(result.response.body, equalTo(null))
            }
        }
    }

    fun recordedRequest(): RecordedRequest? {
        return mockWebServer.takeRequest(2000L, TimeUnit.MILLISECONDS)
    }
}
