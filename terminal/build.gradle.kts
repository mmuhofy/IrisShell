// terminal/ — PTY session management, terminal emulator bridge,
// block engine, semantic parser, ghost text engine.
//
// Per AGENT.md §125-128: "Isolated — only data/ and agent/ interact with it."
// Per AGENT.md §143: "terminal/ is isolated — never imported directly by ui/"
// Per MEMORYBANK.md §97-100: engine/, renderer/, input/.

plugins {
    alias(libs.plugins.iris.android.library)
    alias(libs.plugins.iris.kotlin.serialization)
}

android {
    namespace = "com.iris.irisshell.terminal"

    defaultConfig {
        // termux-view JNI binary (libtermux.so) — vendored prebuilt from IrisCode.
        // ABIs match those shipped by the ported termux-view module.
        ndk {
            abiFilters += setOf("arm64-v8a", "x86_64", "x86", "armeabi-v7a")
        }
    }
}

dependencies {
    api(project(":domain"))
    implementation(project(":core"))

    // coroutines for dedicated TerminalManager CoroutineScope
    // (Coroutine Scopes rule: AGENT.md §222)
    implementation(libs.kotlinx.coroutines.android)

    // logging
    implementation(libs.timber)

    // serialization — block / semantic token export to JSON
    implementation(libs.kotlinx.serialization.json)

    // unit tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)
}
