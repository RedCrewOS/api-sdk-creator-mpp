package au.com.redcrew.apisdkcreator.test

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.support.AnnotationConsumer
import java.util.*
import java.util.stream.Stream
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.functions

class FunctionSourceArgumentsProvider : ArgumentsProvider, AnnotationConsumer<FunctionSource> {
    private lateinit var functionName: String

    override fun provideArguments(context: ExtensionContext): Stream<Arguments>? {
        return getCompanionObject(Optional.of(context))
            .flatMap(::getFunction)
            .flatMap(::getValue)
            .orElseThrow { IllegalArgumentException("Failed to load test arguments") }
    }

    override fun accept(functionSource: FunctionSource) {
        functionName = functionSource.value
    }

    private fun getCompanionObject(context: Optional<ExtensionContext>): Optional<Pair<KClass<*>, Any?>> {
        return context.flatMap { ec ->
            val companion = ec.testClass.map { cls ->
                cls.kotlin.companionObject?.let { Pair(it, cls.kotlin.companionObjectInstance) }
            }

            when {
                companion.isPresent -> companion
                else -> getCompanionObject(ec.parent)
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getFunction(data: Pair<KClass<*>, Any?>): Optional<Pair<KFunction<Stream<Arguments>>, Any?>> {
        val fn = data.first.functions.find { fn -> fn.name == this.functionName } as KFunction<Stream<Arguments>>?

        return fn?.let { Optional.of(Pair(fn, data.second)) } ?: Optional.empty()
    }

    private fun getValue(data: Pair<KFunction<Stream<Arguments>>, Any?>): Optional<Stream<Arguments>> =
         Optional.of(data.first.call(data.second))
}
