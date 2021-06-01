package au.com.redcrew.apisdkcreator.httpclient

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
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
}
