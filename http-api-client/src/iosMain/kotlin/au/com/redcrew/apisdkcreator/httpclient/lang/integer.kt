package au.com.redcrew.apisdkcreator.httpclient.lang

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import au.com.redcrew.apisdkcreator.httpclient.INVALID_NUMBER_ERROR_TYPE
import au.com.redcrew.apisdkcreator.httpclient.SdkError
import platform.Foundation.NSNumberFormatter

private fun toInvalidNumberError(message: String?, cause: Any): SdkError =
    SdkError(INVALID_NUMBER_ERROR_TYPE, message ?: "", cause)

actual fun parseInt(value: String): Either<SdkError, Int> =
    when(val num = NSNumberFormatter().numberFromString(value)) {
        null -> toInvalidNumberError("Invalid number", "'$value' not a valid number").left()
        else -> num.intValue.right()
    }
