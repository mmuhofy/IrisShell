package com.iris.irisshell.ui.setup.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.iris.irisshell.ui.setup.OnboardingPage
import com.iris.irisshell.ui.setup.OnboardingViewModel
import com.iris.irisshell.ui.setup.components.SetupHeroMark
import com.iris.irisshell.ui.setup.theme.SetupPalette
import kotlinx.coroutines.launch

/**
 * Four-page horizontal pager onboarding.
 *
 * Persists `firstLaunchCompleted=true` to DataStore on Continue, then
 * delegates to [TriggerBootstrapUseCase] so the bootstrap kicks off.
 *
 * Skipping the wizard is allowed only on page 1 (welcome); deeper pages
 * require Continue.
 */
@Composable
fun OnboardingScreen(
    onCompleted: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel(),
    modifier: Modifier = Modifier,
) {
    val pages = OnboardingPage.all
    val pagerState: PagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SetupPalette.Background),
    ) {
        // Top-right Skip anchor — visible only on page 0.
        if (pagerState.currentPage == 0) {
            Text(
                text = "Skip",
                color = SetupPalette.TextMuted,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                ),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 44.dp, end = 24.dp)
                    .clickable {
                        viewModel.finishOnboarding(thenStartBootstrap = true)
                        onCompleted()
                    },
            )
        }

        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .padding(horizontal = 28.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(28.dp))
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp),
            ) { page ->
                OnboardingPageContent(pages[page])
            }

            Spacer(modifier = Modifier.height(28.dp))

            PagerDots(
                total = pages.size,
                current = pagerState.currentPage,
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = {
                    val next = pagerState.currentPage + 1
                    if (next < pages.size) {
                        scope.launch {
                            pagerState.animateScrollToPage(next)
                        }
                    } else {
                        viewModel.finishOnboarding(thenStartBootstrap = true)
                        onCompleted()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SetupPalette.Primary,
                    contentColor = SetupPalette.OnPrimary,
                ),
            ) {
                Text(
                    text = if (pagerState.currentPage == pages.lastIndex) "Start setup" else "Continue",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "No telemetry. No accounts. Open source.",
                color = SetupPalette.TextDisabled,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                ),
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (page is OnboardingPage.Welcome) {
            SetupHeroMark(sizeDp = 96.dp)
            Spacer(modifier = Modifier.height(24.dp))
        }
        AnimatedContent(
            targetState = page.title,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(180)))
            },
            label = "page-title",
        ) { title ->
            Text(
                text = title,
                color = SetupPalette.Text,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = page.body,
            color = SetupPalette.TextSecondary,
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 14.sp,
                lineHeight = 22.sp,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
        )
        if (page is OnboardingPage.Architecture) {
            Spacer(modifier = Modifier.height(20.dp))
            ArchDiagram()
        }
        if (page is OnboardingPage.PickShell) {
            Spacer(modifier = Modifier.height(20.dp))
            ShellToggleCard()
        }
    }
}

@Composable
private fun ArchDiagram() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SetupPalette.Surface)
            .border(1.dp, SetupPalette.Outline, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        DiagramRow("Iris Shell (Compose UI)")
        DiagramRow("↓")
        DiagramRow("termux-emulator + JNI (vendored)")
        DiagramRow("↓")
        DiagramRow("PRoot")
        DiagramRow("↓")
        DiagramRow("Ubuntu 24.04 rootfs (chroot-style)")
    }
}

@Composable
private fun DiagramRow(text: String) {
    Text(
        text = text,
        color = if (text == "↓") SetupPalette.TextMuted else SetupPalette.Text,
        style = TextStyle(
            fontFamily = FontFamily.Monospace,
            fontSize = 12.sp,
            fontWeight = if (text == "↓") FontWeight.Normal else FontWeight.Medium,
        ),
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
    )
}

@Composable
private fun ShellToggleCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SetupPalette.Surface)
            .border(1.dp, SetupPalette.Outline, RoundedCornerShape(12.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Default shell",
                color = SetupPalette.TextMuted,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                ),
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "zsh",
                color = SetupPalette.Text,
                style = TextStyle(
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                ),
            )
        }
        Text(
            text = "(change in Settings)",
            color = SetupPalette.TextMuted,
            style = TextStyle(
                fontFamily = FontFamily.SansSerif,
                fontSize = 11.sp,
            ),
        )
    }
}

@Composable
private fun PagerDots(total: Int, current: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(total) { index ->
            val isActive = index == current
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .size(if (isActive) 10.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (isActive) SetupPalette.Primary else SetupPalette.TextDisabled),
            )
        }
    }
}
