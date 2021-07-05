package au.com.redcrew.apisdkcreator.httpclient.okhttp

import arrow.core.Either
import au.com.redcrew.apisdkcreator.httpclient.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.net.URL

val toLowerCase = { entry: Map.Entry<String, String> -> entry.key.lowercase() }

fun okHttpClient(): HttpClient {
    val client = OkHttpClient()

    return { request: HttpRequest<UnstructuredData> ->
        try {
            val okRequest = buildRequest(request)
            val okResponse: Response = client.newCall(okRequest).execute()
            val body = okResponse.body?.let {
                when {
                    it.contentLength() == 0L -> null
                    else -> { UnstructuredData.String(it.string()) }
                }
            }

            Either.Right(
                HttpResult(
                    request,
                    HttpResponse(
                        okResponse.code,
                        okResponse.message,
                        okResponse.headers.toMap().mapKeys(toLowerCase),
                        body
                    )
                )
            )
        }
        catch (e: Exception) {
            Either.Left(e)
        }
    }
}

private fun buildRequest(request: HttpRequest<UnstructuredData>): Request {
    val url: URL = buildUrl(request)
    val body: RequestBody? = buildBody(request.method, request.headers["content-type"], request.body)
    val headers = request.headers.filter { entry -> !(entry.key == "content-type" && body == null) }.toHeaders()

    val builder = Request.Builder()
        .method(request.method.name, body)
        .headers(headers)
        .url(url)

    return builder.build()
}

private fun buildUrl(request: HttpRequest<UnstructuredData>): URL {
    val url: URL = when (request.url) {
        is HttpRequestUrl.URL -> (request.url as HttpRequestUrl.URL).url
        is HttpRequestUrl.String -> URL((request.url as HttpRequestUrl.String).url)
        else -> throw IllegalArgumentException()
    }

    val path = replacePathParams(url.path, request.pathParams ?: emptyMap())
    val query = createQueryString(request.queryParams ?: emptyMap())

    return URL(url.protocol, url.host, url.port, "${path}${query}")
}

private fun buildBody(method: HttpRequestMethod, contentType: String?, body: UnstructuredData?): RequestBody? {
    if (method == HttpRequestMethod.GET || method == HttpRequestMethod.HEAD) {
        return null
    }

    if (body != null && contentType == null) {
        throw IllegalArgumentException("Missing content-type")
    }

    val data: String? = body?.let {
        when (it) {
            is UnstructuredData.String -> (body as UnstructuredData.String).data
            else -> throw IllegalArgumentException()
        }
    }

    return data?.toRequestBody(contentType!!.toMediaType())
}
