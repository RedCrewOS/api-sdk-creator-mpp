package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.*
import au.com.redcrew.apisdkcreator.httpclient.data.aHttpRequest
import au.com.redcrew.apisdkcreator.httpclient.data.aHttpResponse
import au.com.redcrew.apisdkcreator.test.CoroutineExtension
import au.com.redcrew.apisdkcreator.test.throwException
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.sameInstance
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(CoroutineExtension::class)
@DisplayName("Marshalling")
class MarshallingTest(val dispatcher: TestCoroutineDispatcher) {
    /*
     * We will be using JSON as the test data type for marshalling/unmarshalling but any data type could be used.
     */
    data class TestBody(
        val a: Int,
        val b: Map<String, String>,
        val c: Boolean
    )

    private val contentType = "application/json+vnd"
    private val body = TestBody(1, mapOf("x" to "y"), true)
    private val json = UnstructuredData.String("{ \"a\": 1, \"b\": { \"x\": \"y\" }, \"c\": true }")

    @Nested
    @DisplayName("marshallerFor")
    inner class MarshallerForTest {
        private val marshaller: Marshaller = { json.right() }
        private val request = aHttpRequest<TestBody>().withBody(body).build()

        @Test
        fun `should set content type`() = dispatcher.runBlockingTest {
            val result = marshall().fold(::throwException, ::identity)

            assertThat(result.headers["content-type"], equalTo(contentType))
        }

        @Test
        fun `should set no content type when no request body`() = dispatcher.runBlockingTest {
            val request = aHttpRequest<TestBody>().build()

            val result = marshall(request).fold(::throwException, ::identity)

            assertThat(result.headers["content-type"], equalTo(null))
        }

        @Test
        fun `should not marshall body when no request body`() = dispatcher.runBlockingTest {
            val request = aHttpRequest<TestBody>().build()

            val result = marshall(request).fold(::throwException, ::identity)

            assertThat(result.body, equalTo(null))
        }

        @Test
        fun `should marshall request body`() = dispatcher.runBlockingTest {
            val result = marshall().fold(::throwException, ::identity)

            assertThat(result.body, equalTo(json))
        }

        @Test
        fun `should return error when error marshalling request body`() = dispatcher.runBlockingTest {
            val error = Exception("fake marshalling error")
            val marshaller: Marshaller = { error.left() }

            val result = marshall(request, marshaller).fold(::identity, ::throwException)

            assertThat(result, sameInstance(error))
        }

        suspend fun marshall(
            request: HttpRequest<TestBody> = this.request,
            marshaller: Marshaller = this.marshaller,
            contentType: String = this@MarshallingTest.contentType,
        ): Either<Exception, HttpRequest<UnstructuredData>> =
            marshallerFor(contentType)(marshaller)(request)
    }

