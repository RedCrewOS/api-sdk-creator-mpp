package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either
import arrow.core.right
import au.com.redcrew.apisdkcreator.httpclient.data.aHttpRequest
import au.com.redcrew.apisdkcreator.httpclient.data.aHttpResponse
import io.kotest.assertions.arrow.core.shouldBeLeft
import io.kotest.assertions.arrow.core.shouldBeRight
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

@Suppress("NAME_SHADOWING", "unused")
class JsonMarshallingTest : DescribeSpec({
    describe("JSON Marshalling") {
        data class TestBody(
            val a: Int,
            val b: Map<String, String>,
            val c: Boolean
        )

        val body = TestBody(1, mapOf("x" to "y"), true)
        val json = UnstructuredData.String("{ \"a\": 1, \"b\": { \"x\": \"y\" }, \"c\": true }")

        describe("marshaller") {
            val marshaller: Marshaller = { json.right() }
            val request = aHttpRequest<TestBody>().withBody(body).build()

            suspend fun marshall(contentType: String): Either<SdkError, HttpRequest<UnstructuredData>> =
                jsonMarshaller(marshaller, contentType)(request)

            suspend fun marshall(): Either<SdkError, HttpRequest<UnstructuredData>> =
                jsonMarshaller(marshaller)(request)

            it("should set content type") {
                val contentType = "application/json+vnd"

                val result = marshall(contentType).shouldBeRight()

                result.headers["content-type"].shouldBe(contentType)
            }

            it("should default content type") {
                val result = marshall().shouldBeRight()

                result.headers["content-type"].shouldBe(JSON_MIME_TYPE)
            }
        }

        describe("unmarshaller") {
            val unmarshaller: Unmarshaller<TestBody> = { body.right() }

            val result = HttpResult(
                request = aHttpRequest<Any>().build(),
                response = aHttpResponse<UnstructuredData>()
                    .withHeaders(mapOf("content-type" to "$JSON_MIME_TYPE ; charset=utf8"))
                    .withBody(json)
                    .build()
            )

            suspend fun unmarshall(contentType: String): Either<SdkError, HttpResult<*, TestBody>> =
                jsonUnmarshaller(unmarshaller, contentType)(result)

            suspend fun unmarshall(): Either<SdkError, HttpResult<*, TestBody>> =
                jsonUnmarshaller(unmarshaller)(result)

            it("should set content type") {
                val contentType = "text/plain"

                // since we want to unmarshall text/plain we should ignore the data and get back an error
                val result = unmarshall(contentType).shouldBeLeft()

                result.type.shouldBe(UNMARSHALLING_ERROR_TYPE)
            }

            it("should default content type") {
                val result = unmarshall().shouldBeRight()

                result.response.body.shouldBe(body)
            }
        }
    }
})
