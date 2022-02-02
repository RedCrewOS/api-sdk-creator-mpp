package au.com.redcrew.apisdkcreator.httpclient

const val HTTP_CLIENT_ERROR_TYPE = "http-client-error"
const val ILLEGAL_ARGUMENT_ERROR_TYPE = "illegal-argument-error"
const val ILLEGAL_STATE_ERROR_TYPE = "illegal-state-error"
const val INVALID_NUMBER_ERROR_TYPE = "invalid-number-error"
const val MARSHALLING_ERROR_TYPE = "marshalling-error"
const val UNMARSHALLING_ERROR_TYPE = "unmarshalling-error"

/**
 * In order to have a language portable library, we can't rely on a particular language's modelling of errors
 * (eg: Java Exceptions).
 *
 * In functional programming Exceptions are impure. There are also not as helpful as in OO languages because the
 * stack traces are harder to understand due to function composition/currying.
 *
 * Therefore, a generic Error class is used.
 */
data class SdkError(
    val type: String,
    val message: String,
    val cause: Any?
) {
    constructor(type: String, message: String): this(type, message, null)

    fun toException() =
        Exception("$type - $message")

}
