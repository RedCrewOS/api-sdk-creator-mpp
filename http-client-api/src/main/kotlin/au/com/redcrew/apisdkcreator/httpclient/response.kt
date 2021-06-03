package au.com.redcrew.apisdkcreator.httpclient

data class HttpResponse<T : Any>(
    val statusCode: Int,
    val statusMessage: String,

    val headers: HttpHeaders,

    val body: T?
)
