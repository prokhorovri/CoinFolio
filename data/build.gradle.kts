plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.dependency.analysis)
    id("coinfolio.android.lint")
}

android {
    namespace = "me.prokhorov.coinfolio.data"
    compileSdk = 36
    compileSdkMinor = 1

    defaultConfig {
        minSdk = 28
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    // Most lines here are `api`, not `implementation`. The Hilt modules live in :app and build
    // CoinApi, CoinFolioDatabase and CoinRepositoryImpl. So these classes are public, and :app
    // also sees the types they use in constructors and return values.
    api(projects.domain)

    // Coroutines
    api(libs.kotlinx.coroutines.core)

    // Network
    api(libs.retrofit.core)
    api(libs.okhttp.core)
    api(libs.kotlinx.serialization.json)

    // Room
    api(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)

    // DI (constructor injection only; Hilt modules live in :app)
    api(libs.javax.inject)

    // Logging
    implementation(libs.timber)
}
