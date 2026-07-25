package com.iris.irisshell.ui.icons

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Lucide "more-vertical" — three vertical dots.
 *
 * Glyph data lifted from the official Lucide source
 * (https://lucide.dev/icons/more-vertical, MIT licensed) and translated to a
 * Compose [ImageVector]. Rendered through Material 3 [Icon] so it picks up
 * the parent's tint and stays crisp at any density.
 *
 *   viewBox  : 0 0 24 24
 *   paths    : 3 circles, radius = 1, centers (12,5) (12,12) (12,19)
 *
 * Built using [ImageVector.Builder.path] — the DSL extension on
 * [androidx.compose.ui.graphics.vector.ImageVector.Builder] that wraps
 * [androidx.compose.ui.graphics.vector.PathBuilder], which exposes
 * `moveTo`, `arcToRelative`, `close`, etc.
 */
val LucideMoreVertical: ImageVector =
    ImageVector.Builder(
        name = "LucideMoreVertical",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    )
        .path(fill = SolidColor(Color.White)) {
            // dot top — circle of radius 1 centered at (12,5)
            moveTo(12f, 5f)
            arcToRelative(1f, 1f, 0f, true, false, 2f, 0f)
            arcToRelative(1f, 1f, 0f, true, false, -2f, 0f)
            close()
        }
        .path(fill = SolidColor(Color.White)) {
            // dot middle — circle of radius 1 centered at (12,12)
            moveTo(12f, 12f)
            arcToRelative(1f, 1f, 0f, true, false, 2f, 0f)
            arcToRelative(1f, 1f, 0f, true, false, -2f, 0f)
            close()
        }
        .path(fill = SolidColor(Color.White)) {
            // dot bottom — circle of radius 1 centered at (12,19)
            moveTo(12f, 19f)
            arcToRelative(1f, 1f, 0f, true, false, 2f, 0f)
            arcToRelative(1f, 1f, 0f, true, false, -2f, 0f)
            close()
        }
        .build()

@Composable
fun MoreVertical(
    modifier: Modifier = Modifier.size(20.dp),
    tint: Color = Color.Unspecified,
) {
    Icon(
        imageVector = LucideMoreVertical,
        contentDescription = "More actions",
        modifier = modifier,
        tint = tint,
    )
}
