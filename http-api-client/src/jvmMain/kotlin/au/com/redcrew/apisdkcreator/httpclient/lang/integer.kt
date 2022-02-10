package au.com.redcrew.apisdkcreator.httpclient.lang

import arrow.core.Either
import au.com.redcrew.apisdkcreator.httpclient.INVALID_NUMBER_ERROR_TYPE
import au.com.redcrew.apisdkcreator.httpclient.SdkError

private fun toInvalidNumberError(message: String?, cause: Any): SdkError =
    SdkError(INVALID_NUMBER_ERROR_TYPE, message ?: "", cause)

actual fun parseInt(value: String): Either<SdkError, Int> =
    Either.catch(
        { toInvalidNumberError(it.message, it) },
        { Integer.parseInt(value) }
    )
