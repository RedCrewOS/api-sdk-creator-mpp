package au.com.redcrew.apisdkcreator.httpclient

// isSuccessfulResponse :: HttpResponse -> Boolean
fun isSuccessfulResponse(response: HttpResponse<*>): Boolean = response.statusCode in 200..299

// isSuccessfulResult :: HttpResult -> Boolean
fun <T> isSuccessfulResult(result: HttpResult<*, T>) = isSuccessfulResponse(getHttpResponse(result))
