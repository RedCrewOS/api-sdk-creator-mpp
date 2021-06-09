package au.com.redcrew.apisdkcreator.httpclient

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


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
    }
}
