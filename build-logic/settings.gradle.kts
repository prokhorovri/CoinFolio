dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }

    // An included build does not inherit the version catalog of the main build, so it is
    // declared again here, pointing at the same file. One catalog, two builds.
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
