package com.jb.leitnerbox.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

@Composable
fun FlipCard(
    recto: String,
    verso: String,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "CardFlip"
    )

    val isAtRecto = rotation <= 90f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Face recto
            if (isAtRecto) {
                CardFace(
                    text = recto,
                    style = MaterialTheme.typography.headlineMedium,
                    onTap = onFlip,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Face verso — contre-rotation pour annuler le miroir
            if (!isAtRecto) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                    contentAlignment = Alignment.Center
                ) {
                    CardFace(
                        text = verso,
                        style = MaterialTheme.typography.headlineSmall,
                        onTap = onFlip,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
