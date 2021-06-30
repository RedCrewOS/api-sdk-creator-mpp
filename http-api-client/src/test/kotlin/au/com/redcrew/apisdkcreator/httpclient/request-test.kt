package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either
import arrow.core.identity
import au.com.redcrew.apisdkcreator.httpclient.data.aHttpRequest
import au.com.redcrew.apisdkcreator.test.CoroutineExtension
import au.com.redcrew.apisdkcreator.test.throwException
import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
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

    @Nested
    @DisplayName("copyWithBody")
    inner class CopyWithBodyTest {
        private val request = HttpRequest(
            method = HttpRequestMethod.GET,
            url = HttpRequestUrl.String("http://localhost/superhero/:id"),
            headers = mapOf("content-type" to "text/plain"),
            pathParams = mapOf("id" to "batman"),
            queryParams = mapOf("whois" to "true"),
            body = "Who is Batman"
        )

        private val method = HttpRequestMethod.POST
        private val url = HttpRequestUrl.String("http://localhost/add/:a")
        private val headers = mapOf("content-type" to "application/json")
        private val pathParams = mapOf("a" to "1")
        private val queryParams = emptyMap<String, String>()
        private val body = 43

        @Test
        fun `should change method`() {
            val result = request.copyWithBody(method = method, body = body)

            assertThat(result, isRequest(request, method = method, body = body))
        }

        @Test
        fun `should change url`() {
            val result = request.copyWithBody(url = url, body = body)

            assertThat(result, isRequest(request, url = url, body = body))
        }

        @Test
        fun `should change headers`() {
            val result = request.copyWithBody(headers = headers, body = body)

            assertThat(result, isRequest(request, headers = headers, body = body))
        }

        @Test
        fun `should change path params`() {
            val result = request.copyWithBody(pathParams = pathParams, body = body)

            assertThat(result, isRequest(request, pathParams = pathParams, body = body))
        }

        @Test
        fun `should change query parameters`() {
            val result = request.copyWithBody(queryParams = queryParams, body = body)

            assertThat(result, isRequest(request, queryParams = queryParams, body = body))
        }

        @Test
        fun `should change body only`() {
            val result = request.copyWithBody(body = body)

            assertThat(result, isRequest(request, body = body))
        }

        private fun isRequest(
            request: HttpRequest<*>,
            method: HttpRequestMethod = request.method,
            url: HttpRequestUrl = request.url,
            headers: HttpHeaders = request.headers,
            pathParams: Map<String, String>? = request.pathParams,
            queryParams: Map<String, String>? = request.queryParams,
            body: Any? = request.body
        ) =
            object: Matcher.Primitive<HttpRequest<*>>() {
                override fun invoke(actual: HttpRequest<*>): MatchResult {
                    if (actual.method != method) {
                        return MatchResult.Mismatch("${actual.method} != $method")
                    }

                    if (actual.url != url) {
                        return MatchResult.Mismatch("${actual.url} != $url")
                    }

                    if (actual.headers != headers) {
                        return MatchResult.Mismatch("${actual.headers} != $headers")
                    }

                    if (actual.pathParams != pathParams) {
                        return MatchResult.Mismatch("${actual.pathParams} != $pathParams")
                    }

                    if (actual.queryParams != queryParams) {
                        return MatchResult.Mismatch("${actual.queryParams} != $queryParams")
                    }

                    if (actual.body != body) {
                        return MatchResult.Mismatch("${actual.body} != $body")
                    }

                    return MatchResult.Match
                }

                override val description: String
                    get() = "A copied HTTP request with updated body"
            }
    }
}
