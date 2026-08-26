enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    // Shared Gradle setup lives here as convention plugins. See build-logic/convention.
    includeBuild("build-logic")

    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "CoinFolio"
include(":app")
include(":domain")
include(":data")
