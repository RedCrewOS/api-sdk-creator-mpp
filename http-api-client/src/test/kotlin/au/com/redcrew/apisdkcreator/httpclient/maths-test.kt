package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.identity
import au.com.redcrew.apisdkcreator.httpclient.test.throwException
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.lang.NumberFormatException

class MathsTest {
    @Test
    fun `should add two numbers`() {
        assertThat(add(2, 3), equalTo(5))
    }

    @Test
    fun `should multiply two numbers`() {
        assertThat(multiply(5, 3), equalTo(15))
    }

    @Test
    fun `should multiply by two then add three`() {
        assertThat(m2a3(2), equalTo(7))
    }

    // see https://craigrussell.io/2019/11/unit-testing-coroutine-suspend-functions-using-testcoroutinedispatcher/
    @ExperimentalCoroutinesApi
    @Nested
    inner class SuspendingMathsTest {
        private val testDispatcher = TestCoroutineDispatcher()

        @Test
        fun `should add two numbers`() = testDispatcher.runBlockingTest {
            assertThat(susAdd(2, 3), equalTo(5))
        }

        @Test
        fun `should multiply two numbers`() = testDispatcher.runBlockingTest {
            assertThat(susMultiply(5, 3), equalTo(15))
        }

        @Test
        fun `should multiply by two then add three`() = testDispatcher.runBlockingTest {
            assertThat(susM2A3(2), equalTo(7))
        }

        @Test
        fun `should add list of strings`() = testDispatcher.runBlockingTest {
            val values = listOf("1", "2", "3")

            val result = sumStrings(values).fold(::throwException, ::identity)

            assertThat(result, equalTo(6))
        }

        @Test
        fun `should return error when unable to parse string to int`() = testDispatcher.runBlockingTest {
            val values = listOf("1", "x", "3")

            val result = sumStrings(values).fold(::identity, ::throwException)

            assertThat(result is NumberFormatException, equalTo(true))
        }
    }
}
