plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Coroutines — `api` because Flow is part of this module's public API, not an internal detail.
    api(libs.kotlinx.coroutines.core)

    // DI (constructor injection only — Hilt modules live in :app)
    implementation(libs.javax.inject)
}
