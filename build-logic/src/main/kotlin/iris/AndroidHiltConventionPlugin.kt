package iris

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * Applies Hilt + KSP for any Android module that participates in DI.
 *
 * Per AGENT.md §133: Hilt modules live in `di/` (no logic whatsoever), but
 * the actual @Inject / @Module / @HiltViewModel annotations are spread across
 * modules — so EVERY Android module that needs to participate in Hilt's
 * graph applies this plugin.
 *
 * Hilt is driven by **kapt** rather than KSP. Why: with Hilt's KSP2
 * processor we hit "BindingMethodProcessingStep was unable to process ...
 * because 'X' could not be resolved" errors whenever a new domain interface
 * (e.g. SetTerminalFontSizeUseCase) was introduced. Kapt's symbol resolver
 * sees the same JAR outputs but is less aggressive about caching them, so
 * freshly added types resolve reliably. Room stays on KSP because its KSP
 * processor is stable and produces cleaner code-gen than kapt.
 */
class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("org.jetbrains.kotlin.kapt")
                apply("com.google.devtools.ksp")
                apply("com.google.dagger.hilt.android")
            }

            dependencies {
                add("implementation", libs().findLibrary("hilt.android").get())
                // Hilt's annotation processor — kapt path.
                add("kapt", libs().findLibrary("hilt.compiler").get())
                // Hilt + WorkManager integration (cron, agent watch)
                add("implementation", libs().findLibrary("hilt.work").get())
                add("kapt", libs().findLibrary("hilt.work.compiler").get())
                add("implementation", libs().findLibrary("hilt.navigation.compose").get())
            }
        }
    }
}
