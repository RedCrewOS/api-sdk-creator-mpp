package au.com.redcrew.apisdkcreator.httpclient.okhttp

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.left
import arrow.core.right
import au.com.redcrew.apisdkcreator.httpclient.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.net.URL

private fun toLowerCase(entry: Map.Entry<String, String>) = entry.key.lowercase()

private fun toHttpClientError(message: String?, cause: Any) =
    SdkError(HTTP_CLIENT_ERROR_TYPE, message ?: "", cause)

fun okHttpClient(): HttpClient {
    val client = OkHttpClient()

    return { request: HttpRequest<UnstructuredData> ->
        either {
            val okRequest = buildRequest(request).bind()

            val okResponse: Response = Either.catch(
                { toHttpClientError(it.message, it) },
                { client.newCall(okRequest).execute() }
            ).bind()

            val body = okResponse.body?.let {
                when {
                    it.contentLength() == 0L -> null
                    else -> { UnstructuredData.String(it.string()) }
                }
            }

            HttpResult(
                request,
                HttpResponse(
                    okResponse.code,
                    okResponse.message,
                    okResponse.headers.toMap().mapKeys(::toLowerCase),
                    body
                )
            )
        }
    }
}

private suspend fun buildRequest(request: HttpRequest<UnstructuredData>): Either<SdkError, Request> =
    either {
        val url: URL = buildUrl(request).bind()
        val body: RequestBody? = buildBody(request.method, request.headers["content-type"], request.body).bind()
        val headers = request.headers.filter { entry -> !(entry.key == "content-type" && body == null) }.toHeaders()

        val builder = Request.Builder()
            .method(request.method.name, body)
            .headers(headers)
            .url(url)

        builder.build()
    }

private suspend fun buildUrl(request: HttpRequest<UnstructuredData>): Either<SdkError, URL> =
    either {
        val url: URL = when (request.url) {
            is HttpRequestUrl.URL -> (request.url as HttpRequestUrl.URL).url.right()
            is HttpRequestUrl.String -> URL((request.url as HttpRequestUrl.String).url).right()
            else -> SdkError(ILLEGAL_STATE_ERROR_TYPE, "Unrecognised URL type").left()
        }.bind()

        val path = replacePathParams(url.path, request.pathParams ?: emptyMap()).bind()
        val query = createQueryString(request.queryParams ?: emptyMap())

        URL(url.protocol, url.host, url.port, "${path}${query}")
    }

private fun buildBody(
    method: HttpRequestMethod,
    contentType: String?,
    body: UnstructuredData?
): Either<SdkError, RequestBody?> {
    if (method == HttpRequestMethod.GET || method == HttpRequestMethod.HEAD) {
        return null.right()
    }

    if (body != null && contentType == null) {
        return SdkError(ILLEGAL_ARGUMENT_ERROR_TYPE, "Missing content-type").left()
    }

    val data: Either<SdkError, String?> =
        when (body) {
            null -> null.right()
            is UnstructuredData.String -> body.data.right()
            else -> SdkError(ILLEGAL_STATE_ERROR_TYPE, "Unrecognised UnstructuredData type").left()
        }

    return data.map { it?.toRequestBody(contentType!!.toMediaType()) }
}
