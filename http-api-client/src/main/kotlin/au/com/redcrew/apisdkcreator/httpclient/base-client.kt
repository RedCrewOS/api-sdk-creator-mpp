package au.com.redcrew.apisdkcreator.httpclient

import java.lang.IllegalArgumentException
import java.net.URLEncoder

val replacePathParams = { path: String, params: Map<String, String> ->
    path.split("/")
        .map { segment ->
            if (segment.startsWith(":")) {
                val value = params[segment.substring(1)]

                return@map value ?: run { throw IllegalArgumentException("No value provided for '${segment}'") }
            }

            return@map segment
        }
        .joinToString("/")
}

val createQueryString = { params: Map<String, String> ->
    when {
        params.isNotEmpty() -> "?${params.map(mapEntryToPair).joinToString("&")}"
        else -> ""
    }
}

private val mapEntryToPair: (Map.Entry<String, String>) -> String =
    { entry -> "${entry.key}=${URLEncoder.encode(entry.value, "utf-8")}" }
