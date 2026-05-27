package com.jb.leitnerbox.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeableCard(
    isFlipped: Boolean,
    onEvaluate: (isCorrect: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current
    val screenWidthPx = with(density) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    val threshold = screenWidthPx * 0.4f

    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "SwipeOffset",
        finishedListener = { if (offsetX == 0f) { /* animation de retour terminée */ } }
    )

    val overlayAlpha = (abs(animatedOffsetX) / threshold).coerceIn(0f, 0.5f)
    val overlayColor = when {
        animatedOffsetX > 0 -> Color(0xFF4CAF50).copy(alpha = overlayAlpha)
        animatedOffsetX < 0 -> Color.Red.copy(alpha = overlayAlpha)
        else                -> Color.Transparent
    }

    Box(
        modifier = modifier
            .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
            .then(
                if (isFlipped) {
                    Modifier.draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            offsetX += delta
                        },
                        onDragStopped = {
                            when {
                                offsetX > threshold  -> {
                                    onEvaluate(true)
                                    offsetX = 0f
                                }
                                offsetX < -threshold -> {
                                    onEvaluate(false)
                                    offsetX = 0f
                                }
                                else -> offsetX = 0f
                            }
                        }
                    )
                } else {
                    Modifier
                }
            )
    ) {
        content()

        if (overlayAlpha > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(overlayColor)
            )
        }
    }
}
