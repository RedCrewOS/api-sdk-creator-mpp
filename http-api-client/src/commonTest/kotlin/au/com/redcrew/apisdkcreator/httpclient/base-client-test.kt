package au.com.redcrew.apisdkcreator.httpclient

import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

@Suppress("unused")
class BaseHttpClientTest : DescribeSpec({
    describe("Base Http Client") {
        describe("path params") {
            it("should return error if value for param not found") {
                val result = replacePathParams("customer/:id/account/:accountNumber", emptyMap())
                val error = result.shouldBeLeft()

                error.type.shouldBe(ILLEGAL_ARGUMENT_ERROR_TYPE)
            }

            it("should replace slugs in path") {
                val result = replacePathParams(
                    "customer/:id/account/:accountNumber", mapOf(
                        "id" to "123",
                        "accountNumber" to "456"
                    )
                )

                result.shouldBeRight("customer/123/account/456")
            }
        }

        describe("query params") {
            it("should create query string") {
                val queryParams = mapOf(
                    "a" to "1",
                    "x" to "foo"
                )

                val qs = createQueryString(queryParams)

                // we can't guarantee the order of the parts of the query string.
                qs.shouldContain("&")
                qs.shouldContain("a=1")
                qs.shouldContain("x=foo")
            }

            it("should urlencode query parameters") {
                val queryParams = mapOf(
                    "callback" to "http://localhost:5000"
                )

                val qs = createQueryString(queryParams)

                qs.shouldBe("?callback=http%3A%2F%2Flocalhost%3A5000")
            }

            it("should return empty string when no params") {
                val qs = createQueryString(emptyMap())

                qs.shouldBe("")
            }
        }
    }
})
