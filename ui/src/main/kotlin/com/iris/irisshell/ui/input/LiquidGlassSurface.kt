package com.iris.irisshell.ui.input

import android.graphics.RuntimeShader
import android.os.Build
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

private const val GLASS_SHADER = """
uniform shader composable;
uniform float2 resolution;
uniform float time;

half4 main(float2 p) {
    float2 uv = p / resolution;

    // Very small optical drift: enough to make the glass feel refractive,
    // without visibly warping terminal glyphs.
    float2 d = float2(
        sin(uv.y * 8.0 + time * 0.35) * 0.7,
        cos(uv.x * 7.0 + time * 0.30) * 0.7
    );

    half4 c = composable.eval(p + d);

    float top = 1.0 - smoothstep(0.0, 0.28, uv.y);
    float edge = 1.0 - smoothstep(0.0, 0.12, min(uv.x, 1.0 - uv.x));

    c.rgb += half3(top * 0.055);
    c.rgb += half3(edge * 0.018);

    return c;
}
"""

@Composable
fun LiquidGlassSurface(
    modifier: Modifier = Modifier,
) {
    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        val radius = 26.dp.toPx()
        val w = size.width
        val h = size.height

        // Base glass: deliberately low alpha so the sampled terminal remains visible.
        drawRoundRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFF252A31).copy(alpha = 0.54f),
                    Color(0xFF17191D).copy(alpha = 0.46f),
                    Color(0xFF28222A).copy(alpha = 0.50f),
                ),
            ),
            cornerRadius = CornerRadius(radius, radius),
        )

        // Ambient chroma inside the glass.
        drawCircle(
            color = Color(0xFF31577A).copy(alpha = 0.15f),
            radius = h * 1.85f,
            center = Offset(-w * 0.04f, h * 0.48f),
        )
        drawCircle(
            color = Color(0xFF6A466E).copy(alpha = 0.13f),
            radius = h * 1.70f,
            center = Offset(w * 1.02f, -h * 0.22f),
        )
        drawCircle(
            color = Color(0xFF315E52).copy(alpha = 0.09f),
            radius = h * 1.45f,
            center = Offset(w * 0.90f, h * 1.18f),
        )

        // Specular band + subtle lower density.
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.115f),
                    Color.Transparent,
                    Color.Black.copy(alpha = 0.095f),
                ),
            ),
            cornerRadius = CornerRadius(radius, radius),
        )

        // Hairline border.
        drawRoundRect(
            color = Color.White.copy(alpha = 0.14f),
            style = Stroke(width = 1.dp.toPx()),
            cornerRadius = CornerRadius(radius, radius),
        )

        // Fine inner highlight.
        drawRoundRect(
            color = Color.White.copy(alpha = 0.035f),
            style = Stroke(width = 0.5.dp.toPx()),
            cornerRadius = CornerRadius((radius - 1.dp.toPx()).coerceAtLeast(0f)),
        )
    }
}
