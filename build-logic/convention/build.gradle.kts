plugins {
    `kotlin-dsl`
}

group = "me.prokhorov.coinfolio.buildlogic"

dependencies {
    // compileOnly, not implementation: this module only needs the DSL types at compile time.
    // At run time the Android plugin is already there, because the build applies it anyway.
    compileOnly(libs.android.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidLint") {
            id = "coinfolio.android.lint"
            implementationClass = "AndroidLintConventionPlugin"
        }
    }
}
