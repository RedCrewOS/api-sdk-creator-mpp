package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.identity
import au.com.redcrew.apisdkcreator.test.CoroutineExtension
import au.com.redcrew.apisdkcreator.test.throwException
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.reflect.KClass

@ExperimentalCoroutinesApi
@ExtendWith(CoroutineExtension::class)
@DisplayName("unstructured data")
class UnstructuredDataTest(val dispatcher: TestCoroutineDispatcher) {
    @Nested
    @DisplayName("UnstructuredDataToGenericTypeUnmarshaller")
    inner class UnstructuredDataToGenericTypeUnmarshallerTest {
        @Test
        fun `should return result of unmarshalling`() = dispatcher.runBlockingTest {
              val unmarshaller = object : UnstructuredDataToGenericTypeUnmarshaller() {
                @Suppress("UNCHECKED_CAST")
                override fun <T : Any> unmarshallString(cls: KClass<T>, data: String): T  = Integer.parseInt(data) as T
            }

            val result = unmarshaller(Int::class)(UnstructuredData.String("1234")).fold(::throwException, ::identity)

            assertThat(result, equalTo(1234))
        }

        @Test
        fun `should return error when unmarshalling fails`() = dispatcher.runBlockingTest {
            val error = Exception("Something went wrong")

            val unmarshaller = object : UnstructuredDataToGenericTypeUnmarshaller() {
                override fun <T : Any> unmarshallString(cls: KClass<T>, data: String): T {
                    throw error
                }
            }

            val result = unmarshaller(Int::class)(UnstructuredData.String("1234")).fold(::identity, ::throwException)

            assertThat(result, equalTo(error))
        }
    }
}
