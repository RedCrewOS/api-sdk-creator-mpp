package au.com.redcrew.apisdkcreator.httpclient

data class HttpResponse<T>(
    val statusCode: Int,
    val statusMessage: String,

    val headers: HttpHeaders,

    val body: T?
) {
    constructor(statusCode: Int, statusMessage: String, headers: HttpHeaders) :
            this(statusCode, statusMessage, headers, null)

    constructor(statusCode: Int, statusMessage: String) : this(statusCode, statusMessage, emptyMap(), null)

    /**
     * When using the default `copy` the type of the body can't be changed.
     *
     * This method allows a copy where the type of the body can be changed.
     */
    fun <A> copyWithBody(
        statusCode: Int = this.statusCode,
        statusMessage: String = this.statusMessage,
        headers: HttpHeaders = this.headers,
        body: A?
    ): HttpResponse<A> =
        HttpResponse(statusCode, statusMessage, headers, body)
}
