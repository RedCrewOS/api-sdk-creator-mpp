package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.compose
import arrow.core.curried

val add = { a: Int, b: Int -> a + b }
val multiply = { a: Int, b: Int -> a * b }

val add3 = add.curried()(3)
val multiply2 = multiply.curried()(2)

val m2a3 = add3 compose multiply2

