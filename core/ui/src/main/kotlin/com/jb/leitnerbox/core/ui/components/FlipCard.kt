package com.jb.leitnerbox.core.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    actions: @Composable BoxScope.() -> Unit = {}
) {
    var rectoReady by remember { mutableStateOf(false) }

    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "CardFlipAnimation"
    )

    val isAtRecto = rotation <= 90f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp) // Hauteur délimitée de la carte pour forcer le scroll si débordement
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 12f * density
            }
            .clickable { onFlip() }, // Clic sur la structure globale de la carte
        colors = CardDefaults.cardColors(
            containerColor = if (isAtRecto) CardRectoBackground else Color.White
        ),
        border = if (isAtRecto) null else BorderStroke(1.dp, Color(0xFFE0E0E0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Section Header de la Carte
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { if (!isAtRecto) rotationY = 180f },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val subLabel = if (isAtRecto) rectoSubLabel else versoSubLabel
                    if (subLabel.isNotEmpty()) {
                        Text(
                            text = subLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isAtRecto) CardRectoContent.copy(alpha = 0.6f) else Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = if (isAtRecto) rectoLabel else versoLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isAtRecto) CardRectoContent else Color.Black
                    )
                }
                Box(modifier = Modifier.size(48.dp)) { actions() }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Section Contenu Central (Zone d'affichage KaTeX)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isAtRecto) {
                    // Face RECTO : Visible uniquement si l'animation cible le Recto
                    MathText(
                        text = recto,
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (isAtRecto) CardRectoContent else LocalContentColor.current,
                        onRendered = { rectoReady = true },
                        onClick = onFlip,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Face VERSO : Visible uniquement si l'animation cible le Verso
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f },
                        contentAlignment = Alignment.Center
                    ) {
                        MathText(
                            text = verso,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.Black,
                            onClick = onFlip,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
