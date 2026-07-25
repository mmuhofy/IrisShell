package com.iris.irisshell.ui.setup.onboarding

/**
 * The three scenes that make up Iris Shell's onboarding flow.
 *
 * The wizard is intentionally short — a "show, don't tell" preview, not a
 * tutorial. Real configuration (theme packs, API keys, SSH) lives in
 * Settings after onboarding completes.
 *
 * Per MEMORYBANK.md: Iris deliberately avoids Material 3 expressive
 * animations. Transitions are 220ms fade only.
 */
enum class OnboardingSceneKind {
    Welcome,
    Architecture,
    Ready;

    fun next(): OnboardingSceneKind? =
        entries.getOrNull(ordinal + 1)
}
