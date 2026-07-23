plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "me.prokhorov.coinfolio.data"
    compileSdk = 36
    compileSdkMinor = 1

    defaultConfig {
        minSdk = 26
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(projects.domain)
}
