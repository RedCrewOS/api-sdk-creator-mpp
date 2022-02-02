package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.*
import au.com.redcrew.apisdkcreator.httpclient.data.aHttpRequest
import au.com.redcrew.apisdkcreator.httpclient.data.aHttpResponse
import au.com.redcrew.apisdkcreator.test.throwException
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("accessors")
class AccessorsTest {
    @Test
    fun `should extract response body from result`() {
        val body = mapOf<Any, Any>("a" to 1, "x" to "foo" )

        val result = extractHttpBody(givenHttpResult(body))

        assertThat(result, equalTo(body))
    }

    @Nested
    @DisplayName("header parsing")
    inner class HeaderParsingTest {
        @Nested
        @DisplayName("int headers")
        inner class IntHeadersParsingTest {
            @Test
            fun `should return nothing if header not present`() {
                val result = parseIntHeader("x-header", emptyMap()).merge()

                assertThat("Option.None not returned", none<Int>() == result, equalTo(true))
            }

            @Test
            fun `should return error if header value not a number`() {
                val result = parseIntHeader("x-header", mapOf("x-header" to "abc")).fold(::identity, ::throwException)

                assertThat(result.type, equalTo(INVALID_NUMBER_ERROR_TYPE))
            }

            @Test
            fun `should parse int header`() {
                val result = parseIntHeader("x-header", mapOf("x-header" to "123")).merge()

                assertThat("Option not returned", result is Option<*>, equalTo(true))
                assertThat((result as Option<*>).getOrElse { -1 }, equalTo(123))
            }
        }
    }

    private fun givenHttpResult(body: Any): HttpResult<Any, Any> {
        return HttpResult(
            aHttpRequest<Any>().build(),
            aHttpResponse<Any>().withBody(body).build()
        )
    }
}
