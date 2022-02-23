package au.com.redcrew.apisdkcreator.httpclient

import au.com.redcrew.apisdkcreator.httpclient.matchers.notEqual
import au.com.redcrew.apisdkcreator.httpclient.matchers.test
import au.com.redcrew.apisdkcreator.httpclient.matchers.tests
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
            Matcher.tests(
                MatcherResult.test(::notEqual, statusCode, value.statusCode),
                MatcherResult.test(::notEqual, statusMessage, value.statusMessage),
                MatcherResult.test(::notEqual, headers, value.headers),
                MatcherResult.test(::notEqual, body, value.body),
            )
    }
