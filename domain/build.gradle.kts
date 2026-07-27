plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // DI (constructor injection only — Hilt modules live in :app)
    implementation(libs.javax.inject)
}
