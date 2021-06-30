package au.com.redcrew.apisdkcreator.httpclient

import au.com.redcrew.apisdkcreator.httpclient.kotlin.GenericTypeCurriedFunction
import kotlin.reflect.KClass

/**
 * Default mime type for JSON.
 */
const val JSON_MIME_TYPE = "application/json"

/**
 * A factory function to aid in marshalling JSON data into a HttpRequest.
 */
fun jsonMarshaller(contentType: String = JSON_MIME_TYPE): (Marshaller) -> HttpRequestPolicy<*, UnstructuredData> =
    marshallerFor(contentType)

/**
 * A factory function to aid in unmarshalling JSON data
 *
 * The returned HttpResultHandler will return an Exception if anything other than the given content type is
 * in the HttpResponse.
 */
// jsonUnmarshaller :: (KClass<T> -> Unmarshaller<T>, String) -> KClass<T> -> HttpResultHandler<*, UnstructuredData, T>
fun jsonUnmarshaller(unmarshallerFactory: GenericTypeUnmarshaller, contentType: String = JSON_MIME_TYPE): GenericJsonResultHandler =
    object : GenericJsonResultHandler {
        override fun <T : Any> invoke(p1: KClass<T>): HttpResultHandler<*, UnstructuredData, T> {
            return unmarshaller(unmarshallerFor(contentType)(unmarshallerFactory(p1)))
        }
    }

// GenericJsonResultHandler :: (KClass<T>) -> HttpResultHandler<*, UnstructuredData, T>
interface GenericJsonResultHandler: GenericTypeCurriedFunction {
    operator fun <T : Any> invoke(p1: KClass<T>): HttpResultHandler<*, UnstructuredData, T>
}
