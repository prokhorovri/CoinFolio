dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }

    // An included build does not inherit the version catalog of the main build. So it is
    // declared again here, and it points at the same file.
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"
include(":convention")
