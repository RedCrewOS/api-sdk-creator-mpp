package au.com.redcrew.apisdkcreator.test

import java.lang.IllegalStateException

fun <T> throwException(ex: Exception): T {
    throw ex
}

fun throwException() {
    throw IllegalStateException("Expected exception to be returned")
}

fun throwException(@Suppress("UNUSED_PARAMETER") value: Any) {
    throw IllegalStateException("Expected exception to be returned")
}
