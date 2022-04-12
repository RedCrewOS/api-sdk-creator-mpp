package au.com.redcrew.apisdkcreator.httpclient

/**
 * Creates a [HttpRequestPolicy] that tries to marshall a [HttpRequest] body to a string.
 *
 * @param marshaller How to convert a type to [UnstructuredData]
 * @param contentType Optional content type. Defaults to [JSON_MIME_TYPE]
 * @return A request policy
 */
fun jsonMarshaller(marshaller: Marshaller, contentType: String = JSON_MIME_TYPE): HttpRequestPolicy<*, UnstructuredData> =
    marshallerFor(contentType)(marshaller)

/**
 * Creates a [HttpResultHandler] that tries to unmarshall [UnstructuredData] to a type.
 *
 * The returned HttpResultHandler will return an error if anything other than the given
 * content type is in the [HttpResponse].
 *
 * @param jsonUnmarshaller How to convert UnstructuredData to a type.
 * @param contentType Optional content type. Defaults to [JSON_MIME_TYPE]
 * @return A result handler
 */
// jsonUnmarshaller :: (Unmarshaller<T>, String?) -> KClass<T> -> HttpResultHandler<*, UnstructuredData, T>
fun <T : Any> jsonUnmarshaller(
    jsonUnmarshaller: Unmarshaller<T>,
    contentType: String = JSON_MIME_TYPE
): HttpResultHandler<*, UnstructuredData, T> =
    unmarshaller(unmarshallerFor(contentType)(jsonUnmarshaller))
