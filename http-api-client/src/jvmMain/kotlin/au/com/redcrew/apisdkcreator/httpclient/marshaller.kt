package au.com.redcrew.apisdkcreator.httpclient

import au.com.redcrew.apisdkcreator.httpclient.kotlin.GenericTypeCurriedFunction
import kotlin.reflect.KClass

/**
 * A generic class unmarshaller is a function that, given any KClass, will return an instance of the type defined by
 * the class.
 *
 * This will be most often returned/used by functions that wrap JSON libraries like Gson, or Jackson.
 */
// GenericClassUnmarshaller :: (KClass<T>) -> Unmarshaller<T>
interface GenericClassUnmarshaller: GenericTypeCurriedFunction {
    operator fun <T : Any> invoke(p1: KClass<T>): Unmarshaller<T>
}
