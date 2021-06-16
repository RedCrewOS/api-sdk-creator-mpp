package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either
import arrow.core.identity
import au.com.redcrew.apisdkcreator.httpclient.test.CoroutineExtension
import au.com.redcrew.apisdkcreator.httpclient.test.throwException
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(CoroutineExtension::class)
@DisplayName("Headers")
class HeadersTest(val dispatcher: TestCoroutineDispatcher) {
    @Nested
    @DisplayName("header factory")
    inner class HeaderFactoryTest {
        private val factoryOne: RequestHeaderFactory = { Either.Right(mapOf("x-application-name" to "project")) }
        private val factoryTwo: RequestHeaderFactory = { Either.Right(mapOf("x-api-key" to "abc123")) }

        @Test
        fun `should create request headers from List`() = dispatcher.runBlockingTest{
            val factory: RequestHeadersFactory = createHeaders(listOf(factoryOne, factoryTwo))
            val headers: HttpHeaders = factory().fold(::throwException, ::identity)

            assertThat(headers["x-application-name"], equalTo("project"))
            assertThat(headers["x-api-key"], equalTo("abc123"))
        }
    }

    @Nested
    @DisplayName("bearer token factory")
    inner class BearerTokenFactoryTest {
        @Test
        fun `should create authorization header`() = dispatcher.runBlockingTest {
            val token = "abc123"

            val result = bearerToken { Either.Right(token) }(emptyMap()).fold(::throwException, ::identity)

            assertThat(result["authorization"], equalTo("Bearer $token"))
        }
    }

    @Nested
    @DisplayName("constant headers factory")
    inner class ConstantHeadersFactoryTest {
        @Test
        fun `should return provided headers`() = dispatcher.runBlockingTest {
            val headers = mapOf(
                "x-application-name" to "project",
                "x-api-key" to "abc123"
            )

            val result = constantHeaders(headers)(emptyMap()).fold(::throwException, ::identity)

            assertThat(result["x-application-name"], equalTo("project"))
            assertThat(result["x-api-key"], equalTo("abc123"))
        }
    }
}
