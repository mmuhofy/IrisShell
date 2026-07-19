// ui/ — all Compose screens: Terminal, Sessions, SSH Manager, Settings, HUD, etc.
//
// Per AGENT.md §110-111: "Compose screens, components, ViewModels. No direct
// data/agent/terminal access."
// Per AGENT.md §139: "ui/ never imports from data/, agent/, terminal/, or ssh/ directly"
// Per AGENT.md §145-147: ViewModels expose StateFlow<UiState>; UI collects
// with collectAsStateWithLifecycle().

plugins {
    alias(libs.plugins.iris.android.library)
    alias(libs.plugins.iris.android.compose)
    alias(libs.plugins.iris.android.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.iris.irisshell.ui"

    defaultConfig {
        // Coil needs network security config for theme store previews — fine on Android.
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    // ui sees ONLY the domain interfaces + design-system — it does NOT see data/,
    // agent/, terminal/, or ssh/ per AGENT.md §139.
    api(project(":domain"))
    implementation(project(":core"))
    implementation(project(":design-system"))

    // AndroidX Lifecycle + ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    // Hilt + ViewModel integration
    implementation(libs.hilt.navigation.compose)

    // Coil — image loading for theme store previews (MEMORYBANK.md §58)
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Lottie — onboarding animations (MEMORYBANK.md §59)
    implementation(libs.lottie.compose)

    // coroutines
    implementation(libs.kotlinx.coroutines.android)

    // logging
    implementation(libs.timber)

    // unit tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
}
