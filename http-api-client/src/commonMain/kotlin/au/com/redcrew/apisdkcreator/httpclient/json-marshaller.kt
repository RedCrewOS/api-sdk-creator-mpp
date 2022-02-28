package au.com.redcrew.apisdkcreator.httpclient

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
// jsonUnmarshaller :: (Unmarshaller<T>, String?) -> KClass<T> -> HttpResultHandler<*, UnstructuredData, T>
fun <T : Any> jsonUnmarshaller(jsonUnmarshaller: Unmarshaller<T>, contentType: String = JSON_MIME_TYPE): HttpResultHandler<*, UnstructuredData, T> =
    unmarshaller(unmarshallerFor(contentType)(jsonUnmarshaller))
