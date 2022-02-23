package au.com.redcrew.apisdkcreator.httpclient.matchers

import arrow.core.*
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult

fun <T> notEqual(expected: T, actual: T): Boolean =
    expected != actual

fun <T> MatcherResult.Companion.test(predicate: (T, T) -> Boolean, expected: T, actual: T): Option<MatcherResult> =
    when (predicate(expected, actual)) {
        true -> MatcherResult.failure(expected, actual).some()
        false -> none()
    }

fun <T> MatcherResult.Companion.failure(expected: T, actual: T): MatcherResult =
    MatcherResult(
        false,
        { "$actual should be $expected" },
        { "$actual should not be $expected" }
    )

fun Matcher.Companion.tests(results: List<Option<MatcherResult>>): MatcherResult =
    results.sequenceOption().fold(
        { MatcherResult(true, { "value matches expected" }, { "value does not match expected" } ) },
        List<MatcherResult>::first
    )

fun Matcher.Companion.tests(vararg results: Option<MatcherResult>): MatcherResult =
    Matcher.tests(results.asList())
