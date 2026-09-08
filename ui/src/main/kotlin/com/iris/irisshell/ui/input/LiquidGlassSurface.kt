package com.iris.irisshell.ui.input

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier.fillMaxSize(),
    ) {
        val radius = 24.dp.toPx()

        /*
         * Very subtle material tint.
         *
         * This is intentionally NOT an opaque glass panel.
         * Most of the visual information should come from
         * the backdrop underneath.
         */
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.035f),
                    Color.White.copy(alpha = 0.012f),
                    Color.Black.copy(alpha = 0.035f),
                ),
            ),
            cornerRadius = CornerRadius(radius, radius),
        )

        /*
         * Extremely soft top specular reflection.
         */
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.065f),
                    Color.Transparent,
                ),
                startY = 0f,
                endY = size.height * 0.42f,
            ),
            cornerRadius = CornerRadius(radius, radius),
        )

        /*
         * Subtle inner edge.
         */
        drawRoundRect(
            color = Color.White.copy(alpha = 0.075f),
            style = Stroke(
                width = 0.75.dp.toPx(),
            ),
            cornerRadius = CornerRadius(radius, radius),
        )

        /*
         * Slight lower edge darkening.
         */
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.045f),
                ),
                startY = size.height * 0.55f,
                endY = size.height,
            ),
            cornerRadius = CornerRadius(radius, radius),
        )
    }
}