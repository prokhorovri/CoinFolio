import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import com.android.build.api.dsl.Lint
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * One Android Lint setup for every Android module.
 *
 * A `subprojects { }` block in the root build file would do the same. But that is cross-project
 * configuration, and Gradle's isolated projects mode forbids it. A convention plugin is applied
 * by the module itself, so each module still configures only itself.
 *
 * Applied by `:app` and `:data`. `:domain` is a pure Kotlin module and stays free of Android
 * plugins, so Lint does not reach it. detekt covers that module instead.
 */
class AndroidLintConventionPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        with(target) {
            pluginManager.withPlugin("com.android.application") {
                extensions.configure<ApplicationExtension> { lint { configure() } }
            }

            pluginManager.withPlugin("com.android.library") {
                extensions.configure<LibraryExtension> { lint { configure() } }
            }
        }
    }
}

private fun Lint.configure() {
    // A warning fails the build, just like Spotless and detekt do. By default Lint reports
    // almost everything as a warning. So `abortOnError` alone would let most findings pass.
    warningsAsErrors = true

    // Every Android module runs its own Lint task, so :app does not need to check :data again.
    // With this on, Lint would also warn about :domain on every run. It cannot check a module
    // without an Android plugin.
    checkDependencies = false

    disable += setOf(
        // These four only say that a newer version exists. Any new library release would then
        // break the build. Nothing in this repo would have changed. That is not a good reason
        // to break it. Updating versions is Dependabot's job.
        "AndroidGradlePluginVersion",
        "GradleDependency",
        "NewerVersionAvailable",
        "OldTargetApi",
    )
}
