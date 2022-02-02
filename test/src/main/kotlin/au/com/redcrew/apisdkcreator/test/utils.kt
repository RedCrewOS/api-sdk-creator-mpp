package au.com.redcrew.apisdkcreator.test

import java.lang.IllegalStateException

fun <T> throwSdkError(value: Any): T {
    throw Exception(value.toString())
}

fun <T> throwException(@Suppress("UNUSED_PARAMETER") value: Any): T {
    throw IllegalStateException("Expected exception to be returned")
}
