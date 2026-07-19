package iris

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Applies the Compose Compiler Kotlin plugin (`org.jetbrains.kotlin.plugin.compose`)
 * and wires Compose BOM-managed dependencies for any Android module that needs
 * Compose (app, ui, design-system, and any future widgets module).
 *
 * Other modules (data, agent, terminal, ssh, domain, core) MUST NOT apply this
 * plugin — see AGENT.md §143 ("terminal/ is isolated — never imported directly by ui/").
 */
class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            // Compose BOM + core Compose dependencies for both application
            // and library Android extensions (CommonExtension is the parent).
            extensions.getByType<CommonExtension<*, *, *, *, *, *>>().apply {
                buildFeatures {
                    compose = true
                }
            }

            dependencies {
                val bom = platform(libs().findLibrary("compose.bom").get())
                add("implementation", bom)
                add("androidTestImplementation", bom)

                add("implementation", libs().findLibrary("compose.ui").get())
                add("implementation", libs().findLibrary("compose.ui.graphics").get())
                add("implementation", libs().findLibrary("compose.ui.tooling.preview").get())
                add("implementation", libs().findLibrary("compose.material3").get())
                add("implementation", libs().findLibrary("compose.material.icons.extended").get())
                add("implementation", libs().findLibrary("compose.foundation").get())
                add("implementation", libs().findLibrary("compose.runtime").get())
                add("debugImplementation", libs().findLibrary("compose.ui.tooling").get())
                add("debugImplementation", libs().findLibrary("compose.ui.test.manifest").get())
                add("androidTestImplementation", libs().findLibrary("compose.ui.test.junit4").get())
            }
        }
    }
}
