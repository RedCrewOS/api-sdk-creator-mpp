package au.com.redcrew.apisdkcreator.httpclient

import java.lang.IllegalArgumentException
import java.net.URLEncoder

fun replacePathParams(path: String, params: Map<String, String>): String =
    path.split("/")
        .map { segment ->
            if (segment.startsWith(":")) {
                val value = params[segment.substring(1)]

                return@map value ?: run { throw IllegalArgumentException("No value provided for '${segment}'") }
            }

            return@map segment
        }
        .joinToString("/")

fun createQueryString(params: Map<String, String>): String =
    when {
        params.isNotEmpty() -> "?${params.map(::mapEntryToPair).joinToString("&")}"
        else -> ""
    }

private fun mapEntryToPair(entry: Map.Entry<String, String>): String =
    "${entry.key}=${URLEncoder.encode(entry.value, "utf-8")}"
