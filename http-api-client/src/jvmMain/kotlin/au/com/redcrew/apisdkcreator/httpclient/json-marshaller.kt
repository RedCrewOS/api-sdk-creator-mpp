package au.com.redcrew.apisdkcreator.httpclient

import au.com.redcrew.apisdkcreator.httpclient.kotlin.GenericTypeCurriedFunction
import kotlin.reflect.KClass

/**
 * A factory function to aid in marshalling JSON data into a HttpRequest.
 */
fun jsonMarshaller(marshaller: Marshaller, contentType: String = JSON_MIME_TYPE): HttpRequestPolicy<*, UnstructuredData> =
    marshallerFor(contentType)(marshaller)

/**
 * A factory function to aid in unmarshalling JSON data
 *
 * The returned HttpResultHandler will return an SdkError if anything other than the given content type is
 * in the HttpResponse.
 */
// jsonUnmarshaller :: (KClass<T> -> Unmarshaller<T>, String) -> KClass<T> -> HttpResultHandler<*, UnstructuredData, T>
fun jsonUnmarshaller(unmarshallerFactory: GenericTypeUnmarshaller, contentType: String = JSON_MIME_TYPE): GenericJsonResultHandler =
    object : GenericJsonResultHandler {
        override fun <T : Any> invoke(p1: KClass<T>): HttpResultHandler<*, UnstructuredData, T> {
            return unmarshaller(unmarshallerFor(contentType)(unmarshallerFactory(p1)))
        }
    }

/**
 * A generic type unmarshaller is a function that, given any KClass, will return an instance of the type defined by
 * the class.
 *
 * This will be most often returned/used by functions that wrap JSON libraries like Gson, or Jackson.
 */
// GenericTypeUnmarshaller :: (KClass<T>) -> Unmarshaller<T>
interface GenericTypeUnmarshaller: GenericTypeCurriedFunction {
    operator fun <T : Any> invoke(p1: KClass<T>): Unmarshaller<T>
}

// GenericJsonResultHandler :: (KClass<T>) -> HttpResultHandler<*, UnstructuredData, T>
interface GenericJsonResultHandler: GenericTypeCurriedFunction {
    operator fun <T : Any> invoke(p1: KClass<T>): HttpResultHandler<*, UnstructuredData, T>
}
