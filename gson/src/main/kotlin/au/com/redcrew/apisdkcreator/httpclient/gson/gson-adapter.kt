package au.com.redcrew.apisdkcreator.httpclient.gson

import arrow.core.Either
import au.com.redcrew.apisdkcreator.httpclient.*
import com.google.gson.Gson
import kotlin.reflect.KClass

// gsonMarshaller :: Gson -> Marshaller
fun gsonMarshaller(gson: Gson): Marshaller = {
    try {
        Either.Right(UnstructuredData.String(gson.toJson(it)))
    }
    // we don't want to catch Throwables
    catch (e: Exception) {
        Either.Left(SdkError(MARSHALLING_ERROR_TYPE, e.message ?: "", e))
    }
}

// gsonUnmarshaller :: Gson -> KClass<T> -> Unmarshaller<T>
fun gsonUnmarshaller(gson: Gson): GenericTypeUnmarshaller =
    object : UnstructuredDataToGenericTypeUnmarshaller() {
        override fun <T : Any> unmarshallString(cls: KClass<T>, data: String): T =
            gson.fromJson(data, cls.java)
    }