    @Nested
    @DisplayName("unmarshallerFor")
    inner class UnmarshallerForTest {
        private val unmarshaller: Unmarshaller<TestBody> = { body.right() }
        private val response = aHttpResponse<UnstructuredData>()
            .withHeaders(mapOf("content-type" to "$contentType ; charset=utf8"))
            .withBody(json)
            .build()

        @Test
        fun `should unmarshall response body`() = dispatcher.runBlockingTest {
            val result = unmarshall()
                .fold(::throwException, ::identity)
                .fold(::throwUnstructuredDataException, ::identity)

            assertThat(result.body, equalTo(body))
        }

        @Test
        fun `should not unmarshall response body when content type does not match`() = dispatcher.runBlockingTest {
            val response = aHttpResponse<UnstructuredData>()
                .withHeaders(mapOf("content-type" to "text/plain"))
                .withBody(json)
                .build()

            val result = unmarshall(response)
                .fold(::throwException, ::identity)
                .fold(::identity, ::throwUnmarshalledDataException)

            assertThat(result, equalTo(response))
        }

        @Test
        fun `should not unmarshall response body when no response body`() = dispatcher.runBlockingTest {
            val response = this@UnmarshallerForTest.response.copy(body = null)

            // we expect an Either.Right as the result since there is no body.
            val result = unmarshall(response)
                .fold(::throwException, ::identity)
                .fold(::throwUnstructuredDataException, ::identity)

            assertThat(result.body, equalTo(null))
        }

        @Test
        fun `should return error when error unmarshalling response body`() = dispatcher.runBlockingTest {
            val error = Exception("fake marshalling error")
            val unmarshaller: Unmarshaller<TestBody> = { error.left() }

            val result = unmarshall(response, unmarshaller).fold(::identity, ::throwException)

            assertThat(result, sameInstance(error))
        }

        suspend fun unmarshall(
            result: HttpResponse<UnstructuredData> = this.response,
            unmarshaller: Unmarshaller<TestBody> = this.unmarshaller,
            contentType: String = this@MarshallingTest.contentType
        ): Either<Exception, Either<HttpResponse<UnstructuredData>, HttpResponse<TestBody>>> =
            unmarshallerFor(contentType)(unmarshaller)(result)

        private fun <T> throwUnstructuredDataException(
            @Suppress("UNUSED_PARAMETER") response: HttpResponse<UnstructuredData>
        ): HttpResponse<T> {
            throw IllegalStateException("Expected HttpResponse to have had body unmarshalled")
        }

        private fun <T> throwUnmarshalledDataException(
            @Suppress("UNUSED_PARAMETER") response: HttpResponse<T>
        ): HttpResponse<T> {
            throw IllegalStateException("HttpResponse erroneously had body unmarshalled")
        }
    }

    @Nested
    @DisplayName("unmarshaller")
    inner class UnmarshallerTest {
        private val makeUnmarshaller: (String, String) -> ResponseUnmarshaller<String> =
            { contentType: String, data: String?  ->
                { response ->
                    response.body?.let {
                        when {
                            response.headers.containsValue(contentType) -> {
                                Either.Right(response.copyWithBody(body = data).right())
                            }
                            else -> {
                                Either.Right(response.left())
                            }
                        }
                    } ?: run {
                        Either.Right(response.copyWithBody<String>(body = null).right())
                    }
                }
            }

        private val plainTextUnmarshaller = makeUnmarshaller("text/plain", "This is some text")
        private val anotherPlainTextUnmarshaller = makeUnmarshaller("text/plain", "This is other text")
        private val jsonUnmarshaller = makeUnmarshaller("application/json", "{ \"a\": false }")

        private val unmarshallers = unmarshaller(jsonUnmarshaller, plainTextUnmarshaller, anotherPlainTextUnmarshaller)

        private val responseHeaders = mapOf("content-type" to "text/plain")

        @Test
        fun `should return first unmarshalled body`() = dispatcher.runBlockingTest {
            val result = HttpResult(
                request = aHttpRequest<String>().build(),
                response = aHttpResponse<UnstructuredData>()
                    .withHeaders(responseHeaders)
                    .withBody(UnstructuredData.String("Hello World"))
                    .build()
            )

            val body = unmarshallers(result).fold(::throwException, ::identity).response.body

            assertThat(body, equalTo("This is some text"))
        }

        @Test
        fun `should return unsupported content when response body not unmarshalled`() = dispatcher.runBlockingTest {
            val result = HttpResult(
                request = aHttpRequest<String>().build(),
                response = aHttpResponse<UnstructuredData>()
                    .withHeaders(mapOf("content-type" to "image/png"))
                    .withBody(UnstructuredData.String("Hello World"))
                    .build()
            )

            val error = unmarshallers(result).fold(::identity, ::throwException)

            assertThat(error is IllegalStateException, equalTo(true))
            assertThat((error as IllegalStateException).message, equalTo("Unrecognised content type 'image/png'"))
        }
    }
}
