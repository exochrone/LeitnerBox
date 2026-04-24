package com.jb.leitnerbox.core.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun SwipeableCard(
    isFlipped: Boolean,
    onEvaluate: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    
    val screenWidth = configuration.screenWidthDp.dp
    val screenWidthPx = with(density) { screenWidth.toPx() }
    
    // Seuil de validation (45%)
    val thresholdPx = screenWidthPx * 0.45f 
    val colorStartOffsetPx = screenWidthPx * 0.15f 

    // Utilisation de Animatable pour contrôler finement la séquence d'animation
    val offsetX = remember { Animatable(0f) }

    // Calcul de l'alpha progressif basé sur la valeur actuelle de l'animation
    val calculateAlpha = { offset: Float ->
        val absOffset = abs(offset)
        if (absOffset < colorStartOffsetPx) {
            0f
        } else {
            // On progresse vers 85% d'opacité à mesure qu'on approche ou dépasse le bord
            val progress = (absOffset - colorStartOffsetPx) / (screenWidthPx - colorStartOffsetPx)
            (progress * 0.85f).coerceIn(0f, 0.85f)
        }
    }

    val animatedOffsetX = offsetX.value
    val overlayColor = when {
        animatedOffsetX > 0 -> Color(0xFF4CAF50).copy(alpha = calculateAlpha(animatedOffsetX))
        animatedOffsetX < 0 -> Color(0xFFB2261D).copy(alpha = calculateAlpha(animatedOffsetX))
        else -> Color.Transparent
    }

    val rotationZ = (animatedOffsetX / screenWidthPx) * 50f

    Box(
        modifier = modifier
            .offset { IntOffset(animatedOffsetX.roundToInt(), 0) }
            .graphicsLayer {
                this.rotationZ = rotationZ
            }
            .clip(RoundedCornerShape(12.dp))
            .drawWithContent {
                drawContent()
                drawRect(overlayColor)
            }
            .pointerInput(isFlipped) {
                if (!isFlipped) return@pointerInput
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            if (abs(offsetX.value) > thresholdPx) {
                                // Déterminer la direction de sortie
                                val isCorrect = offsetX.value > 0
                                val target = if (isCorrect) screenWidthPx * 1.5f else -screenWidthPx * 1.5f
                                
                                // 1. Animer la sortie complète de la carte
                                offsetX.animateTo(
                                    targetValue = target,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy)
                                )
                                
                                // 2. Seulement après la sortie, on déclenche le changement de carte
                                onEvaluate(isCorrect)
                            } else {
                                // Retour au centre si seuil non atteint
                                offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                )
                            }
                        }
                    }
                )
            }
    ) {
        content()
    }
}
