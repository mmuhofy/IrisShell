// data/ — repository implementations, local (Room), remote (LLM, SSH).
// Per AGENT.md §119-121: implements interfaces from domain/.
// Per MEMORYBANK.md §85-90: terminal manager, agent streamer, Room DAOs,
// remote LLM / SSH clients, theme store client.

plugins {
    alias(libs.plugins.iris.android.library)
    alias(libs.plugins.iris.android.room)
    alias(libs.plugins.iris.android.hilt)
    alias(libs.plugins.iris.kotlin.serialization)
}

android {
    namespace = "com.iris.irisshell.data"
}

dependencies {
    api(project(":domain"))
    implementation(project(":core"))

    // storage — DataStore
    implementation(libs.androidx.datastore.preferences)
    // security — encrypted API keys + SSH key vault
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.biometric)

    // networking — OkHttp + SSE for streaming LLM responses
    implementation(libs.okhttp)
    implementation(libs.okhttp.sse)
    implementation(libs.okhttp.logging)

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
    testImplementation(libs.room.testing)
}
