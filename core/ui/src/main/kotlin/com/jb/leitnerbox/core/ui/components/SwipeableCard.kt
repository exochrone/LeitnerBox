package com.jb.leitnerbox.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
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
    var offsetX by remember { mutableFloatStateOf(0f) }
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "swipeOffset",
        finishedListener = { offsetX = 0f }
    )

    val screenWidthPx = with(LocalDensity.current) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    val threshold = screenWidthPx * 0.4f

    val overlayColor = when {
        animatedOffsetX > 0 ->
            Color(0xFF4CAF50).copy(alpha = (animatedOffsetX / threshold).coerceIn(0f, 0.5f))
        animatedOffsetX < 0 ->
            Color.Red.copy(alpha = (-animatedOffsetX / threshold).coerceIn(0f, 0.5f))
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
            .pointerInput(isFlipped) {
                if (!isFlipped) return@pointerInput

                // Détecter les gestes — n'intercepter que si horizontal dominant
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    var totalDx = 0f
                    var totalDy = 0f
                    var isDraggingHorizontally = false

                    while (true) {
                        val event = awaitPointerEvent()
                        val drag = event.changes.firstOrNull() ?: break

                        if (!drag.pressed) {
                            // Relâchement
                            if (isDraggingHorizontally) {
                                when {
                                    offsetX > threshold -> {
                                        onEvaluate(true)
                                    }
                                    offsetX < -threshold -> {
                                        onEvaluate(false)
                                    }
                                }
                                offsetX = 0f
                            }
                            break
                        }

                        totalDx += drag.positionChange().x
                        totalDy += drag.positionChange().y

                        // Déterminer la direction dominante après 10 px de mouvement
                        if (!isDraggingHorizontally &&
                            (abs(totalDx) + abs(totalDy)) > 10f
                        ) {
                            if (abs(totalDx) > abs(totalDy)) {
                                isDraggingHorizontally = true
                            } else {
                                // Geste vertical : ne pas consommer, laisser passer
                                break
                            }
                        }

                        if (isDraggingHorizontally) {
                            drag.consume()
                            offsetX += drag.positionChange().x
                        }
                    }
                }
            }
    ) {
        content()

        // Overlay coloré
        if (overlayColor != Color.Transparent) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(overlayColor)
            )
        }
    }
}
