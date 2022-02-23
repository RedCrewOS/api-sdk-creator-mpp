package au.com.redcrew.apisdkcreator.httpclient

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should

class ResponseTest : DescribeSpec({
    describe("Http Response") {
        describe("copyWithBody") {
            val response = HttpResponse(
                statusCode = 200,
                statusMessage = "OK",
                headers = mapOf("content-type" to "text/plain"),
                body = "Who is Batman"
            )

            val statusCode = 500
            val statusMessage = "Internal Server Error"
            val headers = mapOf("content-type" to "application/json")
            val body = 12345678

            it("should change status code") {
                val result = response.copyWithBody(statusCode = statusCode, body = body)

                result.should(beResponse(response, statusCode = statusCode, body = body))
            }

            it("should change status message") {
                val result = response.copyWithBody(statusMessage = statusMessage, body = body)

                result.should(beResponse(response, statusMessage = statusMessage, body = body))
            }

            it("should change headers") {
                val result = response.copyWithBody(headers = headers, body = body)

                result.should(beResponse(response, headers = headers, body = body))
            }

            it("should change body only") {
                val result = response.copyWithBody(body = body)

                result.should(beResponse(response, body = body))
            }
        }
    }
})

private fun beResponse(
    response: HttpResponse<*>,
    statusCode: Int = response.statusCode,
    statusMessage: String = response.statusMessage,
    headers: HttpHeaders = response.headers,
    body: Any? = response.body
) =
    object: Matcher<HttpResponse<*>> {
        override fun test(value: HttpResponse<*>): MatcherResult =
            when {
                value.statusCode != statusCode -> MatcherResult(false,
                    { "${value.statusCode} should be $statusCode" },
                    { "${value.statusCode} should not be $statusCode" }
                )

                value.statusMessage != statusMessage -> MatcherResult(false,
                    { "${value.statusMessage} should be $statusMessage" },
                    { "${value.statusMessage} should not be $statusMessage" }
                )

                value.headers != headers -> MatcherResult(false,
                    { "${value.headers} should be $headers" },
                    { "${value.headers} should not be $headers" }
                )

                value.body != body -> MatcherResult(false,
                    { "${value.body} should be $body" },
                    { "${value.body} should not be $body" }
                )

                else -> MatcherResult(true,
                    { "response matches expected" },
                    { "response does not match expected" }
                )
            }
    }
