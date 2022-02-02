package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.left
import kotlin.reflect.KClass

/**
 * Data type that does not have a pre-defined data model/type definition.
 *
 * @typedef {String} UnstructuredData
 */
sealed class UnstructuredData {
    data class String(val data: kotlin.String): UnstructuredData()
}

abstract class UnstructuredDataToGenericTypeUnmarshaller : GenericTypeUnmarshaller {
    override fun <T : Any> invoke(p1: KClass<T>): Unmarshaller<T> {
        return { data: UnstructuredData ->
            val result = when (data) {
                is UnstructuredData.String -> Option(unmarshallString(p1, data.data))
                else -> None
            }

            result.fold(
                { SdkError(ILLEGAL_STATE_ERROR_TYPE, "Unrecognised unstructured data type").left() },
                { Either.Right(it) }
            )
        }
    }

    abstract fun <T : Any> unmarshallString(cls: KClass<T>, data: String): T
}
