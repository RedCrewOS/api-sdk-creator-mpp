package au.com.redcrew.apisdkcreator.httpclient.gson

import arrow.core.Either
import au.com.redcrew.apisdkcreator.httpclient.Marshaller
import au.com.redcrew.apisdkcreator.httpclient.GenericTypeUnmarshaller
import au.com.redcrew.apisdkcreator.httpclient.UnstructuredDataToGenericTypeUnmarshaller
import au.com.redcrew.apisdkcreator.httpclient.UnstructuredData
import com.google.gson.Gson
import kotlin.reflect.KClass

// gsonMarshaller :: Gson -> Marshaller
fun gsonMarshaller(gson: Gson): Marshaller = {
    try {
        Either.Right(UnstructuredData.String(gson.toJson(it)))
    }
    // we don't want to catch Throwables
    catch (e: Exception) {
        Either.Left(e)
    }
}

// gsonUnmarshaller :: Gson -> KClass<T> -> Unmarshaller<T>
fun gsonUnmarshaller(gson: Gson): GenericTypeUnmarshaller =
    object : UnstructuredDataToGenericTypeUnmarshaller() {
        override fun <T : Any> unmarshallString(cls: KClass<T>, data: String): T =
            gson.fromJson(data, cls.java)
    }
