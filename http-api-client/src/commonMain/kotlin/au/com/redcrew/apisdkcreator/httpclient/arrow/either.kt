package au.com.redcrew.apisdkcreator.httpclient.arrow

import arrow.core.Either
import arrow.core.computations.either
import arrow.core.flatMap

/**
 * We define a Kleisli operator for the Either Monad to allow use to compose together (left to right)
 * functions that return Either instances. This is a lot nicer that flatMapping all the time, or having to use Monad
 * comprehensions with intermediate variables.
 *
 * For an introduction to Kleisli arrows, see https://blog.ssanj.net/posts/2017-06-07-composing-monadic-functions-with-kleisli-arrows.html
 */
infix fun <A,E,B,C> ((A) -> Either<E, B>).pipe(ff: (B) -> Either<E,C>): (A) -> Either<E,C> =
    { a: A -> this(a).flatMap(ff) }

/**
 * A version of pipe that can compose (left to right) suspending functions together that return an Either monad instance
 *
 * To understand the use of `this` see https://kotlinlang.org/docs/this-expressions.html
 */
infix fun <A, E, B, C> (
    suspend (A) -> Either<E, B>).pipe(ff: suspend (B) -> Either<E, C>
): suspend (A) -> Either<E, C> =
    { a: A -> either { ff(this@pipe(a).bind()).bind() } }

fun <A, B> aLeft(value: A) = Either.Left(value) as Either<A, B>
fun <A, B> aRight(value: B) = Either.Right(value) as Either<A, B>
