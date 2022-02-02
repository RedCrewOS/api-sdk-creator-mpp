package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either
import arrow.core.identity
import arrow.core.right
import au.com.redcrew.apisdkcreator.httpclient.data.aHttpRequest
import au.com.redcrew.apisdkcreator.httpclient.data.aHttpResponse
import au.com.redcrew.apisdkcreator.test.CoroutineExtension
import au.com.redcrew.apisdkcreator.test.throwException
import au.com.redcrew.apisdkcreator.test.throwSdkError
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
@ExtendWith(CoroutineExtension::class)
@DisplayName("JSON Marshalling")
class JsonMarshallingTest(val dispatcher: TestCoroutineDispatcher) {
    data class TestBody(
        val a: Int,
        val b: Map<String, String>,
        val c: Boolean
    )

    private val body = TestBody(1, mapOf("x" to "y"), true)
    private val json = UnstructuredData.String("{ \"a\": 1, \"b\": { \"x\": \"y\" }, \"c\": true }")

    @Nested
    @DisplayName("marshaller")
    inner class MarshallerTest {
        private val marshaller: Marshaller = { json.right() }
        private val request = aHttpRequest<TestBody>().withBody(body).build()

        @Test
        fun `should set content type`() = dispatcher.runBlockingTest {
            val contentType = "application/json+vnd"

            val result = marshall(contentType).fold(::throwSdkError, ::identity)

            assertThat(result.headers["content-type"], equalTo(contentType))
        }

        @Test
        fun `should default content type`() = dispatcher.runBlockingTest {
            val result = marshall().fold(::throwSdkError, ::identity)

            assertThat(result.headers["content-type"], equalTo(JSON_MIME_TYPE))
        }

        private suspend fun marshall(contentType: String): Either<SdkError, HttpRequest<UnstructuredData>> =
            jsonMarshaller(contentType)(marshaller)(request)

        private suspend fun marshall(): Either<SdkError, HttpRequest<UnstructuredData>> =
            jsonMarshaller()(marshaller)(request)
    }

    @Nested
    @DisplayName("unmarshaller")
    inner class UnmarshallerTest {
        private val unmarshaller: Unmarshaller<TestBody> = { body.right() }
        private val factory: GenericTypeUnmarshaller = GenericTypeUnmarshallerWrapper<TestBody> { unmarshaller }

        private val result = HttpResult(
            request = aHttpRequest<Any>().build(),
            response = aHttpResponse<UnstructuredData>()
                .withHeaders(mapOf("content-type" to "$JSON_MIME_TYPE ; charset=utf8"))
                .withBody(json)
                .build()
        )

        @Test
        fun `should set content type`() = dispatcher.runBlockingTest {
            val contentType = "text/plain"

            // since we want to unmarshall text/plain we should ignore the data and get back an error
            val result = unmarshall(contentType).fold(::identity, ::throwException)

            assertThat(result.type, equalTo(UNMARSHALLING_ERROR_TYPE))
        }

        @Test
        fun `should default content type`() = dispatcher.runBlockingTest {
            val result = unmarshall().fold(::throwSdkError, ::identity)

            assertThat(result.response.body, equalTo(body))
        }

        private suspend fun unmarshall(contentType: String): Either<SdkError, HttpResult<*, TestBody>> =
            jsonUnmarshaller(factory, contentType)(TestBody::class)(result)

        private suspend fun unmarshall(): Either<SdkError, HttpResult<*, TestBody>> =
            jsonUnmarshaller(factory)(TestBody::class)(result)
    }
}

class GenericTypeUnmarshallerWrapper<A: Any>(
    private val delegate: (KClass<A>) -> Unmarshaller<A>
) : GenericTypeUnmarshaller {
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> invoke(p1: KClass<T>): Unmarshaller<T> {
        return delegate(p1 as KClass<A>) as Unmarshaller<T>
    }
}
