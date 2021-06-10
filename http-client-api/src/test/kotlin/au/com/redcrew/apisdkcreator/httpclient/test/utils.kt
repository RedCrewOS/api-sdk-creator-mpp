package au.com.redcrew.apisdkcreator.httpclient.test

import java.lang.IllegalStateException

fun <T> throwException(ex: Exception): T {
    throw ex
}

fun throwException(@Suppress("UNUSED_PARAMETER") value: Any) {
    throw IllegalStateException("Expected exception to be returned")
}
