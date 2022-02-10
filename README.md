# api-sdk-creator-mpp

Contains [Kotlin MPP](https://kotlinlang.org/docs/mpp-get-started.html) libraries to aid SDK developers in the creation of API client SDKs.

See the individual library READMEs for more details. The best starting place is with the
[http-api-client README](http-api-client/README.md)

| :memo: | This library is still in active development and may break compatibility in future releases |
|--------|:------------------------------------------------------------------------------------------|

# Supported Platforms
- [JVM (Android)](https://github.com/RedCrewOS/api-sdk-creator-jvm/)
- iOS (IN PROGRESS)

# Development

The repository is managed by [Gradle](https://gradle.org/) with each library being a Gradle module. Each module has
its own version and can be published and consumed independently.

## Building

```shell
$ ./gradlew build
```

## Testing

```shell
$ ./grawdle test
$ cd <project> && ../gradlew test
```

## Publishing

TODO
