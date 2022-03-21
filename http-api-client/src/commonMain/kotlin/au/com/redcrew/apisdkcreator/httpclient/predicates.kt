package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Option
import arrow.core.andThen
import arrow.core.getOrElse

// split :: String -> String -> List String
private fun split(delimiter: String): (String) -> List<String> = { str -> str.split(delimiter) }

// takeFirst :: List a -> a
private fun <T> takeFirst(lst: List<T>): T = lst.first()

// trim :: String -> String
private fun trim(str: String) = str.trim()

// isSame :: a -> b -> Boolean
private fun isSame(a: Any): (b: Any) -> Boolean = { b -> a == b }

// isSuccessfulResponse :: HttpResponse -> Boolean
fun isSuccessfulResponse(response: HttpResponse<*>): Boolean = response.statusCode in 200..299

// isSuccessfulResult :: HttpResult -> Boolean
fun <T> isSuccessfulResult(result: HttpResult<*, T>) = isSuccessfulResponse(getHttpResponse(result))

// hasContentType :: String -> HttpResult -> Boolean
fun hasContentType(contentType: String, response: HttpResponse<*>): Boolean =
    Option.fromNullable(response.headers["content-type"])
        .map(split(";") andThen ::takeFirst andThen ::trim andThen isSame(contentType))
        .getOrElse { false }
