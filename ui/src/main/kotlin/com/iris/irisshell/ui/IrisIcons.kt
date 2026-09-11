package com.iris.irisshell.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

object IrisIcons {

    private fun iv(
        name: String,
        pathData: List<String>,
    ): ImageVector = ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        val parser = PathParser()
        pathData.forEach { data ->
            addPath(
                pathData = parser.parsePathString(data).toNodes(),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }.build()

    val ALargeSmall: ImageVector = iv(
        "a-large-small",
        listOf(
            "m15,16l2.536,-7.328a1.02,1.02 1 0,1 1.928,0L22,16",
            "m-11.6-.404c.315-.944.716-1.873 1.2-2.778l.09-.157A1,1 0 0,1 14.5,12.5L14.5,14L15,14L15,15.5A1.5,1.5 0 0,1 13.5,17L9.5,17A1.5,1.5 0 0,1 8,15.5L8,14L8.5,14L8.5,12.5A1,1 0 0,1 9.6,11.096",
        )
    )

    val ArrowBigDown: ImageVector = iv(
        "arrow-big-down",
        listOf("M9,5a1,1 0 0,1 1,-1h4a1,1 0 0,1 1,1v6a1,1 0 0,0 1,1h3.293a0.707,0.707 0 0,1 0.5,1.207l-7.086,7.086a1,1 0 0,1 -1.414,0l-7.086,-7.086a0.707,0.707 0 0,1 0.5,-1.207H8a1,1 0 0,0 1,-1Z")
    )

    val ArrowBigLeft: ImageVector = iv(
        "arrow-big-left",
        listOf("M10.793,19.793a0.707,0.707,0,0,0,1.207,-0.5V16a1,1,0,0,1 1,-1h6a1,1,0,0,0 1,-1v-4a1,1,0,0,0 -1,-1h-6a1,1,0,0,1 -1,-1V4.707a0.707,0.707,0,0,0 -1.207,-0.5l-6.94,6.94a1.207,1.207,0,0,0,0,1.707Z")
    )

    val ArrowBigRight: ImageVector = iv(
        "arrow-big-right",
        listOf("M13.207,19.793a0.707,0.707 0,0,1 -1.207,-0.5V16a1,1,0,0,0 -1,-1H5a1,1,0,0,1 -1,-1v-4a1,1,0,0,1 1,-1h6a1,1,0,0,0 1,-1V4.707a0.707,0.707,0,0,1 1.207,-0.5l6.94,6.94a1.207,1.207,0,0,1 0,1.707Z")
    )

    val ArrowBigUp: ImageVector = iv(
        "arrow-big-up",
        listOf("M9,19a1,1 0 0,0 1,1h4a1,1 0 0,0 1,-1v-6a1,1 0 0,1 1,-1h3.293a0.707,0.707 0 0,0 0.5,-1.207l-7.086,-7.086a1,1 0 0,0 -1.414,0l-7.086,7.086a0.707,0.707 0 0,0 0.5,1.207H8a1,1 0 0,1 1,1Z")
    )

    val ArrowDown: ImageVector = iv(
        "arrow-down",
        listOf(
            "M12,5v14",
            "m-7,7l7,7",
            "m7,-7l-7,7",
        )
    )

    val ArrowLeft: ImageVector = iv(
        "arrow-left",
        listOf(
            "m12,19l-7,-7l7,-7",
            "M19,12H5",
        )
    )

    val ArrowUp: ImageVector = iv(
        "arrow-up",
        listOf(
            "m5,12l7,-7l7,7",
            "M12,19V5",
        )
    )

    val Check: ImageVector = iv(
        "check",
        listOf("M20,6L9,17l-5,-5")
    )

    val ChevronDown: ImageVector = iv(
        "chevron-down",
        listOf("m6,9l6,6l6,-6")
    )

    val ChevronUp: ImageVector = iv(
        "chevron-up",
        listOf("m18,15l-6,-6l-6,6")
    )

    val Copy: ImageVector = iv(
        "copy",
        listOf(
            "M10,8H20A2,2 0 0,1 22,10V20A2,2 0 0,1 20,22H10A2,2 0 0,1 8,20V10A2,2 0 0,1 10,8Z",
            "M4,16c-1.1,0 -2,-0.9 -2,-2V4c0,-1.1 0.9,-2 2,-2h10c1.1,0 2,0.9 2,2",
        )
    )

    val Download: ImageVector = iv(
        "download",
        listOf(
            "M12,15V3",
            "M21,15v4a2,2 0 0,1 -2,2H5a2,2 0 0,1 -2,-2v-4",
            "m7,0l5,5l5,-5",
        )
    )

    val EllipsisVertical: ImageVector = iv(
        "ellipsis-vertical",
        listOf(
            "M12,12A1,1 0 0,1 11,13A1,1 0 0,1 13,13A1,1 0 0,1 12,12Z",
            "M12,5A1,1 0 0,1 11,6A1,1 0 0,1 13,6A1,1 0 0,1 12,5Z",
            "M12,19A1,1 0 0,1 11,20A1,1 0 0,1 13,20A1,1 0 0,1 12,19Z",
        )
    )

    val Lock: ImageVector = iv(
        "lock",
        listOf(
            "M5,11H19A2,2 0 0,1 21,13V20A2,2 0 0,1 19,22H5A2,2 0 0,1 3,20V13A2,2 0 0,1 5,11Z",
            "M7,11V7a5,5 0 0,1 10,0v4",
        )
    )

    val Pencil: ImageVector = iv(
        "pencil",
        listOf(
            "M21.174,6.812a1,1 0 0,0 -3.986,-3.987L3.842,16.174a2,2 0 0,0 -0.5,0.83l-1.321,4.352a0.5,0.5 0 0,0 0.623,0.622l4.353,-1.32a2,2 0 0,0 0.83,-0.497Z",
            "m15,5l4,4",
        )
    )

    val Play: ImageVector = iv(
        "play",
        listOf("M5,5a2,2 0 0,1 3.008,-1.728l11.997,6.998a2,2 0 0,1 0.003,3.458l-12,7A2,2 0 0,1 5,19Z")
    )

    val Plus: ImageVector = iv(
        "plus",
        listOf(
            "M5,12h14",
            "M12,5v14",
        )
    )

    val Search: ImageVector = iv(
        "search",
        listOf(
            "m21,21l-4.34,-4.34",
            "M19,11A8,8 0 0,1 11,19A8,8 0 0,1 3,11A8,8 0 0,1 19,11Z",
        )
    )

    val Settings: ImageVector = iv(
        "settings",
        listOf(
            "M9.671,4.136a2.34,2.34 0 0,1 4.659,0a2.34,2.34 0 0,1 3.319,1.915a2.34,2.34 0 0,1 2.33,4.033a2.34,2.34 0 0,1 0,3.831a2.34,2.34 0 0,1 -2.33,4.033a2.34,2.34 0 0,1 -3.319,1.915a2.34,2.34 0 0,1 -4.659,0a2.34,2.34 0 0,1 -3.32,-1.915a2.34,2.34 0 0,1 -2.33,-4.033a2.34,2.34 0 0,1 0,-3.831A2.34,2.34 0 0,1 6.35,6.051a2.34,2.34 0 0,1 3.319,-1.915",
            "M15,12A3,3 0 0,1 12,15A3,3 0 0,1 9,12A3,3 0 0,1 15,12Z",
        )
    )

    val Square: ImageVector = iv(
        "square",
        listOf("M5,3H19A2,2 0 0,1 21,5V19A2,2 0 0,1 19,21H5A2,2 0 0,1 3,19V5A2,2 0 0,1 5,3Z")
    )

    val SquareTerminal: ImageVector = iv(
        "square-terminal",
        listOf(
            "m7,11l2,-2l-2,-2",
            "M11,13h4",
            "M5,3H19A2,2 0 0,1 21,5V19A2,2 0 0,1 19,21H5A2,2 0 0,1 3,19V5A2,2 0 0,1 5,3Z",
        )
    )

    val Terminal: ImageVector = iv(
        "terminal",
        listOf(
            "M12,19h8",
            "m-4,-16l-4,4l4,4",
        )
    )

    val Trash2: ImageVector = iv(
        "trash-2",
        listOf(
            "M10,11v6",
            "M14,11v6",
            "M19,6v14a2,2 0 0,1 -2,2H7a2,2 0 0,1 -2,-2V6",
            "M5,6h14",
            "M8,6V4a2,2 0 0,1 2,-2h4a2,2 0 0,1 2,2v2",
        )
    )

    val X: ImageVector = iv(
        "x",
        listOf(
            "M18,6L6,18",
            "m6,-18L6,18",
        )
    )
}
