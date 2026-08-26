plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    id("coinfolio.android.lint")
}

android {
    namespace = "me.prokhorov.coinfolio"
    compileSdk = 36
    compileSdkMinor = 1

    defaultConfig {
        applicationId = "me.prokhorov.coinfolio"
        // Android 8 (API 26 and 27) behaves differently in Compose text input: focus is not
        // cleared, so the keyboard stays open after a save and no public API closes it.
        // The screens are verified from API 28 upward, so that is the floor.
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // The shared Lint setup lives in the coinfolio.android.lint convention plugin.
    // Only one exception here that applies to this module.
    lint {
        // The launcher icon comes later. Drop this line when it lands.
        disable += "MissingApplicationIcon"
    }
}

dependencies {
    // Modules
    @Suppress("AvoidDuplicateDependencies")
    implementation(projects.domain)
    @Suppress("AvoidDuplicateDependencies")
    implementation(projects.data)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)
    debugImplementation(libs.androidx.ui.tooling)

    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Network
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)

    // Room
    implementation(libs.androidx.room.runtime)

    // Logging
    implementation(libs.timber)
}
