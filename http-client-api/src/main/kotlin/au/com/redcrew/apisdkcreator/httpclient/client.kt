package au.com.redcrew.apisdkcreator.httpclient

interface HttpResult<Request : Any, Response : Any> {
    val request: HttpRequest<Request>
    val response: HttpResponse<Response>
}
