plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
}

dependencies {
    detektPlugins(libs.detekt.compose.rules)
}

// Same single-place setup as Spotless below: one plugin on the root project, reaching every
// module through source paths instead of being applied per module.
detekt {
    source.setFrom(files("app/src", "data/src", "domain/src"))
    config.setFrom(files("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    parallel = true
}

// Formatting is configured once here and reaches every module through file globs.
// Applying the plugin to each module from the root would be cross-project configuration,
// which Gradle's isolated projects mode forbids.
//
// One caveat: ktlint parses .editorconfig once per Gradle daemon and caches the result, so an
// edit to .editorconfig does not reach ktlint until the daemon restarts. Worse, a run that used
// the stale cache is recorded as up to date, so a later run can replay the stale verdict. After
// changing .editorconfig, run `./gradlew --stop`, then add `--rerun-tasks` to the next Spotless
// task. Upstream issue: https://github.com/diffplug/spotless/issues/1924
spotless {
    val ktlintVersion = libs.versions.ktlint.get()

    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**")
        ktlint(ktlintVersion)
    }

    kotlinGradle {
        target("**/*.kts")
        targetExclude("**/build/**")
        ktlint(ktlintVersion)
    }
}
