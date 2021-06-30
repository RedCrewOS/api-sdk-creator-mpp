package au.com.redcrew.apisdkcreator.test

import org.junit.jupiter.params.provider.ArgumentsSource

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@ArgumentsSource(FunctionSourceArgumentsProvider::class)
annotation class FunctionSource(
    /**
     * The name of the static variable
     */
    val value: String
)
