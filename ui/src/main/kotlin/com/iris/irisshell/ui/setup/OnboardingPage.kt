package com.iris.irisshell.ui.setup

/**
 * Static metadata for a single onboarding page.
 *
 * Pages are intentionally narrow — the wizard is a quick orientation, not a
 * tutorial. Real configuration (theme packs, API keys, SSH) lives in Settings
 * after onboarding completes.
 */
sealed class OnboardingPage {
    abstract val title: String
    abstract val body: String

    data object Welcome : OnboardingPage() {
        override val title = "Iris Shell"
        override val body = "Your phone is a Unix machine. Finally."
    }

    data object Architecture : OnboardingPage() {
        override val title = "How it works"
        override val body = "Iris ships a real Ubuntu 24.04 in your sandbox, runs " +
            "it through PRoot + a vendored Termux terminal emulator. No rooting, " +
            "no Java pipe — true Linux CLI on tap."
    }

    data object PickShell : OnboardingPage() {
        override val title = "Pick your shell"
        override val body = "Default is zsh with Oh My Zsh, autosuggestions, and syntax " +
            "highlighting. Bash is also available — switch any time in Settings."
    }

    data object Ready : OnboardingPage() {
        override val title = "Ready when you are"
        override val body = "Bootstrap takes 2–5 minutes. We'll show every step — and you " +
            "can peek at the live log any time during setup."
    }

    companion object {
        val all: List<OnboardingPage> = listOf(Welcome, Architecture, PickShell, Ready)
    }
}
