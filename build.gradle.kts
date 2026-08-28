plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dependency.analysis)
}

dependencies {
    detektPlugins(libs.detekt.compose.rules)
}

// Same single-place setup as Spotless below: one plugin on the root project, reaching every
// module through source paths instead of being applied per module.
detekt {
    // build-logic is a separate Gradle build, so its own detekt task does not run from here.
    // Adding its sources checks the convention plugins with the same rules as the app code.
    source.setFrom(files("app/src", "data/src", "domain/src", "build-logic/convention/src"))
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

// Checks the dependencies of every module. It finds three things: a dependency we declare but
// never use, a library we use but never declare, and the wrong choice between implementation and
// api. Run `./gradlew buildHealth`.
dependencyAnalysis {
    abi {
        exclusions {
            // A module needs `api` only for types that other modules can see. These two lines say
            // what does not count as visible: classes marked `internal`, and classes that Hilt and
            // Room generate for us.
            ignoreGeneratedCode()
            ignoreInternalPackages()
        }
    }

    issues {
        all {
            // Any problem breaks the build. The default is only a warning.
            onAny { severity("fail") }

            // One kind of problem: our code uses a library that we never declared. A smaller block
            // replaces the severity above, so it is set again here.
            onUsedTransitiveDependencies {
                severity("fail")

                // We never wrote code that uses these two. Only the classes that Room and Hilt
                // generate touch them. Declaring them would describe the code generator, not
                // this app.
                // ignoreGeneratedCode() above does not cover this. It only filters ABI analysis,
                // so it can move a dependency from api to implementation, never from used to
                // unused. "Used transitive" is decided by a raw bytecode scan that reads the
                // generated classes too, so the exclusion has to happen here.
                exclude(
                    "androidx.fragment:fragment",
                    "androidx.sqlite:sqlite",
                )
            }
        }
    }

    structure {
        // A bundle says: for us these artifacts are one library. Example: we declare
        // navigation-compose, and our code uses NavController, which lives in navigation-runtime.
        // Without the bundle the plugin asks us to declare navigation-runtime too.
        // Each bundle below is one group with one version, so its parts always move together.
        // Compose needs a pattern, because it is split over several groups.
        bundle("compose") { include("^androidx\\.compose.*") }
        bundle("activity") { includeGroup("androidx.activity") }
        bundle("lifecycle") { includeGroup("androidx.lifecycle") }
        bundle("navigation") { includeGroup("androidx.navigation") }
        bundle("room") { includeGroup("androidx.room") }
        bundle("dagger") { includeGroup("com.google.dagger") }

        // The group org.jetbrains.kotlinx also holds coroutines, which are a different library.
        // So here we name the two artifacts instead of the group.
        bundle("serialization") {
            includeDependency("org.jetbrains.kotlinx:kotlinx-serialization-json")
            includeDependency("org.jetbrains.kotlinx:kotlinx-serialization-core")
        }
    }
}

// buildHealth is not part of `check` by default, so wire it in. Now `./gradlew check` runs
// Spotless, detekt, Android Lint, the unit tests and the dependency analysis together.
tasks.named("check") { dependsOn(tasks.named("buildHealth")) }
