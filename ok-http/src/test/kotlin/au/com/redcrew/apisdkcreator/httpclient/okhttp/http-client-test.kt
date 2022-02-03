package au.com.redcrew.apisdkcreator.httpclient.okhttp

import au.com.redcrew.apisdkcreator.httpclient.*
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.ints.shouldBeGreaterThanOrEqual
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import kotlinx.coroutines.runBlocking
import okhttp3.Headers.Companion.toHeaders
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.net.URL
import java.util.concurrent.TimeUnit

/*
 * Note: describe() blocks can't have the same name, otherwise the `beforeEach` blocks will attach to every describe
 * block and execute, which will cause erroneous outcomes for the test by modifying the MockWebServer state for
 * unrelated scenarios.
 */
@Suppress("NAME_SHADOWING")
class OkHttpHttpClientTest : DescribeSpec({
    describe("OkHttp HttpClient") {
        lateinit var mockWebServer: MockWebServer
        lateinit var url: String
        lateinit var request: HttpRequest<UnstructuredData>

        beforeEach {
            /*
             * When shutting down the server, the "is started" flag isn't cleared, so the server can't be reused.
             */
            mockWebServer = MockWebServer()
            mockWebServer.start()

            url = "http://127.0.0.1:${mockWebServer.port}"

            request = HttpRequest(
                method = HttpRequestMethod.GET,
                url = HttpRequestUrl.String(url),
                headers = emptyMap(),
                pathParams = emptyMap(),
                queryParams = emptyMap(),
                body = null
            )
        }

        afterEach {
            mockWebServer.shutdown()
        }

        suspend fun sendRequest(
            request: HttpRequest<UnstructuredData>,
            expectedResponseStatusCode: Int = 200
        ): HttpResponse<UnstructuredData> {
            val response = okHttpClient()(request).shouldBeRight().response
            response.statusCode.shouldBe(expectedResponseStatusCode)

            return response
        }

        fun recordedRequest(): RecordedRequest? =
            mockWebServer.takeRequest(2000L, TimeUnit.MILLISECONDS)

        describe("request") {
            describe("method") {
                HttpRequestMethod.values().forEach { method ->
                    it("should use $method method") {
                        runBlocking {
                            val request = request.copy(
                                headers = request.headers + mapOf("content-type" to "text/plain"),
                                body = UnstructuredData.String("Hello World")
                            )

                            mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))

                            sendRequest(request.copy(method = method))

                            // check the request was correct
                            val result = recordedRequest()
                            result.shouldNotBe(null)
                            result?.method.shouldBe(method.toString())
                        }
                    }
                }
            }

            describe("url") {
                beforeEach {
                    mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))
                }

                it("should use URL from URL") {
                    runBlocking {
                        val request = request.copy(url = HttpRequestUrl.URL(URL(url)))

                        sendRequest(request)

                        val result = recordedRequest()
                        result.shouldNotBe(null)
                    }
                }

                it("should use URL from string") {
                    runBlocking {
                        sendRequest(request)

                        val result = recordedRequest()
                        result.shouldNotBe(null)
                    }
                }
            }

            describe("headers") {
                beforeEach {
                    mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))
                }

                it("should copy headers") {
                    runBlocking {
                        val name = "x-header-name"
                        val value = "value"
                        val request = request.copy(headers = request.headers + mapOf(name to value))

                        sendRequest(request)

                        val result = recordedRequest()
                        result?.shouldNotBe(null)
                        result?.headers!!.size.shouldBeGreaterThanOrEqual(2)
                        result.headers[name].shouldBe(value)
                    }
                }

                it("should filter out content type header when no body") {
                    runBlocking {
                        val request = request.copy(body = null)

                        sendRequest(request)

                        val result = recordedRequest()
                        result!!.headers["content-type"].shouldBe(null)
                    }
                }
            }

            describe("path params") {
                lateinit var urlWithPath: String

                beforeEach {
                    mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))

                    urlWithPath = "$url/:id"
                }

                it("should replace slugs with values") {
                    runBlocking {
                        val request = request.copy(url = HttpRequestUrl.String(urlWithPath), pathParams = mapOf("id" to "123"))

                        sendRequest(request)

                        val result = recordedRequest()
                        result?.path!!.shouldContain("/123")
                    }
                }

                it("should return error if no value for slug") {
                    runBlocking {
                        val request = request.copy(url = HttpRequestUrl.String(urlWithPath), pathParams = null)

                        val result = okHttpClient()(request).shouldBeLeft()
                        result.type.shouldBe(ILLEGAL_ARGUMENT_ERROR_TYPE)
                    }
                }
            }

            describe("query params") {
                beforeEach {
                    mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))

                    request = request.copy(queryParams = mapOf("type" to "xml"))
                }

                it("should append query parameters") {
                    runBlocking {
                        sendRequest(request)

                        val result = recordedRequest()
                        result?.requestUrl?.query.shouldBe("type=xml")
                    }
                }
            }

            describe("request body") {
                beforeEach {
                    mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))

                    request = request.copy(
                        method = HttpRequestMethod.POST,
                        headers = request.headers + mapOf("content-type" to "text/plain"),
                        body = UnstructuredData.String("Hello World")
                    )
                }

                it("should write string into request") {
                    runBlocking {
                        val body = "This is some data"
                        val request = request.copy(body = UnstructuredData.String(body))

                        sendRequest(request)

                        val result = recordedRequest()
                        result?.body?.readUtf8().shouldBe(body)
                    }
                }

                it("should return error if body present but no content type") {
                    runBlocking {
                        val request = request.copy(headers = emptyMap())

                        val result = okHttpClient()(request).shouldBeLeft()
                        result.message.shouldBe("Missing content-type")
                    }
                }

                it("should ignore body when body should not be sent") {
                    runBlocking {
                        val request = request.copy(method = HttpRequestMethod.GET)

                        sendRequest(request)

                        val result = recordedRequest()
                        result?.body?.size.shouldBe(0)
                    }
                }
            }
        }

        describe("response") {
            describe("errors") {
                it("should return error when non HTTP error occurs") {
                    runBlocking {
                        val request = request.copy(url = HttpRequestUrl.String("http://127.0.0.1:8080"))

                        okHttpClient()(request).shouldBeLeft()

                        Unit
                    }
                }
            }

            describe("status") {
                it("should return success status code") {
                    runBlocking {
                        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(""))

                        val result = okHttpClient()(request).shouldBeRight()
                        result.response.statusCode.shouldBe(200)
                    }
                }

                /*
                 * HttpClient libraries often treat errors differently from success eg: throw rather than return.
                 */
                it("should return error status code") {
                    runBlocking {
                        mockWebServer.enqueue(MockResponse().setResponseCode(500))

                        val result = okHttpClient()(request).shouldBeRight()
                        result.response.statusCode.shouldBe(500)
                    }
                }

                it("should return status message") {
                    runBlocking {
                        mockWebServer.enqueue(MockResponse().setResponseCode(404))

                        val result = okHttpClient()(request).shouldBeRight()
                        result.response.statusMessage.shouldBe("Client Error")
                    }
                }
            }

            describe("response headers") {
                it("should return headers") {
                    runBlocking {
                        val headers = mapOf(
                            "x-my-header" to "value",
                            "x-my-other-header" to "other value",

                            // MockWebserver puts this in automatically
                            "content-length" to "0"
                        )

                        mockWebServer.enqueue(
                            MockResponse()
                                .setResponseCode(200)
                                .setHeaders(headers.toHeaders())
                                .setBody("")
                        )

                        val result = okHttpClient()(request).shouldBeRight()
                        result.response.headers.shouldContainAll(headers)
                    }
                }

                it("should lowercase all headers") {
                    runBlocking {
                        val headers = mapOf("x-MY-Header" to "value")

                        mockWebServer.enqueue(
                            MockResponse()
                                .setResponseCode(200)
                                .setHeaders(headers.toHeaders())
                                .setBody("")
                        )

                        val result = okHttpClient()(request).shouldBeRight()
                        result.response.headers["x-my-header"].shouldBe("value")
                    }
                }
            }

            describe("response body") {
                it("should return string") {
                    runBlocking {
                        val body = "This is some data"

                        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(body))

                        val result = okHttpClient()(request).shouldBeRight()
                        result.response.body.shouldBe(UnstructuredData.String(body))
                    }
                }

                it("should return null when no content returned from server") {
                    runBlocking {
                        mockWebServer.enqueue(MockResponse().setResponseCode(200))

                        val result = okHttpClient()(request).shouldBeRight()
                        result.response.body.shouldBe(null)
                    }
                }
            }
        }
    }
})
