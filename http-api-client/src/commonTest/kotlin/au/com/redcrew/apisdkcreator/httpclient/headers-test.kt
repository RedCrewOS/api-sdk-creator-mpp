package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

@Suppress("unused")
class HeadersTest : DescribeSpec({
    describe("Headers") {
       describe("header factory") {
            val factoryOne: RequestHeaderFactory = { Either.Right(mapOf("x-application-name" to "project")) }
            val factoryTwo: RequestHeaderFactory = { Either.Right(mapOf("x-api-key" to "abc123")) }

            it("should create request headers from List") {
                val factory: RequestHeadersFactory = createHeaders(factoryOne, factoryTwo)
                val headers: HttpHeaders = factory().shouldBeRight()

                headers["x-application-name"].shouldBe("project")
                headers["x-api-key"].shouldBe("abc123")
            }
        }

        describe("bearer token factory") {
            it("should create authorization header") {
                val token = "abc123"
                val result = bearerToken({ Either.Right(token) })(emptyMap()).shouldBeRight()

                result["authorization"].shouldBe("Bearer $token")
            }

            it("should use header name") {
                val headerName = "Authorization"
                val result = bearerToken({ Either.Right("abc123") }, headerName)(emptyMap()).shouldBeRight()

                result[headerName].shouldNotBeNull()
            }
        }

        describe("constant headers factory") {
            it("should return provided headers") {
                val headers = mapOf(
                    "x-application-name" to "project",
                    "x-api-key" to "abc123"
                )

                val result = constantHeaders(headers)(emptyMap()).shouldBeRight()

                result["x-application-name"].shouldBe("project")
                result["x-api-key"].shouldBe("abc123")
            }
        }
    }
})
