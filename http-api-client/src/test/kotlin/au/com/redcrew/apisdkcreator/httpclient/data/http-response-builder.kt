package au.com.redcrew.apisdkcreator.httpclient.data

import au.com.redcrew.apisdkcreator.httpclient.HttpHeaders
import au.com.redcrew.apisdkcreator.httpclient.HttpResponse

fun <T: Any> aHttpResponse() = HttpResponseBuilder<T>()

class HttpResponseBuilder<T: Any> {
    private var statusCode = 200
    private var statusMessage = "OK"
    private val headers = mutableMapOf<String, String>()
    private var body: T? = null

    fun withStatusCode(code: Int): HttpResponseBuilder<T> {
        this.statusCode = code

        return this
    }

    fun withStatusMessage(message: String): HttpResponseBuilder<T> {
        this.statusMessage = message

        return this
    }

    fun withHeaders(headers: HttpHeaders): HttpResponseBuilder<T> {
        this.headers.clear()
        this.headers.putAll(headers)

        return this
    }

    fun addHeader(name: String, value: String): HttpResponseBuilder<T> {
        this.headers[name] = value

        return this
    }

    fun withBody(body: T): HttpResponseBuilder<T> {
        this.body = body

        return this
    }

    fun build(): HttpResponse<T> = HttpResponse(statusCode, statusMessage, headers, body)
}
