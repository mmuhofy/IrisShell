package iris

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

/**
 * Convention plugin applied to the root `:app` application module.
 *
 * Sets up:
 *  - AGP `com.android.application`
 *  - Kotlin Android
 *  - Java 17 toolchain + Kotlin jvmTarget 17
 *  - Common Android SDK levels (minSdk 26, targetSdk 36, compileSdk 36)
 *  - Test fixtures + unit test support
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.android")
            }

            tasks.withType(KotlinJvmCompile::class.java).configureEach {
                compilerOptions {
                    jvmTarget.set(JvmTarget.fromVersion(IrisBuildConfig.KOTLIN_JVM_TARGET))
                    allWarningsAsErrors.set(false)
                }
            }

            extensions.configure<ApplicationExtension> {
                compileSdk = IrisBuildConfig.COMPILE_SDK

                defaultConfig {
                    applicationId = IrisBuildConfig.APPLICATION_ID
                    minSdk = IrisBuildConfig.MIN_SDK
                    targetSdk = IrisBuildConfig.TARGET_SDK
                    versionCode = 1
                    versionName = "0.1.0"
                    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
                    vectorDrawables.useSupportLibrary = true
                }

                compileOptions {
                    sourceCompatibility = IrisBuildConfig.JAVA_VERSION
                    targetCompatibility = IrisBuildConfig.JAVA_VERSION
                }

                buildTypes {
                    getByName("debug") {
                        isMinifyEnabled = false
                        applicationIdSuffix = ".debug"
                    }
                    getByName("release") {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                    }
                }

                packaging {
                    resources {
                        excludes += "/META-INF/{AL2.0,LGPL2.1}"
                    }
                }
            }
        }
    }
}
