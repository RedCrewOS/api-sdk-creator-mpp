package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either
import kotlin.reflect.KClass

/**
 * Data type that does not have a pre-defined data model/type definition.
 *
 * @typedef {Buffer | | NodeJS.ReadableStream | ReadableStream | string} UnstructuredData
 */
sealed class UnstructuredData {
    data class String(val data: kotlin.String): UnstructuredData()
}

abstract class UnstructuredDataToGenericTypeUnmarshaller : GenericTypeUnmarshaller {
    override fun <T : Any> invoke(p1: KClass<T>): Unmarshaller<T> {
        return { data: UnstructuredData ->
            try {
                val result = when (data) {
                    is UnstructuredData.String -> unmarshallString(p1, data.data)
                    else -> throw IllegalStateException()
                }

                Either.Right(result)
            }
            catch(e: Exception) {
                Either.Left(e)
            }
        }
    }

    abstract fun <T : Any> unmarshallString(cls: KClass<T>, data: String): T
}
