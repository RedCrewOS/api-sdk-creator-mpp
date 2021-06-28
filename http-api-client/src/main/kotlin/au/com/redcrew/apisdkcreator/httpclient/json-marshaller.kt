package au.com.redcrew.apisdkcreator.httpclient

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
fun <T> jsonUnmarshaller(contentType: String = JSON_MIME_TYPE): (Unmarshaller<T>) -> HttpResultHandler<*, UnstructuredData, T> =
    { unmarshaller -> unmarshaller(unmarshallerFor<T>(contentType)(unmarshaller)) }
