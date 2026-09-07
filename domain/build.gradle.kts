plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dependency.analysis)
    // Publishes the fakes in src/testFixtures, so every module's tests use the same ones.
    // It does not work with KMP. If :domain moves there, the fakes need a :domain-testing module.
    `java-test-fixtures`
}

dependencies {
    // Coroutines. `api` because Flow is part of this module's public API, not an internal detail.
    api(libs.kotlinx.coroutines.core)

    // DI (constructor injection only; Hilt modules live in :app). `api` because `@Inject` is
    // written on public constructors, and Hilt in :app reads it.
    api(libs.javax.inject)

    // Test fixtures. The fakes return Flow, so it is part of their public API.
    testFixturesApi(libs.kotlinx.coroutines.core)

    // Testing. Pure JVM.
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}
