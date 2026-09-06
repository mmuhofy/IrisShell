package com.iris.irisshell.ui.setup.onboarding

/**
 * The four scenes that make up Iris Shell's onboarding wizard.
 *
 * Flow:
 *   Welcome → DeviceCheck → Preferences → ShellSetup (conditional — Zsh only)
 *
 * ShellSetup is only shown when the user selects Zsh in Preferences. If Bash
 * is selected, the wizard finishes after Preferences.
 *
 * Transitions are full-screen fade (220ms). The terminal backdrop from the
 * old 3-scene flow is no longer used — the new scenes use clean Surface
 * cards with SetupButton anchors.
 */
enum class OnboardingSceneKind {
    Welcome,
    DeviceCheck,
    Preferences,
    ShellSetup;

    fun next(): OnboardingSceneKind? =
        entries.getOrNull(ordinal + 1)
}
