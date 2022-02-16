package au.com.redcrew.apisdkcreator.httpclient

import au.com.redcrew.apisdkcreator.httpclient.data.aHttpRequest
import au.com.redcrew.apisdkcreator.httpclient.data.aHttpResponse
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe

data class TestData(
    val code: Int,
    val successful: Boolean
)

private fun successToString(successful: Boolean): String =
    when(successful) {
        true -> "successful"
        false -> "unsuccessful"
    }

class PredicatesTest : DescribeSpec({
    val data = listOf(
        TestData(199, false),
        TestData(200, true),
        TestData(299, true),
        TestData(300, false)
    )

    describe("predicates") {
        describe("is successful") {
            describe("successful response") {
                data.forEach { item ->
                    it("should determine that ${item.code} is ${successToString(item.successful)}") {
                        val response = aHttpResponse<Any>().withStatusCode(item.code).build()

                        isSuccessfulResponse(response).shouldBe(item.successful)
                    }
                }
            }

            describe("successful result") {
                data.forEach { pair ->
                    it("should determine that ${pair.code} is ${successToString(pair.successful)}") {
                        val result = HttpResult(
                            aHttpRequest<Any>().build(),
                            aHttpResponse<Any>().withStatusCode(pair.code).build()
                        )

                        isSuccessfulResult(result).shouldBe(pair.successful)
                    }
                }
            }
        }
    }
})
