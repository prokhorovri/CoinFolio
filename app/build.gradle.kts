plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    alias(libs.plugins.dependency.analysis)
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    testOptions {
        unitTests {
            // Robolectric reads the merged manifest and resources of the module. Without this
            // it cannot start, and the test fails before the first line of the test body.
            isIncludeAndroidResources = true
        }

        // Gradle starts this emulator, runs the tests, then closes it. No AVD needed on the
        // machine, and CI uses the same device.
        // Run: ./gradlew :app:pixel6Api34DebugAndroidTest
        managedDevices {
            localDevices {
                create("pixel6Api34") {
                    device = "Pixel 6"
                    apiLevel = 34
                    // ATD is a small image for tests: no browser, no Play Store. It boots faster.
                    systemImageSource = "aosp-atd"
                }
            }
        }
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

    // AndroidX
    implementation(libs.androidx.annotation)

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

    // DI
    implementation(libs.javax.inject)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.lifecycle.viewmodel.compose)
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

    // Testing
    // The fakes and model builders come from :domain.
    testImplementation(testFixtures(projects.domain))
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    // CoinDetailViewModel reads its coin id with SavedStateHandle.toRoute, which builds a Bundle.
    testImplementation(libs.robolectric)

    // UI tests build the ViewModel by hand, so they need the same fakes as the unit tests.
    androidTestImplementation(testFixtures(projects.domain))
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    // The tests never use this class in code. Only testInstrumentationRunner names it.
    androidTestRuntimeOnly(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.truth)
    // Adds an empty activity to the debug app. A test shows one composable inside it.
    debugImplementation(libs.androidx.ui.test.manifest)
}
