package au.com.redcrew.apisdkcreator.httpclient

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.throws
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Base Http Client")
class BaseHttpClientTest {
    @Nested
    @DisplayName("path params")
    inner class PathParamsTest {
        @Test
        fun `should throw error if value for param not found`() {
            assertThat(
                { replacePathParams("customer/:id/account/:accountNumber", emptyMap()) },
                throws<IllegalArgumentException>()
            )
        }

        @Test
        fun `should replace slugs in path`() {
            val path = replacePathParams("customer/:id/account/:accountNumber", mapOf(
                "id" to "123",
                "accountNumber" to "456"
            ))

            assertThat(path, equalTo("customer/123/account/456"))
        }
    }

    @Nested
    @DisplayName("query params")
    inner class QueryParamsTest {
        @Test
        fun `should create query string`() {
            val queryParams = mapOf(
                "a" to "1",
                "x" to "foo"
            )

            val qs = createQueryString(queryParams)

            // we can't guarantee the order of the parts of the query string.
            assertThat(qs.contains("&"), equalTo(true))
            assertThat(qs.contains("a=1"), equalTo(true))
            assertThat(qs.contains("x=foo"), equalTo(true))
        }

        @Test
        fun `should urlencode query parameters`() {
            val queryParams = mapOf(
                "callback" to "http://localhost:5000"
            )

            val qs = createQueryString(queryParams)

            assertThat(qs, equalTo("?callback=http%3A%2F%2Flocalhost%3A5000"))
        }

        @Test
        fun `should return empty string when no params`() {
            val qs = createQueryString(emptyMap())

            assertThat(qs, equalTo(""))
        }
    }
}
