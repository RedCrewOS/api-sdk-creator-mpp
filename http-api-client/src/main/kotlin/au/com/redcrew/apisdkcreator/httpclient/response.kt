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
}
