package com.jb.leitnerbox.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

import com.jb.leitnerbox.core.ui.theme.CardRectoBackground
import com.jb.leitnerbox.core.ui.theme.CardRectoContent

@Composable
fun FlipCard(
    recto: String,
    verso: String,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
    rectoLabel: String = "",
    rectoSubLabel: String = "",
    versoLabel: String = "",
    versoSubLabel: String = "",
    actions: @Composable () -> Unit = {}
) {
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(
            durationMillis = 350,
            easing = FastOutSlowInEasing
        ),
        label = "cardFlip"
    )

    val isAtRecto = rotation <= 90f

    // État de préparation du recto
    var rectoReady by remember { mutableStateOf(false) }

    val cardAlpha by animateFloatAsState(
        targetValue = if (rectoReady) 1f else 0f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label = "cardAlpha"
    )

    Card(
        modifier = modifier
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .alpha(cardAlpha)
            .clickable { onFlip() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAtRecto) CardRectoBackground else MaterialTheme.colorScheme.surfaceVariant,
            contentColor = if (isAtRecto) CardRectoContent else MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = BorderStroke(2.dp, Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ligne du haut avec Label + Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        if (rotation > 90f) rotationY = 180f
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Espaceur à gauche pour centrer le titre
                Spacer(modifier = Modifier.size(48.dp))

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (rotation <= 90f) rectoLabel else versoLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    val subLabel = if (rotation <= 90f) rectoSubLabel else versoSubLabel
                    if (subLabel.isNotBlank()) {
                        Text(
                            text = subLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Normal,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    actions()
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                // Face recto
                MathText(
                    text = recto,
                    style = MaterialTheme.typography.headlineMedium,
                    onRendered = { rectoReady = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .verticalScroll(rememberScrollState())
                        .alpha(if (rotation <= 90f) 1f else 0f)
                )

                // Face verso
                MathText(
                    text = verso,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            rotationY = 180f
                        }
                        .padding(vertical = 8.dp)
                        .verticalScroll(rememberScrollState())
                        .alpha(if (rotation > 90f) 1f else 0f)
                )
            }
        }
    }
}
