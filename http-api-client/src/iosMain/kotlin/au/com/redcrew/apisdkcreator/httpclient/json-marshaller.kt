package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either

// jsonMarshaller :: (a -> Either<SdkError, UnstructuredData>, String) -> HttpRequestPolicy<* UnstructuredData>
@Suppress("MoveLambdaOutsideParentheses")
fun jsonMarshaller(
    marshaller: (Any) -> Either<SdkError, UnstructuredData>,
    contentType: String = JSON_MIME_TYPE
): HttpRequestPolicy<*, UnstructuredData> =
    marshallerFor(contentType)({ marshaller(it) })

// jsonUnmarshaller :: (UnstructuredData -> Either<SdkError, T>, String) -> HttpResultHandler<* UnstructuredData, T>
fun <T : Any> jsonUnmarshaller(
    unmarshallerFactory: (UnstructuredData) -> Either<SdkError, T>,
    contentType: String = JSON_MIME_TYPE
): HttpResultHandler<*, UnstructuredData, T> =
    unmarshaller(unmarshallerFor(contentType)(unmarshallerFactory))
