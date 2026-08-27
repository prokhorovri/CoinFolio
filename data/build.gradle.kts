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
    implementation(projects.domain)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Network
    implementation(libs.retrofit.core)
    implementation(libs.kotlinx.serialization.json)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DI (constructor injection only; Hilt modules live in :app)
    implementation(libs.javax.inject)

    // Logging
    implementation(libs.timber)
}
