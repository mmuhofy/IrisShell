package com.iris.irisshell.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.iris.irisshell.design.system.IrisBorderSubtle
import com.iris.irisshell.design.system.IrisPrimary
import com.iris.irisshell.design.system.IrisSurfaceVariant
import com.iris.irisshell.design.system.IrisText
import com.iris.irisshell.design.system.IrisTextMuted
import com.iris.irisshell.design.system.IrisTextSecondary
import com.iris.irisshell.ui.R

private val SEARCH_BAR_SHAPE = RoundedCornerShape(20.dp)

enum class SearchScope {
    GLOBAL, BLOCK
}

@Composable
fun DraggableSearchBar(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    matchCount: Int,
    currentMatch: Int,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onClose: () -> Unit,
    searchScope: SearchScope,
    onToggleScope: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .zIndex(100f)
            .graphicsLayer {
                translationX = offsetX
                translationY = offsetY
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDrag = { change, _ ->
                        offsetX += change.delta.x
                        offsetY += change.delta.y
                        change.consume()
                    },
                )
            },
    ) {
        Box(
            modifier = Modifier
                .width(340.dp)
                .height(44.dp)
                .clip(SEARCH_BAR_SHAPE)
                .background(IrisSurfaceVariant)
                .border(
                    width = 1.dp,
                    color = IrisBorderSubtle.copy(alpha = 0.3f),
                    shape = SEARCH_BAR_SHAPE,
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    painter = painterResource(R.drawable.lucide_search),
                    contentDescription = null,
                    tint = IrisTextSecondary,
                    modifier = Modifier.size(16.dp),
                )

                SearchTextField(
                    value = searchText,
                    onValueChange = onSearchTextChange,
                    modifier = Modifier.weight(1f),
                )

                if (matchCount > 0) {
                    Text(
                        text = "$currentMatch/$matchCount",
                        color = IrisTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                    )
                }

                TextButton(
                    onClick = onToggleScope,
                    modifier = Modifier
                        .size(48.dp, 24.dp)
                        .padding(vertical = 4.dp),
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        containerColor = if (searchScope == SearchScope.BLOCK)
                            IrisPrimary.copy(alpha = 0.2f) else Color.Transparent,
                    ),
                ) {
                    Text(
                        text = if (searchScope == SearchScope.BLOCK) "Block" else "All",
                        color = if (searchScope == SearchScope.BLOCK) IrisPrimary else IrisTextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                NavigationArrow(
                    iconRes = R.drawable.lucide_chevron_up,
                    contentDescription = "Previous match",
                    onClick = onPrev,
                    enabled = matchCount > 1 && currentMatch > 1,
                )

                NavigationArrow(
                    iconRes = R.drawable.lucide_chevron_down,
                    contentDescription = "Next match",
                    onClick = onNext,
                    enabled = matchCount > 1 && currentMatch < matchCount,
                )

                NavigationArrow(
                    iconRes = R.drawable.lucide_x,
                    contentDescription = "Close search",
                    onClick = onClose,
                    enabled = true,
                )
            }
        }
    }
}

@Composable
private fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier,
) {
    var focused by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        if (value.isEmpty()) {
            Text(
                text = "Find in output...",
                color = IrisTextMuted,
                fontSize = 13.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp, vertical = 8.dp),
            )
        }
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { state ->
                    focused = state.isFocused
                },
            textStyle = TextStyle(
                color = IrisText,
                fontSize = 13.sp,
            ),
            placeholder = null,
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = { },
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = IrisPrimary,
            ),
            interactionSource = remember { MutableInteractionSource() },
        )
    }
}

@Composable
private fun NavigationArrow(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val tint = when {
        !enabled -> IrisTextMuted.copy(alpha = 0.4f)
        pressed -> IrisPrimary
        else -> IrisTextSecondary
    }

    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(24.dp),
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(14.dp),
        )
    }
}
