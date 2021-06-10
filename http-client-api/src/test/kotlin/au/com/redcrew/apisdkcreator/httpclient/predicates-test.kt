package au.com.redcrew.apisdkcreator.httpclient

import au.com.redcrew.apisdkcreator.httpclient.data.aHttpRequest
import au.com.redcrew.apisdkcreator.httpclient.data.aHttpResponse
import au.com.redcrew.apisdkcreator.httpclient.test.FunctionSource
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import java.util.stream.Stream

data class TestData(
    val code: Int,
    val successful: Boolean
)

@DisplayName("predicates")
class PredicatesTest {
    companion object {
        fun data(): Stream<Arguments> =
            Stream.of(
                Arguments.of(199, false, "unsuccessful"),
                Arguments.of(200, true, "successful"),
                Arguments.of(299, true, "successful"),
                Arguments.of(300, false, "unsuccessful"),
            )
    }

    @Nested
    @DisplayName("is successful")
    inner class SuccessfulTests {
        @Nested
        @DisplayName("successful response")
        inner class SuccessfulResponseTests {
            @ParameterizedTest(name = "should determine that {0} is {2}")
            @FunctionSource("data")
            fun checkSuccessfulResponseGenerator(code: Int, successful: Boolean, successOrNot: String) {
                val response = aHttpResponse<Any>().withStatusCode(code).build()

                assertThat(isSuccessfulResponse(response), equalTo(successful))
            }
        }

        @Nested
        @DisplayName("successful result")
        inner class SuccessfulResultTests {
            @ParameterizedTest(name = "should determine that {0} is {2}")
            @FunctionSource("data")
            fun checkSuccessfulResultGenerator(code: Int, successful: Boolean, successOrNot: String) {
                val result = HttpResult(
                    aHttpRequest<Any>().build(),
                    aHttpResponse<Any>().withStatusCode(code).build()
                )

                assertThat(isSuccessfulResult(result), equalTo(successful))
            }
        }
    }
}
