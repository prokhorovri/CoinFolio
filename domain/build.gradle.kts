plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.dependency.analysis)
}

dependencies {
    // Coroutines. `api` because Flow is part of this module's public API, not an internal detail.
    api(libs.kotlinx.coroutines.core)

    // DI (constructor injection only; Hilt modules live in :app). `api` because `@Inject` is
    // written on public constructors, and Hilt in :app reads it.
    api(libs.javax.inject)

    // Testing. Pure JVM.
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
}
