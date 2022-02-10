package au.com.redcrew.apisdkcreator.httpclient.data

import au.com.redcrew.apisdkcreator.httpclient.HttpHeaders
import au.com.redcrew.apisdkcreator.httpclient.HttpRequest
import au.com.redcrew.apisdkcreator.httpclient.HttpRequestMethod
import au.com.redcrew.apisdkcreator.httpclient.HttpRequestUrl

fun <T : Any> aHttpRequest() = HttpRequestBuilder<T>()

class HttpRequestBuilder<T : Any> {
    private var method = HttpRequestMethod.GET
    private var url: HttpRequestUrl = HttpRequestUrl.String("http://localhost:3000")
    private val headers = mutableMapOf<String, String>()
    private val pathParams = mutableMapOf<String, String>()
    private val queryParams = mutableMapOf<String, String>()
    private var body: T? = null

    fun withMethod(method: HttpRequestMethod): HttpRequestBuilder<T> {
        this.method = method

        return this
    }

    fun withUrl(url: HttpRequestUrl): HttpRequestBuilder<T> {
        this.url = url

        return this
    }

    fun withHeaders(headers: HttpHeaders): HttpRequestBuilder<T> {
        this.headers.clear()
        this.headers.putAll(headers)

        return this
    }

    fun addHeader(name: String, value: String): HttpRequestBuilder<T> {
        this.headers[name] = value

        return this
    }

    fun withPathParams(params: Map<String, String>): HttpRequestBuilder<T> {
        this.pathParams.clear()
        this.pathParams.putAll(params)

        return this
    }

    fun addPathParam(name: String, value: String): HttpRequestBuilder<T> {
        this.pathParams[name] = value

        return this
    }

    fun withQueryParms(params: Map<String, String>): HttpRequestBuilder<T> {
        this.queryParams.clear()
        this.queryParams.putAll(params)

        return this
    }

    fun addQueryParam(name: String, value: String): HttpRequestBuilder<T> {
        this.queryParams[name] = value

        return this
    }

    fun withBody(body: T): HttpRequestBuilder<T> {
        this.body = body

        return this
    }

    fun build(): HttpRequest<T> = HttpRequest(method, url, headers, pathParams, queryParams, body)
}
