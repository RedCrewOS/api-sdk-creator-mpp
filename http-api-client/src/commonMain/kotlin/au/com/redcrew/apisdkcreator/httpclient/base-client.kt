package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.*
import au.com.redcrew.apisdkcreator.httpclient.net.urlEncoder

/**
 * Helper function to replace path parameters (slugs) in a URI
 *
 * @param path A string with slugs
 * @param params A map of slugs to values
 * @return The path with the params replaced.
 */
fun replacePathParams(path: String, params: Map<String, String>): Either<SdkError, String> =
    path.split("/")
        .map { segment: String ->
            if (segment.startsWith(":")) {
                val value = Option.fromNullable(params[segment.substring(1)])

                return@map value.fold(
                    { SdkError(ILLEGAL_ARGUMENT_ERROR_TYPE, "No value provided for '${segment}'").left() },
                    { it.right() }
                )
            }

            return@map segment.right()
        }
        .sequenceEither()
        .map { it.joinToString("/") }

/**
 * Creates a query string from an object.
 *
 * @param params
 * @return The query string
 */
fun createQueryString(params: Map<String, String>): String =
    when {
        params.isNotEmpty() -> "?${params.map(::mapEntryToPair).joinToString("&")}"
        else -> ""
    }

private fun mapEntryToPair(entry: Map.Entry<String, String>): String =
    "${entry.key}=${urlEncoder(entry.value, "utf-8")}"
