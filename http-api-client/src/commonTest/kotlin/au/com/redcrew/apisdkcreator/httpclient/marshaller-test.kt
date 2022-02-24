package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import au.com.redcrew.apisdkcreator.httpclient.data.aHttpRequest
import au.com.redcrew.apisdkcreator.httpclient.data.aHttpResponse
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

@Suppress("NAME_SHADOWING", "unused")
class MarshallingTest :DescribeSpec({
    describe("Marshalling") {
        /*
         * We will be using JSON as the test data type for marshalling/unmarshalling but any data type could be used.
         */
        data class TestBody(
            val a: Int,
            val b: Map<String, String>,
            val c: Boolean
        )

        val contentType = "application/json+vnd"
        val body = TestBody(1, mapOf("x" to "y"), true)
        val json = UnstructuredData.String("{ \"a\": 1, \"b\": { \"x\": \"y\" }, \"c\": true }")

        describe("marshallerFor") {
            val marshaller: Marshaller = { json.right() }
            val request = aHttpRequest<TestBody>().withBody(body).build()

            suspend fun marshall(
                aRequest: HttpRequest<TestBody> = request,
                aMarshaller: Marshaller = marshaller,
                aContentType: String = contentType,
            ): Either<SdkError, HttpRequest<UnstructuredData>> =
                marshallerFor(aContentType)(aMarshaller)(aRequest)

            it("should set content type") {
                val result = marshall().shouldBeRight()

                result.headers["content-type"].shouldBe(contentType)
            }

            it("should set no content type when no request body") {
                val request = aHttpRequest<TestBody>().build()

                val result = marshall(request).shouldBeRight()

                result.headers["content-type"].shouldBe(null)
            }

            it("should not marshall body when no request body") {
                val request = aHttpRequest<TestBody>().build()

                val result = marshall(request).shouldBeRight()

                result.body.shouldBe(null)
            }

            it("should marshall request body") {
                val result = marshall().shouldBeRight()

                result.body.shouldBe(json)
            }

            it("should return error when error marshalling request body") {
                val error = SdkError("fake-error", "fake marshalling error")
                val marshaller: Marshaller = { error.left() }

                val result = marshall(request, marshaller).shouldBeLeft()

                result.shouldBe(error)
            }
        }

        describe("unmarshallerFor") {
            val unmarshaller: Unmarshaller<TestBody> = { body.right() }
            val response = aHttpResponse<UnstructuredData>()
                .withHeaders(mapOf("content-type" to "$contentType ; charset=utf8"))
                .withBody(json)
                .build()

            suspend fun unmarshall(
                aResult: HttpResponse<UnstructuredData> = response,
                anUnmarshaller: Unmarshaller<TestBody> = unmarshaller,
                aContentType: String = contentType
            ): Either<SdkError, Either<HttpResponse<UnstructuredData>, HttpResponse<TestBody>>> =
                unmarshallerFor(aContentType)(anUnmarshaller)(aResult)

            it("should unmarshall response body") {
                val result = unmarshall().shouldBeRight().shouldBeRight { "Expected HttpResponse to have had body unmarshalled" }

                result.body.shouldBe(body)
            }

            it("should not unmarshall response body when content type does not match") {
                val response = aHttpResponse<UnstructuredData>()
                    .withHeaders(mapOf("content-type" to "text/plain"))
                    .withBody(json)
                    .build()

                val result = unmarshall(response).shouldBeRight().shouldBeLeft { "HttpResponse erroneously had body unmarshalled" }

                result.shouldBe(response)
            }

            it("should not unmarshall response body when no response body") {
                val resp = response.copy(body = null)

                // we expect an Either.Right as the result since there is no body.
                val result = unmarshall(resp).shouldBeRight().shouldBeRight { "Expected HttpResponse to have had body unmarshalled" }

                result.body.shouldBe(null)
            }

            it("should return error when error unmarshalling response body") {
                val error = SdkError("fake-error", "fake marshalling error")
                val unmarshaller: Unmarshaller<TestBody> = { error.left() }

                unmarshall(response, unmarshaller).shouldBeLeft(error)
            }
        }

        describe("unmarshaller") {
            val makeUnmarshaller: (String, String) -> ResponseUnmarshaller<String> =
                { contentType: String, data: String? ->
                    { response ->
                        when(response.body) {
                            null -> Either.Right(response.copyWithBody<String>(body = null).right())
                            else -> when {
                                response.headers.containsValue(contentType) ->
                                    Either.Right(response.copyWithBody(body = data).right())
                                else ->
                                    Either.Right(response.left())
                            }
                        }
                    }
                }

            val plainTextUnmarshaller = makeUnmarshaller("text/plain", "This is some text")
            val anotherPlainTextUnmarshaller = makeUnmarshaller("text/plain", "This is other text")
            val jsonUnmarshaller = makeUnmarshaller("application/json", "{ \"a\": false }")

            val unmarshallers =
                unmarshaller(jsonUnmarshaller, plainTextUnmarshaller, anotherPlainTextUnmarshaller)

            val responseHeaders = mapOf("content-type" to "text/plain")

            it("should return first unmarshalled body") {
                val result = HttpResult(
                    request = aHttpRequest<String>().build(),
                    response = aHttpResponse<UnstructuredData>()
                        .withHeaders(responseHeaders)
                        .withBody(UnstructuredData.String("Hello World"))
                        .build()
                )

                val body = unmarshallers(result).shouldBeRight().response.body

                body.shouldBe("This is some text")
            }

            it("should return unsupported content when response body not unmarshalled") {
                val result = HttpResult(
                    request = aHttpRequest<String>().build(),
                    response = aHttpResponse<UnstructuredData>()
                        .withHeaders(mapOf("content-type" to "image/png"))
                        .withBody(UnstructuredData.String("Hello World"))
                        .build()
                )

                val error = unmarshallers(result).shouldBeLeft()

                error.type.shouldBe(UNMARSHALLING_ERROR_TYPE)
                error.message.shouldBe("Unrecognised content type 'image/png'")
            }
        }
    }
})
