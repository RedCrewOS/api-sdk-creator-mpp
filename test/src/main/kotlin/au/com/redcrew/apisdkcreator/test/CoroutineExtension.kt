package au.com.redcrew.apisdkcreator.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver

@ExperimentalCoroutinesApi
class CoroutineExtension(
    private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()
) : AfterAllCallback, ParameterResolver {
    override fun afterAll(context: ExtensionContext?) {
        testDispatcher.cleanupTestCoroutines()
    }

    override fun supportsParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Boolean =
        parameterContext?.parameter?.type == TestCoroutineDispatcher::class.java

    override fun resolveParameter(parameterContext: ParameterContext?, extensionContext: ExtensionContext?): Any =
        testDispatcher
}
