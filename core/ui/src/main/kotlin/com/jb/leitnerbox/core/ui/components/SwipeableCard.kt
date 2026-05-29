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
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
    var pendingEvaluation by remember { mutableStateOf<Boolean?>(null) }
    
    // Mémorise la direction pour éviter le clignotement lors de l'overshoot du ressort
    var swipeDirection by remember { mutableFloatStateOf(0f) }
    if (offsetX > 0) swipeDirection = 1f
    else if (offsetX < 0) swipeDirection = -1f

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "SwipeOffset",
        finishedListener = { finalValue ->
            if (abs(finalValue) > threshold) {
                pendingEvaluation?.let { onEvaluate(it) }
                pendingEvaluation = null
                offsetX = 0f
            }
            if (finalValue == 0f) {
                swipeDirection = 0f
            }
        }
    )

    val overlayAlpha = (abs(animatedOffsetX) / threshold).coerceIn(0f, 0.5f)
    val overlayColor = when {
        swipeDirection > 0 -> Color(0xFF4CAF50).copy(alpha = overlayAlpha)
        swipeDirection < 0 -> Color.Red.copy(alpha = overlayAlpha)
        else               -> Color.Transparent
    }

    Box(
        modifier = modifier
            .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
            .graphicsLayer {
                // Inclinaison progressive accentuée
                rotationZ = (animatedOffsetX / screenWidthPx) * 15f
            }
            .clip(CardDefaults.shape)
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
                                    pendingEvaluation = true
                                    offsetX = screenWidthPx * 1.5f
                                }
                                offsetX < -threshold -> {
                                    pendingEvaluation = false
                                    offsetX = -screenWidthPx * 1.5f
                                }
                                else                 -> offsetX = 0f
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
                    .clip(CardDefaults.shape)
                    .background(overlayColor)
            )
        }
    }
}
