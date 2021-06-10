package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either
import arrow.core.identity
import au.com.redcrew.apisdkcreator.httpclient.data.aHttpRequest
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
import java.net.URL

@ExperimentalCoroutinesApi
@ExtendWith(CoroutineExtension::class)
@DisplayName("Http Request")
class RequestTest(val dispatcher: TestCoroutineDispatcher) {
    @Nested
    @DisplayName("adding headers")
    inner class AddingHeadersTest {
        private val request = aHttpRequest<Any>().addHeader("authorization", "foo").build()

        @Test
        fun `should add to existing headers`() = dispatcher.runBlockingTest {
            val headers = mapOf("x-app-header" to "abc123")

            val result = addHeaders { Either.Right(headers) }(request).fold(::throwException, ::identity)

            assertThat(result.headers["authorization"], equalTo(request.headers["authorization"]))
            assertThat(result.headers["x-app-header"], equalTo(headers["x-app-header"]))
            assertThat(result.url, equalTo(request.url))
            assertThat(result.method, equalTo(request.method))
        }

        @Test
        fun `should return error if factory fails to create headers`() = dispatcher.runBlockingTest {
            val error = Exception("Can't create headers")

            val result = addHeaders { Either.Left(error) }(request).fold(::identity, ::throwException)

            assertThat(result, equalTo(error))
        }
    }

    @Nested
    @DisplayName("resolving url")
    inner class ResolvingUrlTest {
        private val base = "http://localhost:3000"

        @Test
        fun `should join path to base`() = dispatcher.runBlockingTest {
            val path = "/v1/foo/bar"
            val request = aHttpRequest<Any>().withUrl(HttpRequestUrl.String(path)).build()

            val result = resolveUrl(base)(request).fold(::throwException, ::identity)

            assertThat(result.url, equalTo(HttpRequestUrl.String("${base}${path}")))
        }

        @Test
        fun `should return error if trying to resolve an absolute url`() = dispatcher.runBlockingTest {
            @Suppress("BlockingMethodInNonBlockingContext")
            val url = URL("http://localhost")
            val request = aHttpRequest<Any>().withUrl(HttpRequestUrl.URL(url)).build()

            val result = resolveUrl(base)(request).fold(::identity, ::throwException)

            assertThat(result is IllegalArgumentException, equalTo(true))
        }
    }
}
