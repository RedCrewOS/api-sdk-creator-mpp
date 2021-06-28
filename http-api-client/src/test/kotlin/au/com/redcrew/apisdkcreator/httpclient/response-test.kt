package au.com.redcrew.apisdkcreator.httpclient

import au.com.redcrew.apisdkcreator.httpclient.test.CoroutineExtension
import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import com.natpryce.hamkrest.assertion.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
@ExtendWith(CoroutineExtension::class)
@DisplayName("Http Response")
class ResponseTest {
    @Nested
    @DisplayName("copyWithBody")
    inner class CopyWithBodyTest {
        private val response = HttpResponse(
            statusCode = 200,
            statusMessage = "OK",
            headers = mapOf("content-type" to "text/plain"),
            body = "Who is Batman"
        )

        private val statusCode = 500
        private val statusMessage = "Internal Server Error"
        private val headers = mapOf("content-type" to "application/json")
        private val body = 12345678

        @Test
        fun `should change status code`() {
            val result = response.copyWithBody(statusCode = statusCode, body = body)

            assertThat(result, isResponse(response, statusCode = statusCode, body = body))
        }

        @Test
        fun `should change status message`() {
            val result = response.copyWithBody(statusMessage = statusMessage, body = body)

            assertThat(result, isResponse(response, statusMessage = statusMessage, body = body))
        }

        @Test
        fun `should change headers`() {
            val result = response.copyWithBody(headers = headers, body = body)

            assertThat(result, isResponse(response, headers = headers, body = body))
        }

        @Test
        fun `should change body only`() {
            val result = response.copyWithBody(body = body)

            assertThat(result, isResponse(response, body = body))
        }

        private fun isResponse(
            response: HttpResponse<*>,
            statusCode: Int = response.statusCode,
            statusMessage: String = response.statusMessage,
            headers: HttpHeaders = response.headers,
            body: Any? = response.body
        ) =
            object: Matcher.Primitive<HttpResponse<*>>() {
                override fun invoke(actual: HttpResponse<*>): MatchResult {
                    if (actual.statusCode != statusCode) {
                        return MatchResult.Mismatch("${actual.statusCode} != $statusCode")
                    }

                    if (actual.statusMessage != statusMessage) {
                        return MatchResult.Mismatch("${actual.statusMessage} != $statusMessage")
                    }

                    if (actual.headers != headers) {
                        return MatchResult.Mismatch("${actual.headers} != $headers")
                    }

                    if (actual.body != body) {
                        return MatchResult.Mismatch("${actual.body} != $body")
                    }

                    return MatchResult.Match
                }

                override val description: String
                    get() = "A copied HTTP response with updated body"
            }
    }
}
