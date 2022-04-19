package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either

fun toMarshaller(marshaller: (Any) -> Either<SdkError, UnstructuredData>): Marshaller =
    { marshaller(it) }

fun <T> toUnmarshaller(unmarshaller: (UnstructuredData) -> Either<SdkError, T>): Unmarshaller<T> =
    { unmarshaller(it) }
