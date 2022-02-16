package au.com.redcrew.apisdkcreator.httpclient.net

import platform.Foundation.NSCharacterSet.Companion.alphanumericCharacterSet
import platform.Foundation.NSString
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters

actual fun urlEncoder(value: String, enc: String): String =
    (value as NSString).stringByAddingPercentEncodingWithAllowedCharacters(alphanumericCharacterSet).toString()
