package au.com.redcrew.apisdkcreator.httpclient.kotlin

/**
 * Because Kotlin does not allow anonymous functions/lambdas to have generic type parameters, we are unable to write
 * curried functions where the generic type does not appear as part of the overall function signature.
 *
 * For example, the following code does not compile.
 *
 * ```
 * fun gsonUnmarshaller(gson: Gson): (KClass<T>) -> Unmarshaller<T>
 * ```
 *
 * The only option here to make it compile is to add `<T>` to the named function, to make `<T>` part of the overall
 * function signature; in this case `gsonUnmarshaller`. For example,
 *
 * ```
 * fun <T> gsonUnmarshaller(gson: Gson): (KClass<T>) -> Unmarshaller<T>
 * ```
 *
 * This however would bind all resulting functions to one type when the first function is called. If `gsonUnmarshaller`
 * was called with `String` then we can only ever call the result with `KClass<String>` leading to an
 * `Unmarshaller<String>`
 *
 * This inhibits reuse and the ability to partially apply functions and pass the result to other functions which may
 * want to work on a set of types that are not known to where the partial application is performed.
 *
 * For example, we'd want to pass an initial Gson configuration at the start of a pipeline to specify how all
 * API pipelines are to marshall/unmarshall JSON bodies according to the SDK/application needs. We then would want to
 * pass the curried function to SDK specific functions to add steps to the pipeline when the result type of the SDK
 * operation is known, while not having to perform any casts and maintain type safety.
 *
 * The solution is to take advantage of Kotlin's operator overloading to provide an object that knows what to do when
 * invoked; which makes it look like a function. This delays the binding of the generic type until "the function" is
 * invoked, or where the implementation class's `invoke` method is called, rather than when the curried signature is
 * defined.
 *
 * It adds an extra layer of indirection for implementation readers but results in nice type safe code in the caller.
 * Functions that utilise this technique for curried functions with generic type parameters should document the
 * function signature they are emulating using Hindley-Milner, which is the defacto way of specifying function type
 * signatures.
 *
 * @see https://kotlinlang.org/docs/operator-overloading.html#invoke-operator
 */
interface GenericTypeCurriedFunction
