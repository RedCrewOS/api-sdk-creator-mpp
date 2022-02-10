package au.com.redcrew.apisdkcreator.httpclient.lang

import arrow.core.Either
import au.com.redcrew.apisdkcreator.httpclient.SdkError

expect fun parseInt(value: String): Either<SdkError, Int>
