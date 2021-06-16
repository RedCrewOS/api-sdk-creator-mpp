package au.com.redcrew.apisdkcreator.httpclient

import arrow.core.Either
import arrow.core.compose
import arrow.core.computations.either
import arrow.core.curried

fun add(a: Int, b: Int) = a + b
fun multiply(a: Int, b: Int) = a * b

/*val add = { a: Int, b: Int -> a + b }
val multiply = { a: Int, b: Int -> a * b }*/

val add3 = ::add.curried()(3)
val multiply2 = ::multiply.curried()(2)

val m2a3 = add3 compose multiply2

val susAdd: suspend (Int, Int) -> Int = { a, b -> add(a, b) }
val susAdd3 = susAdd.curried()(3)

val susMultiply: suspend (Int, Int) -> Int = { a, b -> multiply(a, b) }
val susMultiply2 = susMultiply.curried()(2)

val susM2A3: suspend (Int) -> Int = { a ->
    susAdd3(susMultiply2(a))
}

val parseInt: (value: String) -> Either<NumberFormatException, Int> = {
    try {
        Either.Right(Integer.parseInt(it))
    }
    catch(e: NumberFormatException) {
        Either.Left(e)
    }
}

val sumStrings: suspend (List<String>) -> Either<NumberFormatException, Int> = { values ->
    values.fold<String, Either<NumberFormatException, Int>>(Either.Right(0)) { result, value ->
        either {
           result.bind() + parseInt(value).bind()
        }
    }
}
