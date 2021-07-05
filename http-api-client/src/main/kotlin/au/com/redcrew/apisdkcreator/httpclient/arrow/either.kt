package au.com.redcrew.apisdkcreator.httpclient.arrow

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.flatMap

infix fun <A,E,B,C> ((A) -> Either<E, B>).pipeK(ff: (B) -> Either<E,C>): (A) -> Either<E,C> =
    { a: A -> this(a).flatMap(ff) }

suspend infix fun <A, E, B, C> (suspend (A) -> Either<E, B>).pipeK(ff: suspend (B) -> Either<E, C>): suspend (A) -> Either<E, C> =
    { a: A -> either { ff(this@pipeK(a).bind()).bind() } }
