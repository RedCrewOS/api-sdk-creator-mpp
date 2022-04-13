# api-sdk-creator-mpp

Contains [Kotlin MPP](https://kotlinlang.org/docs/mpp-get-started.html) libraries to aid SDK developers in the creation of API client SDKs.

See the individual library READMEs for more details. The best starting place is with the
[http-api-client README](http-api-client/README.md)

| :memo: | This library is still in active development and may break compatibility in future releases |
|--------|:------------------------------------------------------------------------------------------|

# Supported Platforms
- [JVM (Android)](https://github.com/RedCrewOS/api-sdk-creator-jvm/)
- iOS (VERY EXPERIMENTAL)

# Development

The repository is managed by [Gradle](https://gradle.org/) with each library being a Gradle module. Each module has
its own version and can be published and consumed independently.

## Building

```shell
$ ./gradlew build
```

## Testing

```shell
$ ./gradlew jvmTest iosX64Test
```

## Publishing & Using

### JVM

JARs are available via [Maven Central](https://mvnrepository.com/).

Previous versions are available via JitPack however JitPack can't [publish MPP](https://github.com/jitpack/jitpack.io/issues/3853)
builds.

```groovy
dependencies {
  implementation "au.com.redcrew.apisdkcreator:http-api-client:0.5.4"
}
```

### iOS

TODO

## Companion repositories

Functionality that is often desired in an API pipeline is platform specific, for example, HTTP clients or JSON parsers. While modules in this repo abstracts platform specifics away, in a real application there needs to be an implementation of the abstraction.

To help API SDK developers, companion repos are available with modules that provide specific implementations of abstractions that can suit application requirements.

- [api-sdk-creator-jvm](https://github.com/RedCrewOS/api-sdk-creator-jvm)
- iOS - TODO

# Docs & examples

- [📘 http-api-client reference docs](/http-api-cleint/docs/sdk/index.html)


- [api-sdk-creator-jvm](https://github.com/RedCrewOS/api-sdk-creator-jvm) - has an `examples` module.
- iOS - TODO
