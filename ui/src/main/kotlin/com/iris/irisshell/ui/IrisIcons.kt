package com.iris.irisshell.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

object IrisIcons {

    val ALargeSmall: ImageVector = ImageVector.Builder(
        name = "a-large-small",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "m15,16l2.536,-7.328a1.02,1.02 1 0,1 1.928,0L22,16",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M15.697,14h5.606",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "m2,16l4.039,-9.69a0.5,0.5 0 0,1 0.923,0L11,16",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M3.304,13h6.392",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val ArrowBigDown: ImageVector = ImageVector.Builder(
        name = "arrow-big-down",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M9,5a1,1 0 0,1 1,-1h4a1,1 0 0,1 1,1v6a1,1 0 0,0 1,1h3.293a0.707,0.707 0 0,1 0.5,1.207l-7.086,7.086a1,1 0 0,1 -1.414,0l-7.086,-7.086a0.707,0.707 0 0,1 0.5,-1.207H8a1,1 0 0,0 1,-1Z",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val ArrowBigLeft: ImageVector = ImageVector.Builder(
        name = "arrow-big-left",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M10.793,19.793a0.707,0.707,0,0,0,1.207,-0.5V16a1,1,0,0,1,1,-1h6a1,1,0,0,0,1,-1v-4a1,1,0,0,0,-1,-1h-6a1,1,0,0,1,-1,-1V4.707a0.707,0.707,0,0,0,-1.207,-0.5l-6.94,6.94a1.207,1.207,0,0,0,0,1.707Z",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val ArrowBigRight: ImageVector = ImageVector.Builder(
        name = "arrow-big-right",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M13.207,19.793a0.707,0.707 0,0,1 -1.207,-0.5V16a1,1 0,0,0 -1,-1H5a1,1 0,0,1 -1,-1v-4a1,1 0,0,1 1,-1h6a1,1 0,0,0 1,-1V4.707a0.707,0.707 0,0,1 1.207,-0.5l6.94,6.94a1.207,1.207 0,0,1 0,1.707Z",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val ArrowBigUp: ImageVector = ImageVector.Builder(
        name = "arrow-big-up",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M9,19a1,1 0 0,0 1,1h4a1,1 0 0,0 1,-1v-6a1,1 0 0,1 1,-1h3.293a0.707,0.707 0 0,0 0.5,-1.207l-7.086,-7.086a1,1 0 0,0 -1.414,0l-7.086,7.086a0.707,0.707 0 0,0 0.5,1.207H8a1,1 0 0,1 1,1Z",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val ArrowDown: ImageVector = ImageVector.Builder(
        name = "arrow-down",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M12,5v14",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "m19,12l-7,7l-7,-7",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val ArrowLeft: ImageVector = ImageVector.Builder(
        name = "arrow-left",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "m12,19l-7,-7l7,-7",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M19,12H5",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val ArrowUp: ImageVector = ImageVector.Builder(
        name = "arrow-up",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "m5,12l7,-7l7,7",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M12,19V5",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val Check: ImageVector = ImageVector.Builder(
        name = "check",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M20,6L9,17l-5,-5",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val ChevronDown: ImageVector = ImageVector.Builder(
        name = "chevron-down",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "m6,9l6,6l6,-6",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val ChevronUp: ImageVector = ImageVector.Builder(
        name = "chevron-up",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "m18,15l-6,-6l-6,6",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val Copy: ImageVector = ImageVector.Builder(
        name = "copy",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M10,8H20A2,2 0 0,1 22,10V20A2,2 0 0,1 20,22H10A2,2 0 0,1 8,20V10A2,2 0 0,1 10,8Z",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M4,16c-1.1,0 -2,-0.9 -2,-2V4c0,-1.1 0.9,-2 2,-2h10c1.1,0 2,0.9 2,2",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val Download: ImageVector = ImageVector.Builder(
        name = "download",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M12,15V3",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M21,15v4a2,2 0 0,1 -2,2H5a2,2 0 0,1 -2,-2v-4",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "m7,10l5,5l5,-5",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val EllipsisVertical: ImageVector = ImageVector.Builder(
        name = "ellipsis-vertical",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M13,12A1,1 0 0,1 12,13A1,1 0 0,1 11,12A1,1 0 0,1 13,12Z",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M13,5A1,1 0 0,1 12,6A1,1 0 0,1 11,5A1,1 0 0,1 13,5Z",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M13,19A1,1 0 0,1 12,20A1,1 0 0,1 11,19A1,1 0 0,1 13,19Z",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val Lock: ImageVector = ImageVector.Builder(
        name = "lock",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M5,11H19A2,2 0 0,1 21,13V20A2,2 0 0,1 19,22H5A2,2 0 0,1 3,20V13A2,2 0 0,1 5,11Z",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M7,11V7a5,5 0 0,1 10,0v4",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val Pencil: ImageVector = ImageVector.Builder(
        name = "pencil",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M21.174,6.812a1,1 0 0,0 -3.986,-3.987L3.842,16.174a2,2 0 0,0 -0.5,0.83l-1.321,4.352a0.5,0.5 0 0,0 0.623,0.622l4.353,-1.32a2,2 0 0,0 0.83,-0.497Z",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "m15,5l4,4",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val Play: ImageVector = ImageVector.Builder(
        name = "play",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M5,5a2,2 0 0,1 3.008,-1.728l11.997,6.998a2,2 0 0,1 0.003,3.458l-12,7A2,2 0 0,1 5,19Z",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val Plus: ImageVector = ImageVector.Builder(
        name = "plus",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M5,12h14",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M12,5v14",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val Search: ImageVector = ImageVector.Builder(
        name = "search",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "m21,21l-4.34,-4.34",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M19,11A8,8 0 0,1 11,19A8,8 0 0,1 3,11A8,8 0 0,1 19,11Z",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val Settings: ImageVector = ImageVector.Builder(
        name = "settings",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M9.671,4.136a2.34,2.34 0 0,1 4.659,0a2.34,2.34 0 0,0 3.319,1.915a2.34,2.34 0 0,1 2.33,4.033a2.34,2.34 0 0,0 0,3.831a2.34,2.34 0 0,1 -2.33,4.033a2.34,2.34 0 0,0 -3.319,1.915a2.34,2.34 0 0,1 -4.659,0a2.34,2.34 0 0,0 -3.32,-1.915a2.34,2.34 0 0,1 -2.33,-4.033a2.34,2.34 0 0,0 0,-3.831A2.34,2.34 0 0,1 6.35,6.051a2.34,2.34 0 0,0 3.319,-1.915",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M15,12A3,3 0 0,1 12,15A3,3 0 0,1 9,12A3,3 0 0,1 15,12Z",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val Square: ImageVector = ImageVector.Builder(
        name = "square",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M5,3H19A2,2 0 0,1 21,5V19A2,2 0 0,1 19,21H5A2,2 0 0,1 3,19V5A2,2 0 0,1 5,3Z",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val SquareTerminal: ImageVector = ImageVector.Builder(
        name = "square-terminal",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "m7,11l2,-2l-2,-2",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M11,13h4",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M5,3H19A2,2 0 0,1 21,5V19A2,2 0 0,1 19,21H5A2,2 0 0,1 3,19V5A2,2 0 0,1 5,3Z",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val Terminal: ImageVector = ImageVector.Builder(
        name = "terminal",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M12,19h8",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "m4,17l6,-6l-6,-6",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val Trash2: ImageVector = ImageVector.Builder(
        name = "trash-2",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M10,11v6",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M14,11v6",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M19,6v14a2,2 0 0,1 -2,2H7a2,2 0 0,1 -2,-2V6",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M3,6h18",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "M8,6V4a2,2 0 0,1 2,-2h4a2,2 0 0,1 2,2v2",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )

    val X: ImageVector = ImageVector.Builder(
        name = "x",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build(
        path(
            pathData = "M18,6L6,18",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
        path(
            pathData = "m6,6l12,12",
            strokeColor = Color.Black,
            strokeWidth = 2f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round,
        ),
    )
}
