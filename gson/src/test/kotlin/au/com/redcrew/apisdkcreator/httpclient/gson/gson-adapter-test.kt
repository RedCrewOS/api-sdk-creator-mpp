package au.com.redcrew.apisdkcreator.httpclient.gson

import arrow.core.identity
import au.com.redcrew.apisdkcreator.httpclient.UNMARSHALLING_ERROR_TYPE
import au.com.redcrew.apisdkcreator.httpclient.UnstructuredData
import au.com.redcrew.apisdkcreator.test.CoroutineExtension
import au.com.redcrew.apisdkcreator.test.throwException
import au.com.redcrew.apisdkcreator.test.throwSdkError
import com.google.gson.Gson
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Ignore

data class MyType(
    val x: Int,
    val y: List<String>
)

@ExperimentalCoroutinesApi
@ExtendWith(CoroutineExtension::class)
@DisplayName("Gson adapter")
class GsonAdapterTest(val dispatcher: TestCoroutineDispatcher) {
    private val gson = Gson()
    private val obj = MyType(1, listOf("a", "b", "c"))
    private val json = "{\"x\":1,\"y\":[\"a\",\"b\",\"c\"]}"

    @Nested
    @DisplayName("marshalling")
    inner class MarshallerTest {
        @Test
        fun `should marshall any object`() = dispatcher.runBlockingTest {
            val result = gsonMarshaller(gson)(obj).fold(::throwSdkError, ::identity)

            assertThat(result, equalTo(UnstructuredData.String(json)))
        }
    }

    @Nested
    @DisplayName("unmarhsalling")
    inner class UnmarshallerTest {
        private val unmarshaller = gsonUnmarshaller(gson)(MyType::class)

        @Test
        fun `should deserialise any object`() = dispatcher.runBlockingTest {
            val result: MyType = unmarshaller(UnstructuredData.String(json)).fold(::throwSdkError, ::identity)

            assertThat(result, equalTo(obj))
        }

        @Ignore
        @Test
        fun `should catch error deserialising object`() = dispatcher.runBlockingTest {
            val result = unmarshaller(UnstructuredData.String("{")).fold(::identity, ::throwException)

            assertThat(result.type, equalTo(UNMARSHALLING_ERROR_TYPE))
        }
    }
}
