package com.jb.leitnerbox.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
fun SwipeableFlipCard2(
    recto: String,
    verso: String,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onEvaluate: (isCorrect: Boolean) -> Unit,
    modifier: Modifier = Modifier,
    rectoZoom: Float = 1.0f,
    versoZoom: Float = 1.0f
) {
    val density = LocalDensity.current
    val screenWidthPx = with(density) {
        LocalConfiguration.current.screenWidthDp.dp.toPx()
    }
    val threshold = screenWidthPx * 0.4f

    var offsetX by remember { mutableFloatStateOf(0f) }
    // Mémorise la direction pour éviter le clignotement lors de l'overshoot du ressort
    var swipeDirection by remember { mutableFloatStateOf(0f) }
    if (offsetX > 0) swipeDirection = 1f
    else if (offsetX < 0) swipeDirection = -1f

    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "SwipeOffset2",
        finishedListener = { finalValue ->
            if (abs(finalValue) > threshold) {
                onEvaluate(finalValue > 0)
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
                // Inclinaison progressive — accentuée à 15f
                rotationZ = (animatedOffsetX / screenWidthPx) * 15f
            }
            .clip(CardDefaults.shape)
            .clickable { onFlip() }
            .then(
                // draggable(Horizontal) uniquement sur le verso
                if (isFlipped) {
                    Modifier.draggable(
                        orientation = Orientation.Horizontal,
                        state = rememberDraggableState { delta ->
                            offsetX += delta
                        },
                        onDragStopped = {
                            when {
                                offsetX > threshold  -> offsetX = screenWidthPx * 1.5f
                                offsetX < -threshold -> offsetX = -screenWidthPx * 1.5f
                                else                 -> offsetX = 0f
                            }
                        }
                    )
                } else {
                    Modifier
                }
            )
    ) {
        // La Card conserve ses coins arrondis Material 3.
        // Le clip est géré par Card elle-même via sa forme par défaut.
        FlipCard2(
            recto = recto,
            verso = verso,
            isFlipped = isFlipped,
            onFlip = onFlip,
            rectoZoom = rectoZoom,
            versoZoom = versoZoom,
            modifier = Modifier.fillMaxWidth()
        )

        // Overlay coloré — par-dessus la carte, clippé à la même forme arrondie
        if (overlayAlpha > 0f) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CardDefaults.shape)   // même forme que Card
                    .background(overlayColor)
            )
        }
    }
}
