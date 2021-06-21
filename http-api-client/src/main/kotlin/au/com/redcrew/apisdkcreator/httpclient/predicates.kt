package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.compose

// isSuccessfulResponse :: HttpResponse -> Boolean
val isSuccessfulResponse = { response: HttpResponse<*> -> response.statusCode in 200..299 }

// isSuccessfulResult :: HttpResult -> Boolean
val isSuccessfulResult = isSuccessfulResponse compose  getHttpResponse
