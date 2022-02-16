package au.com.redcrew.apisdkcreator.httpclient.net

import java.net.URLEncoder

actual fun urlEncoder(value: String, enc: String): String =
    URLEncoder.encode(value, enc)
