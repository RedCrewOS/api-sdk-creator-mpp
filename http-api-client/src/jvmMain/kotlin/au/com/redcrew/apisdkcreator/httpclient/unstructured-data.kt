package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either
import arrow.core.left
import kotlin.reflect.KClass

abstract class UnstructuredDataToGenericTypeUnmarshaller : GenericTypeUnmarshaller {
    override fun <T : Any> invoke(p1: KClass<T>): Unmarshaller<T> {
        return { data: UnstructuredData ->
            @Suppress("REDUNDANT_ELSE_IN_WHEN")
            when (data) {
                is UnstructuredData.String -> unmarshallString(p1, data.data)

                // future proofing
                else -> SdkError(ILLEGAL_STATE_ERROR_TYPE, "Unrecognised unstructured data type").left()
            }
        }
    }

    abstract fun <T : Any> unmarshallString(cls: KClass<T>, data: String): Either<SdkError, T>
}