package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either
import au.com.redcrew.apisdkcreator.httpclient.data.aHttpRequest
import au.com.redcrew.apisdkcreator.httpclient.matchers.notEqual
import au.com.redcrew.apisdkcreator.httpclient.matchers.test
import au.com.redcrew.apisdkcreator.httpclient.matchers.tests
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

@Suppress("unused")
class RequestTest : DescribeSpec({
    describe("Http Request") {
        describe("adding headers") {
            val request = aHttpRequest<Any>().addHeader("authorization", "foo").build()

            it("should add to existing headers") {
                val headers = mapOf("x-app-header" to "abc123")

                val result = addHeaders { Either.Right(headers) }(request).shouldBeRight()

                result.headers["authorization"].shouldBe(request.headers["authorization"])
                result.headers["x-app-header"].shouldBe(headers["x-app-header"])
                result.url.shouldBe(request.url)
                result.method.shouldBe(request.method)
            }

            it("should return error if factory fails to create headers") {
                val error = SdkError("fake-error", "Can't create headers")

                addHeaders { Either.Left(error) }(request).shouldBeLeft(error)
            }
        }

        describe("resolving url") {
            val base = "http://localhost:3000"

            it("should join path to base") {
                val path = "/v1/foo/bar"
                val request = aHttpRequest<Any>().withUrl(HttpRequestUrl.String(path)).build()

                val result = resolveUrl(base)(request).shouldBeRight()

                result.url.shouldBe(HttpRequestUrl.String("${base}${path}"))
            }
        }

        describe("copyWithBody") {
            val request = HttpRequest(
                method = HttpRequestMethod.GET,
                url = HttpRequestUrl.String("http://localhost/superhero/:id"),
                headers = mapOf("content-type" to "text/plain"),
                pathParams = mapOf("id" to "batman"),
                queryParams = mapOf("whois" to "true"),
                body = "Who is Batman"
            )

            val method = HttpRequestMethod.POST
            val url = HttpRequestUrl.String("http://localhost/add/:a")
            val headers = mapOf("content-type" to "application/json")
            val pathParams = mapOf("a" to "1")
            val queryParams = emptyMap<String, String>()
            val body = 43

            it("should change method") {
                val result = request.copyWithBody(method = method, body = body)

                result.should(beRequest(request, method = method, body = body))
            }

            it("should change url") {
                val result = request.copyWithBody(url = url, body = body)

                result.should(beRequest(request, url = url, body = body))
            }

            it("should change headers") {
                val result = request.copyWithBody(headers = headers, body = body)

                result.should(beRequest(request, headers = headers, body = body))
            }

            it("should change path params") {
                val result = request.copyWithBody(pathParams = pathParams, body = body)

                result.should(beRequest(request, pathParams = pathParams, body = body))
            }

            it("should change query parameters") {
                val result = request.copyWithBody(queryParams = queryParams, body = body)

                result.should(beRequest(request, queryParams = queryParams, body = body))
            }

            it("should change body only") {
                val result = request.copyWithBody(body = body)

                result.should(beRequest(request, body = body))
            }
        }
    }
})

fun beRequest(
    request: HttpRequest<*>,
    method: HttpRequestMethod = request.method,
    url: HttpRequestUrl = request.url,
    headers: HttpHeaders = request.headers,
    pathParams: Map<String, String>? = request.pathParams,
    queryParams: Map<String, String>? = request.queryParams,
    body: Any? = request.body
) =
    object : Matcher<HttpRequest<*>> {
        override fun test(value: HttpRequest<*>): MatcherResult =
            Matcher.tests(
                MatcherResult.test(::notEqual, method, value.method),
                MatcherResult.test(::notEqual, url, value.url),
                MatcherResult.test(::notEqual, headers, value.headers),
                MatcherResult.test(::notEqual, pathParams, value.pathParams),
                MatcherResult.test(::notEqual, queryParams, value.queryParams),
                MatcherResult.test(::notEqual, body, value.body)
            )
    }
