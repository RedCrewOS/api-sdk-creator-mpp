#  http-api-client

A [Kotlin MPP](https://kotlinlang.org/docs/mpp-get-started.html) library that can be used as a foundation to build clients (SDKs) for HTTP based APIs.

| :memo: | This library is still in active development and may break compatibility in future releases |
|--------|:------------------------------------------------------------------------------------------|

## Usage

Include in your dependencies

### JVM (Android)

```groovy
dependencies {
    implementation "au.com.redcrew.apisdkcreator:http-api-client:0.1-SNAPSHOT"
}
```

### iOS

TODO

## Motivation

To understand the motivation behind building this library, and the design decisions, see
[the original motivation doc](https://github.com/RedCrewOS/api-sdk-creator-js/blob/main/http-api-client/docs/motivation.md)

This is a Kotlin port of the JS library. A lot of the type and function names have been carried over, however there are
some important implementation differences.

### Arrow

In order to have the ability to return errors and data from functions, and having access to ADTs in Kotlin, a library
called [Arrow](https://arrow-kt.io/docs/core/) is used. The Arrow docs have a good intro to [Monads](https://arrow-kt.io/docs/patterns/monads/)
and [error handling](https://arrow-kt.io/docs/patterns/error_handling/) with Arrow.

#### Threading model

Arrow and thus this library uses [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-basics.html) to achieve
concurrency. Because of that, most of the functions in this library are [suspendable](https://kotlinlang.org/docs/composing-suspending-functions.html)
which allows non-blocking functions to be composed together. That means that users of the functions in this library
will either have to be suspending functions, or launch a new Coroutine in order to use the functions.

SDK developers will have to decide how to allow client applications to [decide the Dispatcher](https://kotlinlang.org/docs/coroutine-context-and-dispatchers.html#unconfined-vs-confined-dispatcher) that the Coroutines use in order to be executed on the correct thread.

This is very important in UI applications (eg: Android apps) where IO work should be performed on the 
[IO Dispatcher](https://developer.android.com/kotlin/coroutines) so that a background thread is used, to prevent the 
[UI being blocked on IO](https://developer.android.com/kotlin/coroutines/coroutines-best-practices#main-safe).

It's worth noting that because the functions in this library never throw, exceptions in Coroutines can't be
[silently dropped](https://medium.com/androiddevelopers/coroutines-on-android-part-ii-getting-started-3bff117176dd)
and will always be returned in an `Either.Left` However if SDK developers choose the throw an exception, application
developers will still have to make sure that they can handle exceptions thrown within Coroutines properly.

#### Asynchronous task modelling

While Crocks (in Javascript) has the `Async` monad to represent chainable units of concurrent work, Arrow uses Kotlin
`suspend` functions [instead of a Monad](https://arrow-kt.io/docs/effects/io/).  As a result, ideas from the JS version 
of api-sdk-creator had to be reworked to fit with this paradigm.

One of the consequences of using `suspend` functions is that the orthodox Monad methods of `map` `chain`/`flatMap` etc
can't be used. Thankfully Arrow offers [monad comprehensions](https://arrow-kt.io/docs/patterns/monad_comprehensions/)
which allow us to write code that composes monads together (like Crock's `chain`ing). While the code reads like imperative
code, monad comprehensions will exit early if the result of `bind`ing a result is not mappable, etc. This preserves the
monad semantics we want (`Option`, `Either`, etc) in code that has a lot less boilerplate, which is nice in a statically
typed language.

#### Invoke operator overloading

Because Kotlin is a statically typed language it is does not support currying lambdas with generic type parameters where
the generic type is not part of the entire function signature. For example, 
`fun gsonUnmarshaller(gson: Gson): (KClass<T>) -> Unmarshaller<T>` does not compile.

We overcome this by using [invoke operator overloading](ttps://kotlinlang.org/docs/operator-overloading.html#invoke-operator).
See the docs on `GenericTypeCurriedFunction` for more details on the implementation.

### References

- [FP with Kotlin blog posts](https://mattmoore.io/blog/category/Functional+Programming)
- [A series on using Either for error handling](http://oneeyedmen.com/failure-is-not-an-option-part-1.html)
- [Using Coroutines with Android](https://medium.com/androiddevelopers/coroutines-on-android-part-i-getting-the-background-3e0e54d20bb)

## Docs

// TODO

## Examples

See the [example](../example) for a documented example of how to use this library to compose an API call.
