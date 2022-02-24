package au.com.redcrew.apisdkcreator.httpclient

import au.com.redcrew.apisdkcreator.httpclient.data.aHttpRequest
import au.com.redcrew.apisdkcreator.httpclient.data.aHttpResponse
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeNone
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.assertions.arrow.core.shouldBeSome
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

class AccessorsTest : DescribeSpec({
    describe("accessors") {
        fun givenHttpResult(body: Any): HttpResult<Any, Any> =
            HttpResult(
                aHttpRequest<Any>().build(),
                aHttpResponse<Any>().withBody(body).build()
            )

        it("should extract response body from result") {
            val body = mapOf<Any, Any>("a" to 1, "x" to "foo")

            val result = extractHttpBody(givenHttpResult(body))

            result.shouldBe(body)
        }

        describe("header parsing") {
            describe("int headers") {
                it("should return nothing if header not present") {
                    val result = parseIntHeader("x-header", emptyMap())

                    result.shouldBeRight().shouldBeNone()
                }

                it("should return error if header value not a number") {
                    val result = parseIntHeader("x-header", mapOf("x-header" to "abc"))
                    val error = result.shouldBeLeft()

                    error.type.shouldBe(INVALID_NUMBER_ERROR_TYPE)
                }

                it("should parse int header") {
                    val result = parseIntHeader("x-header", mapOf("x-header" to "123"))

                    result.shouldBeRight().shouldBeSome().shouldBe(123)
                }
            }
        }
    }
})
