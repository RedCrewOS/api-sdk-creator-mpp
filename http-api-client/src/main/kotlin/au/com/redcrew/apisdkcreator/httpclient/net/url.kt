package au.com.redcrew.apisdkcreator.httpclient.net

import java.net.URLEncoder

fun urlEncoder(value: String, enc: String): String =
    URLEncoder.encode(value, enc)
